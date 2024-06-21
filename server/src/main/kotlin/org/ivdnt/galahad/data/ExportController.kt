package org.ivdnt.galahad.data

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.JOB_DOCUMENT_URL
import org.ivdnt.galahad.app.JOB_URL
import org.ivdnt.galahad.app.User
import org.ivdnt.galahad.data.document.Document
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.port.CorpusTransformMetadata
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.InternalFile
import org.ivdnt.galahad.util.setContentDisposition
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.io.File
import java.util.*

@RestController
class ExportController(
    val corpora: CorporaController,
) : Logging {

    @Autowired
    private val request: HttpServletRequest? = null

    @Autowired
    private val response: HttpServletResponse? = null

    private fun getCorpusTransformMetadata(corpusID: UUID, jobName: String, formatName: DocumentFormat): CorpusTransformMetadata {
        // Exporting documents requires you to have write access.
        val corpus = corpora.getWriteAccessOrThrow(corpusID, request)
        val job = corpus.jobs.readOrThrow(jobName)
        return CorpusTransformMetadata(
            corpus = corpus, job = job, user = User.getUserFromRequestOrThrow(request), targetFormat = formatName
        )
    }

    private fun getDocumentTransformMetadata(
        corpus: UUID,
        job: String,
        document: String,
        format: DocumentFormat,
    ): DocumentTransformMetadata {
        return getCorpusTransformMetadata(corpus, job, format).documentMetadata(document)
    }

    @GetMapping("$JOB_URL/export/convert")
    @CrossOrigin
    @ResponseBody
    fun convertAndExportJob(
        @PathVariable corpus: UUID,
        @PathVariable job: String,
        @RequestParam("format") formatName: String,
        @RequestParam("posHeadOnly") posHeadOnly: Boolean = false,
    ) {
        return exportCorpusJobInFormat(corpus, job, formatName, shouldMerge = false, posHeadOnly)
    }

    @GetMapping("$JOB_URL/export/merge")
    @CrossOrigin
    @ResponseBody
    fun mergeAndExportJob(
        @PathVariable corpus: UUID,
        @PathVariable job: String,
        @RequestParam("format")
        filterFormat: String, // Needed when raw formats are mixed, since BlackLab only accepts 1 type per corpus
        @RequestParam("posHeadOnly")
        posHeadOnly: Boolean,
    ) {
        return exportCorpusJobInFormat(corpus, job, filterFormat, shouldMerge = true, posHeadOnly)
    }

    fun exportCorpusJobInFormat(corpus: UUID, job: String, formatName: String, shouldMerge: Boolean, posHeadOnly: Boolean) {
        val format = DocumentFormat.fromString(formatName)
        val ctm = getCorpusTransformMetadata(corpus, job, format)
        setZipResponseHeader(ctm)
        ctm.corpus.getZipped(ctm, formatMapper = {
            try {
                // Document conversions.
                val dtm = ctm.documentMetadata(it.name)
                return@getZipped if (shouldMerge && mergeFormatMatches(it, format)) {
                    logger.info("Merging ${it.name} of format ${it.format}")
                    mergeAndExportDocument(dtm, posHeadOnly).file
                } else {
                    logger.info("Converting ${it.name} of format ${it.format} to $format")
                    convertAndExportDocument(dtm, format, posHeadOnly)
                }
            } catch (e: Exception) {
                throw Exception("Could not convert file ${it.name} to format ${format}. ${e.message}.")
            }
        }, filter = {
            // Filter out untagged documents.
                document ->
            ctm.documentMetadata(document.name).layer != Layer.EMPTY
        }, outputStream = response?.outputStream)
    }

    private fun mergeFormatMatches(
        it: Document, format: DocumentFormat,
    ): Boolean {
        var otherFormat = it.format
        // Overwrite the format for legacy formats that can in fact be merged.
        if (otherFormat == DocumentFormat.TeiP5Legacy) {
            otherFormat = DocumentFormat.TeiP5
        }
        return otherFormat == format
    }

    @GetMapping("$JOB_DOCUMENT_URL/export/convert")
    @CrossOrigin
    @ResponseBody
    fun convertAndExportDocument(
        @PathVariable corpus: UUID,
        @PathVariable job: String,
        @PathVariable document: String,
        @RequestParam("format") formatName: String,
        @RequestParam("posHeadOnly") posHeadOnly: Boolean,
    ): ByteArray? {
        val format = DocumentFormat.fromString(formatName)
        val dtm = getDocumentTransformMetadata(corpus, job, document, format)
        // TODO("set headers")
        return convertAndExportDocument(dtm, format, posHeadOnly).readBytes()
    }

    fun convertAndExportDocument(dtm: DocumentTransformMetadata, format: DocumentFormat, posHeadOnly: Boolean): File {
        if (posHeadOnly) {
            dtm.convertLayerToPosHead()
        }
        return dtm.document.generateAs(format, dtm)
    }

    @GetMapping("$JOB_DOCUMENT_URL/export/merge")
    @CrossOrigin
    @ResponseBody
    fun mergeAndExportDocument(
        @PathVariable corpus: UUID,
        @PathVariable job: String,
        @PathVariable document: String,
        @RequestParam("posHeadOnly") posHeadOnly: Boolean,
    ): ByteArray? {
        val doc = corpora.getWriteAccessOrThrow(corpus, request).documents.readOrThrow(document)
        val dtm = getDocumentTransformMetadata(corpus, job, document, doc.format)
        // TODO("set headers")
        return mergeAndExportDocument(dtm, posHeadOnly).file.readBytes()
    }

    fun mergeAndExportDocument(dtm: DocumentTransformMetadata, posHeadOnly: Boolean): InternalFile {
        if (posHeadOnly) {
            dtm.convertLayerToPosHead()
        }
        return dtm.document.merge(dtm)
    }

    private fun setZipResponseHeader(ctm: CorpusTransformMetadata) {
        response!!.contentType = "application/zip"
        response.setContentDisposition(ctm.corpus.metadata.expensiveGet().name + ".zip")
    }
}
