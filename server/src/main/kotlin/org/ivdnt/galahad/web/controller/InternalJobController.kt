package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Hidden
import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.jobs.JobScheduler
import org.ivdnt.galahad.web.service.CorporaService
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

typealias ProcessingID = UUID

typealias CorpusID = UUID

typealias JobName = String

typealias DocumentName = String

@RestController
@Hidden
class InternalJobController(val corpora: CorporaService, val config: Config) : Logging {
    //    // This is not an efficient implementation. TODO: efficient implementation
    //    private fun dataForProcessingID(processingID: UUID): Triple<CorpusID, JobName,
    // DocumentName>? {
    //        corpora.all.forEach { corpus ->
    //            corpus.jobs.readAll().forEach { job ->
    //                val candidate = job.documentNameForProcessingIDOrNull(processingID)
    //                if (candidate != null) return Triple(corpus.uuid, job.name, candidate)
    //            }
    //        }
    //        return null
    //    }

    /**
     * This is a special endpoint, as it is not for use with the client, but for use with the
     * taggers
     */
    @PostMapping(Endpoints.Internal.RESULT)
    fun receiveTaggerResult(
        @RequestParam(value = "file_id", required = false) fileId: UUID,
        @RequestBody file: MultipartFile,
    ) {
        JobScheduler.receive(fileId, file.inputStream, file.originalFilename!!)
    }

    //    /**
    //     * This is a special endpoint, as it is not for use with the client,
    //     * but for use with the taggers
    //     */
    //    @PostMapping(INTERNAL_JOBS_ERROR_URL)
    //    fun receiveTaggerError(
    //        @RequestParam(value = "file_id") fileId: UUID,
    //        @RequestBody message: String,
    //    ): String {
    //        logger.info("Received error with processing id $fileId: $message")
    //        val (corpusID, jobName, documentName) = dataForProcessingID(fileId)
    //            ?: throw Exception("Processing ID not found, was this file uploaded by me?")
    //
    // corpora.readCorpusUnsafe(corpusID).jobs.readOrThrow(jobName).results.readOrThrow(documentName).error =
    //            message
    //
    //        // TODO Even thought we had an error, we can consider job.next() here
    //        return "KEEP" // or "DELETE"
    //    }
}
