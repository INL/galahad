package org.ivdnt.galahad.jobs

import io.swagger.v3.oas.annotations.Hidden
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.INTERNAL_JOBS_ERROR_URL
import org.ivdnt.galahad.app.INTERNAL_JOBS_RESULT_URL
import org.ivdnt.galahad.data.CorporaController
import org.ivdnt.galahad.data.document.Document
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.port.InternalFile
import org.ivdnt.galahad.port.SourceLayerableFile
import org.ivdnt.galahad.port.tsv.TSVFile
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.tagset.Tagset
import org.ivdnt.galahad.tagset.TagsetStore
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.File
import java.util.*

typealias ProcessingID = UUID
typealias CorpusID = UUID
typealias JobName = String
typealias DocumentName = String

@RestController
@Hidden
class InternalJobController (
    val corpora: CorporaController,
    val config: Config
) : Logging {

    val tagsets = TagsetStore()

    // This is not an efficient implementation. TODO: efficient implementation
    private fun dataForProcessingID( processingID: UUID ): Triple<CorpusID, JobName, DocumentName>?  {
        corpora.all.forEach { corpus ->
            corpus.jobs.readAll().forEach { job ->
                val candidate = job.documentNameForProcessingIDOrNull(processingID)
                if (candidate != null) return Triple(corpus.metadata.expensiveGet().uuid, job.name, candidate)
            }
        }
        return null
    }

    /**
     * This is a special endpoint, as it is not for use with the client,
     * but for use with the taggers
     */
    @PostMapping(INTERNAL_JOBS_RESULT_URL)
    fun receiveTaggerResult(
        @RequestParam(value="file_id", required=false) fileId: UUID,
        @RequestBody file: MultipartFile
    ): String {
        logger.info( "Received result with processing id $fileId" )
        return try {
            // TODO remove the processing ID after processing
            val tempFile = File.createTempFile("job", file.originalFilename!!)
            file.transferTo(tempFile)
            val (corpusID, jobName, documentName) = dataForProcessingID( fileId ) ?: throw Exception("Processing ID not found, was this file uploaded by me?")
            val original: Document = corpora.getUncheckedCorpusAccess( corpusID ).documents.readOrThrow( documentName )
            val job: Job = corpora.getUncheckedCorpusAccess( corpusID ).jobs.readOrThrow( jobName )
            val taggerTagger: Tagger? = job.taggerStore.getSummaryOrNull(job.name, null ).expensiveGet()
            val tagset: Tagset? = tagsets.getOrNull(taggerTagger?.tagset)

            when (val uploadedFile = InternalFile.from(tempFile, DocumentFormat.Tsv).expensiveGet()) {
                // Treat TSVFiles separately form SourceLayerableFiles, because calling sourceLayer() on a TSV
                // Would default its alignment to offset=0. Instead, we force it to align with the original plaintext.
                is TSVFile -> {
                    val alignedLayer = uploadedFile.mapOnPlainText(
                            plaintext =   original.plaintext,
                            layerName = jobName
                    )
                    job.document( documentName ).setResult( Layer(
                            name = jobName,
                            tagset = tagset ?: Tagset.UNKNOWN,
                            wordForms = alignedLayer.wordForms,
                            terms = alignedLayer.terms
                    ) )
                }
                // To my knowledge, not a single tagger outputs non-tsv, so this is unused for now.
                is SourceLayerableFile -> {
                    val sourceLayer = uploadedFile.sourceLayer()
                    job.document( documentName ).setResult( Layer(
                        name = jobName,
                        tagset = tagset ?: Tagset.UNKNOWN,
                        wordForms = sourceLayer.wordForms,
                        terms = sourceLayer.terms
                    ) )
                }
                else -> {
                    throw Exception("File types is not supported")
                }
            }
            // If this was the last file, set active false
            if (job.progress.pending == 0 && job.progress.processing == 0) {
                job.isActive = false
            }
            job.next() // send new files
            "DELETE"
        } catch (e: Exception) {
            // Something went wrong, let the tagger keep the file for investigation
            // Alternatively, the user stopped the original job so there is nowhere to return to.
            logger.error("Could not receive tagger result. Exception $e")
            "KEEP"
        }
    }

    /**
     * This is a special endpoint, as it is not for use with the client,
     * but for use with the taggers
     */
    @PostMapping(INTERNAL_JOBS_ERROR_URL)
    fun receiveTaggerError(
        @RequestParam fileId: UUID,
        @RequestBody message: String
    ): String {
        logger.info( "Received error with processing id $fileId: $message" )
        val (corpusID, jobName, documentName) = dataForProcessingID(fileId) ?: throw Exception("Processing ID not found, was this file uploaded by me?")
        corpora.getUncheckedCorpusAccess( corpusID ).jobs.readOrThrow( jobName ).document( documentName ).setError( message )

        // TODO Even thought we had an error, we can consider job.next() here
        return "KEEP" // or "DELETE"
    }

}