package org.ivdnt.galahad.web.controller.evaluation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.evaluation.confusion.JobConfusion
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.web.controller.Endpoints
import org.ivdnt.galahad.web.service.evaluation.ConfusionService
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
class ConfusionController(private val confusionService: ConfusionService) : Logging {
    @Operation(
        summary = "Get confusion",
        description =
            "Get the confusion matrix for a job in a corpus. Returns a map of annotation types to confusion matrices for all supported annotations.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "A map of annotation types to confusion matrices.",
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
    @GetMapping(Endpoints.Evaluation.Layer.Confusion.BASE)
    fun getLayerConfusion(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
        @RequestParam @Parameter(description = "Layer name") reference: String = Layer.SOURCE_LAYER,
        @RequestParam @Parameter(description = "Annotation") annotation: Annotation,
    ): JobConfusion = confusionService.getLayerConfusion(corpus, layer, reference, annotation)

    @Operation(
        summary = "Get confusion samples",
        description =
            "Samples of tokens that are confused in a corpus for a specific job, filtered by, e.g., a specific part of speech.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "A zip file containing the samples in csv format.",
        content = [Content(mediaType = "application/zip,*/*")],
    )
    @ApiResponse(
        responseCode = "400",
        description =
            "The annotation type does not exist (or misspelled) or is not present in the layer.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
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
    @GetMapping(Endpoints.Evaluation.Layer.Confusion.DOWNLOAD)
    fun getConfusionSamples(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
        @RequestParam @Parameter(description = "Layer name") reference: String = Layer.SOURCE_LAYER,
        @RequestParam
        @Parameter(description = "Annotation type for which to generate the confusion")
        annotation: Annotation,
        @RequestParam @Parameter(description = "Annotation head to filter on") hypFilter: String,
        @RequestParam @Parameter(description = "Annotation head to filter on") refFilter: String,
    ): ByteArray {
        confusionService.setZipResponseHeader(corpus)
        return confusionService.getConfusionSamples(
            hypFilter,
            refFilter,
            annotation,
            corpus,
            layer,
            reference,
        )
    }
}
