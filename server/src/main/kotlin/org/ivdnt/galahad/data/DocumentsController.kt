package org.ivdnt.galahad.data

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.DOCUMENTS_URL
import org.ivdnt.galahad.app.DOCUMENT_RAW_FILE_URL
import org.ivdnt.galahad.app.DOCUMENT_URL
import org.ivdnt.galahad.app.executeAndLogTime
import org.ivdnt.galahad.data.document.Document
import org.ivdnt.galahad.data.document.DocumentMetadata
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.util.setContentDisposition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.io.InputStream
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipFile

class FileUploadException(
    override val message: String,
) : Exception(message)

class DocumentWriteType(
    val filename: String,
    val inputStream: InputStream,
)

@RestController
class DocumentsController(
    val corpora: CorporaController,
) : Logging {

    @Autowired
    private val request: HttpServletRequest? = null
    @Autowired
    private val response: HttpServletResponse? = null

    fun UUID.readDocs() = corpora.getReadAccessOrThrow(this, request).documents
    fun UUID.writeDocs() = corpora.getWriteAccessOrThrow(this, request).documents
    fun UUID.readJobs() = corpora.getReadAccessOrThrow(this, request).jobs
    fun UUID.writeJobs() = corpora.getWriteAccessOrThrow(this, request).jobs

    @GetMapping(DOCUMENTS_URL)
    @CrossOrigin
    fun getAllDocuments(@PathVariable corpus: UUID): Set<DocumentMetadata> {
        return corpus.readDocs().readAll().mapNotNull {
            // Potentially, the uploaded file might no longer exist, so try.
            try {
                it.metadata.expensiveGet()
            } catch (e: Exception) {
                // Consider the document a lost cause.
                deleteDocument(corpus, it.name)
                null
            }
        }.toSet()
    }

    @GetMapping(DOCUMENT_URL)
    @CrossOrigin
    fun getDocument(@PathVariable corpus: UUID, @PathVariable document: String): DocumentMetadata? =
        corpus.readDocs().readOrNull(document)?.metadata?.expensiveGet()

    @PostMapping(value = [DOCUMENTS_URL], consumes = ["multipart/form-data"])
    @CrossOrigin
    fun uploadFile(
        @RequestBody file: MultipartFile,
        @PathVariable corpus: UUID,
    ) {
        logger.info("Upload file ${file.originalFilename} to corpus $corpus")
        executeAndLogTime("handelFileUpload") {
            if (file.contentType == "application/zip" || file.contentType == "application/x-zip-compressed" || file.contentType == "application/octet-stream") {
                uploadZipFile(file, corpus)
            } else {
                logger.info("${file.originalFilename} is a single file. Will convert it to document.")
                createDocumentWithSourceLayer(
                    corpus,
                    DocumentWriteType(file.originalFilename.toString(), file.inputStream)
                )
                return // needed so executeAndLogTime doesn't think we return a value
            }
        }
    }

    private fun uploadZipFile(file: MultipartFile, corpus: UUID) {
        logger.info("${file.originalFilename} is a zip file. Will unzip it.")
        val localFile = File.createTempFile("zip", file.originalFilename!!)
        file.transferTo(localFile)
        val exceptions = HashMap<String, Exception>() // <filename, exception>
        ZipFile(localFile).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                zip.getInputStream(entry).use { input ->
                    try {
                        if (!entry.isDirectory && entry.name.split(".").last() != "zip"){
                            logger.info("Unzipped ${entry.name} from ${file.originalFilename}. Will convert it to document.")
                            // The entry might be in a subfolder, so extract the true file name.
                            val fileName = Paths.get(entry.name).fileName.toString()
                            createDocumentWithSourceLayer(corpus, DocumentWriteType(fileName, input))
                        }
                    } catch (e: Exception) {
                        // Some things might go wrong when processing a file, for example the file can be invalid
                        // This is however not a reason not to process the other files
                        // But is an exception to throw, we just collect the exceptions and throw them as one
                        exceptions[entry.name] = e
                    }
                }
            }
        }
        if (exceptions.isNotEmpty()) {
            var message = "${exceptions.size} exceptions encountered: "
            exceptions.toList().mapIndexed { index, pair ->
                message += "(${index + 1}) "
                message += "File ${pair.first}:"
                message += pair.second
            }.joinToString { " --- " }
            throw FileUploadException(message)
        }
    }

    fun createDocumentWithSourceLayer(corpus: UUID, value: DocumentWriteType): String {
        // create the document
        val documentName: String
        try {
            documentName = corpus.writeDocs().create(value)
        } catch (e: Exception) {
            // Document is somehow invalid.
            // Show error to user, but don't save the file
            corpus.writeDocs().delete(value.filename)
            throw e
        }
        val document: Document = corpus.readDocs().readOrThrow(documentName)
        // Invalidate job caches.
        invalidateJobCaches(corpus)
        // Invalidate corpus cache
        corpora.readOrNull(corpus)?.invalidateCache()
        // Set the sourceLayer as job.
        val sourceLayerJob = corpus.writeJobs().createOrThrow(SOURCE_LAYER_NAME)
        sourceLayerJob.document(documentName).setResult(document.sourceLayer.read<Layer>())

        return documentName
    }

    private fun invalidateJobCaches(corpus: UUID) {
        val jobs = corpus.writeJobs()
        jobs.readAll().map { it.stateFile.delete() }
    }

    @GetMapping(DOCUMENT_RAW_FILE_URL)
    @CrossOrigin
    fun getRawFile(
        @PathVariable corpus: UUID,
        @PathVariable document: String,
    ): ByteArray {
        logger.info("Get raw file for $document from corpus $corpus")
        @Suppress("UNREACHABLE_CODE") // This is in fact very much reachable
        return executeAndLogTime("getRawFileForDocument") {
            // consider it to be writing, so that you need writing permissions to download.
            val raw = (corpus.writeDocs().readOrNull(document)
                ?: throw Exception("Document $document not found")).getUploadedRawFile()

            response!!.contentType = "text/plain" // TODO set a better type
            response.setContentDisposition(raw.name)
            return raw.readBytes()
        }
    }

    @DeleteMapping(DOCUMENT_URL)
    @CrossOrigin
    fun deleteDocument(
        @PathVariable corpus: UUID,
        @PathVariable document: String,
    ): DocumentMetadata? {
        // Get meta BEFORE deleting.
        val meta = try { corpus.writeDocs().readOrNull(document)?.metadata?.expensiveGet() } catch (e: Exception) { null }
        // Delete all jobs and results of this document.
        corpus.writeJobs().readAll().forEach { it.document(document).delete() }
        // Invalidate corpus cache
        corpora.readOrNull(corpus)?.invalidateCache()
        // Now delete it
        corpus.writeDocs().delete(document)

        return meta
    }
}
