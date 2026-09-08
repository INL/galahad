package org.ivdnt.galahad.web.controller.evaluation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.web.controller.Endpoints
import org.ivdnt.galahad.web.service.evaluation.EvaluationService
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
class EvaluationController(private val evaluationService: EvaluationService) : Logging {

    @Operation(
        summary = "Download evaluation",
        description =
            "Download a zip containing all combinations of evaluations (metrics, distribution, confusion) and (available) annotations (e.g. pos, depending on the layer).",
    )
    @ApiResponse(
        responseCode = "200",
        description = "A zip containing all combinations of evaluations.",
        content = [Content(mediaType = "application/zip,*/*")],
    )
    @ApiResponse(
        responseCode = "403",
        description = "User needs read-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or job was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Evaluation.Layer.DOWNLOAD)
    fun download(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
        @RequestParam @Parameter(description = "Layer name") reference: String = Layer.SOURCE_LAYER,
    ): ByteArray {
        evaluationService.setZipResponseHeader(corpus)
        return evaluationService.getEvaluation(corpus, layer, reference)
    }
}
