package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import java.util.*
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.jobs.JobMetadata
import org.ivdnt.galahad.jobs.Progress
import org.ivdnt.galahad.web.service.JobsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class JobsController(private val jobsService: JobsService) {

    @Autowired private val response: HttpServletResponse? = null

    @Operation(
        summary = "Get all job metadata",
        description = "Get a summary of the state of all tagger jobs.",
    )
    @ApiResponse(
        responseCode = "403",
        description = "User needs read-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Jobs.BASE)
    fun getJobs(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID
    ): List<JobMetadata> = jobsService.readAll(corpus)

    @Operation(
        summary = "Get single job metadata",
        description = "Get a summary of the state of a tagger job.",
    )
    @ApiResponse(responseCode = "200", description = "The job state.")
    @ApiResponse(
        responseCode = "403",
        description = "User needs read-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Jobs.JOB)
    fun getJob(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
    ): JobMetadata = jobsService.readOrThrow(corpus, job)

    @Operation(
        summary = "Get progress of job",
        description = "Get progress of the documents of the job.",
    )
    @ApiResponse(responseCode = "200", description = "The documents progress.")
    @ApiResponse(
        responseCode = "403",
        description = "User needs read-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Jobs.PROGRESS)
    fun getJobProgress(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
    ): Progress = jobsService.readOrThrowProgress(corpus, job)

    @Operation(
        summary = "Start job",
        description = "Start a job. Requires write access to the corpus.",
    )
    @ApiResponse(responseCode = "202", description = "The job progress.")
    @ApiResponse(
        responseCode = "403",
        description = "User needs write-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @PostMapping(Endpoints.Jobs.JOB)
    fun postJob(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
    ) {
        response?.status = HttpServletResponse.SC_ACCEPTED
        jobsService.createOrThrow(corpus, job)
    }

    @Operation(
        summary = "Cancel job",
        description = "Cancel a job. Requires write access to the corpus.",
    )
    @ApiResponse(responseCode = "204", description = "Job cancelled or deleted.")
    @ApiResponse(
        responseCode = "403",
        description = "User needs write-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @DeleteMapping(Endpoints.Jobs.JOB)
    fun deleteJob(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Tagger name") job: String,
    ): ResponseEntity<String> {
        jobsService.deleteOrThrow(corpus, job)
        return ResponseEntity.noContent().build()
    }
}
