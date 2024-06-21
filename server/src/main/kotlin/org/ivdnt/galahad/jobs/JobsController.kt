package org.ivdnt.galahad.jobs

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.ivdnt.galahad.app.JOBS_URL
import org.ivdnt.galahad.app.JOB_URL
import org.ivdnt.galahad.data.CorporaController
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
class JobsController(
    val corpora: CorporaController,
) {

    @Autowired
    private val request: HttpServletRequest? = null
    @Autowired
    private val response: HttpServletResponse? = null

    fun UUID.readJobs(): Jobs = corpora.getReadAccessOrThrow(this, request).jobs
    fun UUID.writeJobs(): Jobs = corpora.getWriteAccessOrThrow(this, request).jobs

    @GetMapping(JOBS_URL)
    @CrossOrigin
    fun getJobs(@PathVariable corpus: UUID, @RequestParam includePotentialJobs: Boolean = false): Set<State> {
        return if (includePotentialJobs) corpus.readJobs().readAllJobStatesIncludingPotentialJobs()
        else corpus.readJobs().readAll().map { it.state }.toSet()
    }

    @GetMapping(JOB_URL)
    @CrossOrigin
    fun getJob(@PathVariable corpus: UUID, @PathVariable job: String): State? = corpus.readJobs().readOrNull(job)?.state
    @PostMapping(JOB_URL)
    @CrossOrigin
    fun postJob(@PathVariable corpus: UUID, @PathVariable job: String): Progress? {
        corpus.writeJobs().readOrCreateOrNull(job)?.start()
        return progress(corpus, job)
    }

    @DeleteMapping(JOB_URL)
    @CrossOrigin
    fun cancelOrDeleteJob(
        @PathVariable corpus: UUID,
        @PathVariable job: String,
        @RequestParam hard: Boolean,
    ): Progress? {
        val jobObject = corpus.writeJobs().readOrNull(job)
        if (hard) jobObject?.delete() else jobObject?.cancel()
        return progress(corpus, job) // I don't know if progress is the correct return type here
    }

    @GetMapping("${JOB_URL}/documents/{document}/result")
    @CrossOrigin
    fun getDocumentResult(@PathVariable corpus: UUID, @PathVariable job: String, @PathVariable document: String): DocumentJobResult? {
        val result = corpus.readJobs().readOrNull(job)?.document(document)?.result
        return if (result == null) {
            null
        } else {
            DocumentJobResult(
                preview = result.preview,
                name = result.name,
                tagset = result.tagset,
                summary = result.summary,
            )
        }
    }

    @GetMapping("$JOB_URL/progress")
    @CrossOrigin
    fun progress(@PathVariable corpus: UUID, @PathVariable job: String): Progress? =
        corpus.readJobs().readOrNull(job)?.progress

    /** This is a utility simplified version of poll, because for some purposes the data from progress() is too detailed. */
    @GetMapping("$JOB_URL/isBusy")
    @CrossOrigin
    fun isBusy(@PathVariable corpus: UUID, @PathVariable job: String): Boolean? = progress(corpus, job)?.busy

    /** This is a utility simplified version of poll, because for some purposes the data from progress() is too detailed. */
    @GetMapping("$JOB_URL/hasError")
    @CrossOrigin
    fun hasError(@PathVariable corpus: UUID, @PathVariable job: String): Boolean? = progress(corpus, job)?.hasError
}