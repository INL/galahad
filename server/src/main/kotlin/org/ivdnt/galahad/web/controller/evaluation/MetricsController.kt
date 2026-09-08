package org.ivdnt.galahad.web.controller.evaluation

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Analysis
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.evaluation.metrics.CorpusMetrics
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.web.controller.Endpoints
import org.ivdnt.galahad.web.service.evaluation.MetricsService
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class MetricsController(private val metricsService: MetricsService) : Logging {

    @GetMapping(Endpoints.Evaluation.Corpus.Metrics.BASE)
    fun getCorpusMetrics(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestParam @Parameter(description = "Annotations") annotations: List<Annotation>,
        @RequestParam @Parameter(description = "Group") group: Annotation,
        @RequestParam @Parameter(description = "Analysis") analysis: Analysis? = Analysis.BOTH,
    ): CorpusMetrics = metricsService.getCorpusMetrics(corpus, annotations, group, analysis!!)

    @Operation(
        summary = "Get metrics",
        description =
            "Get detailed accuracy metrics for a job in a corpus compared to a ground truth reference.",
    )
    @ApiResponse(responseCode = "200", description = "A map of annotation types to metrics.")
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
    @GetMapping(Endpoints.Evaluation.Layer.Metrics.BASE)
    fun getLayerMetrics(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
        @RequestParam @Parameter(description = "Layer name") reference: String = Layer.SOURCE_LAYER,
        @RequestParam @Parameter(description = "Annotations") annotations: List<Annotation>?,
        @RequestParam @Parameter(description = "Group") group: Annotation?,
        @RequestParam @Parameter(description = "Analysis") analysis: Analysis? = Analysis.BOTH,
    ): Any {
        if (group != null && annotations != null) {
            return metricsService.getLayerMetrics(
                corpus,
                layer,
                reference,
                annotations,
                group,
                analysis!!,
            )
        }
        return metricsService.getLayerMetrics(corpus, layer, reference, analysis!!)
    }

    @Operation(
        summary = "Get metrics samples",
        description =
            "Samples of tokens in a specific grouping (e.g. the NOU-C group for PoS), or a specific statistical hypothesis class (e.g. true positive).",
    )
    @ApiResponse(
        responseCode = "200",
        description = "A zip file containing the samples in csv format.",
        content = [Content(mediaType = "application/zip,*/*")],
    )
    @ApiResponse(
        responseCode = "400",
        description = "The setting or classification type does not exist.",
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
    @GetMapping(Endpoints.Evaluation.Layer.Metrics.DOWNLOAD)
    fun getMetricsSamples(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
        @RequestParam @Parameter(description = "Layer name") reference: String = Layer.SOURCE_LAYER,
        @RequestParam @Parameter(description = "Annotations") annotations: List<Annotation>,
        @RequestParam @Parameter(description = "Group") group: Annotation,
        @RequestParam @Parameter(description = "Analysis") analysis: Analysis? = Analysis.BOTH,
        @RequestParam
        @Parameter(description = "Classification type (e.g. true positive)")
        classification: String,
        @RequestParam @Parameter(description = "Group filter") groupFilter: String? = null,
    ): ByteArray { // TODO should this return unit and should we stream into the response out
        // stream?
        metricsService.setZipResponseHeader(corpus)
        return metricsService.getMetricsSamples(
            corpus,
            layer,
            reference,
            annotations,
            group,
            analysis!!,
            classification,
            groupFilter,
        )
    }
}
