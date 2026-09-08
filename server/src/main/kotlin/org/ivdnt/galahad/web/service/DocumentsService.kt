package org.ivdnt.galahad.web.service

import java.io.BufferedInputStream
import java.io.InputStream
import java.nio.file.Paths
import java.util.*
import java.util.zip.ZipInputStream
import kotlin.io.path.createTempDirectory
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.documents.Document
import org.ivdnt.galahad.documents.DocumentMetadata
import org.ivdnt.galahad.exceptions.FileUploadException
import org.ivdnt.galahad.layer.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.layers.CorpusLayer
import org.ivdnt.galahad.util.ThreadPoolUtil
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile

@Service
class DocumentsService(private val corpora: CorporaService) : Logging {
    fun readAll(corpus: UUID, layer: String): List<DocumentMetadata> =
        corpora.readOrThrow(corpus).layers.readOrThrow(layer).documents.readAll().map {
            it.metadata
        }

    fun readOrThrow(corpus: UUID, layer: String, document: String): Document =
        corpora.readOrThrow(corpus).layers.readOrThrow(layer).documents.readOrThrow(document)

    fun createOrThrow(corpus: UUID, layer: String, file: MultipartFile) {
        val layer: CorpusLayer = corpora.writeOrThrow(corpus).layers.createOrThrow(layer)
        if (file.contentType in ZIP_TYPES) {
            uploadZipFile(layer, file)
        } else {
            createOrThrow(layer, file.originalFilename!!, file.inputStream)
        }
    }

    fun deleteOrThrow(corpus: UUID, layer: String, document: String) {
        // try to delete the document first as it may throw not found
        val corpObj = corpora.writeOrThrow(corpus)
        corpObj.layers.readOrThrow(layer).documents.deleteOrThrow(document)

        // Delete this document in all layers if it was deleted in the source layer
        if (layer == SOURCE_LAYER) {
            corpObj.layers.readAll().forEach { l ->
                l.documents.deleteOrNull(document) // Doesn't matter if null.
            }
            // Delete all jobs and results of this document.
            corpObj.jobs.readAll().forEach {
                it.results.deleteOrNull(document) // Doesn't matter if null.
            }
        }
        // Delete all evaluation cache
        corpObj.evaluation.deleteRecursively()
    }

    private fun uploadZipFile(layer: CorpusLayer, file: MultipartFile) {
        logger.debug("Unzipping ${file.originalFilename}")
        val exceptions = HashMap<String, Exception>()
        val futures =
            ZipInputStream(BufferedInputStream(file.inputStream)).use { stream ->
                generateSequence { stream.nextEntry }
                    .filterNot { it.isDirectory }
                    .map { entry ->
                        val fileName = Paths.get(entry.name).fileName.toString()
                        val entryData = stream.readBytes()
                        ThreadPoolUtil.pool.submit {
                            logger.debug(
                                "Unzipping ${entry.name} in thread ${Thread.currentThread().name}"
                            )
                            try {
                                createOrThrow(layer, fileName, entryData.inputStream())
                            } catch (e: Exception) {
                                exceptions[fileName] = e
                            }
                        }
                    }
                    .toList()
            }
        // TODO use completion service like CorpusMetrics?
        // Wait for all futures to complete
        futures.forEach { it.get() }
        // Check if any exceptions were thrown
        if (exceptions.isNotEmpty()) {
            var message = "${exceptions.size} exceptions encountered: "
            exceptions
                .toList()
                .mapIndexed { index, pair ->
                    message += "(${index + 1}) "
                    message += "File ${pair.first}:"
                    message += pair.second
                }
                .joinToString { " --- " }
            throw FileUploadException(message)
        }
    }

    private fun createOrThrow(layer: CorpusLayer, fileName: String, input: InputStream) {
        val file = createTempDirectory().toFile().resolve(fileName)
        input.use { input -> file.outputStream().use { output -> input.copyTo(output) } }
        layer.documents.createOrThrow(file)
    }

    companion object {
        private val ZIP_TYPES: List<String> =
            listOf("application/zip", "application/x-zip-compressed", "application/octet-stream")
    }
}
