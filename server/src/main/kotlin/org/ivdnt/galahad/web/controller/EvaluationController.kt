package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.evaluation.confusion.JobConfusion
import org.ivdnt.galahad.evaluation.distribution.TypeToken
import org.ivdnt.galahad.evaluation.metrics.CorpusMetrics
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.util.setContentDisposition
import org.ivdnt.galahad.web.service.EvaluationService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
class EvaluationController(private val evaluationService: EvaluationService) : Logging {
    @Autowired private val response: HttpServletResponse? = null

    private fun setZipResponseHeader(corpus: UUID) {
        response!!.contentType = "application/zip"
        val corpusName = evaluationService.getCorpusName(corpus)
        response.setContentDisposition("$corpusName-evaluation.zip")
    }

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
        @RequestParam @Parameter(description = "Layer name") reference: String = SOURCE_LAYER,
    ): ByteArray {
        setZipResponseHeader(corpus)
        return evaluationService.getEvaluation(corpus, layer, reference)
    }

    @CrossOrigin
    @RestController
    inner class DistributionEvaluationController {
        @Operation(
            summary = "Get distribution",
            description = "Get the distribution of annotations in a corpus for a specific layer.",
        )
        @ApiResponse(
            responseCode = "200",
            description = "The distribution of annotations in the corpus.",
        )
        @ApiResponse(
            responseCode = "403",
            description = "User needs read-access.",
            content =
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @ApiResponse(
            responseCode = "404",
            description = "The corpus or layer was not found.",
            content =
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @GetMapping(Endpoints.Evaluation.Layer.Distribution.BASE)
        fun getLayerDistribution(
            @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
            @PathVariable @Parameter(description = "Layer name") layer: String,
            @RequestParam @Parameter(description = "Annotation") annotation: Annotation,
            @RequestParam @Parameter(description = "Group") group: Annotation,
        ): List<TypeToken> =
            evaluationService.getLayerDistribution(corpus, layer, annotation, group)
    }

    @CrossOrigin
    @RestController
    inner class ConfusionEvaluationController {
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
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @ApiResponse(
            responseCode = "404",
            description = "The corpus or job was not found.",
            content =
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @GetMapping(Endpoints.Evaluation.Layer.Confusion.BASE)
        fun getLayerConfusion(
            @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
            @PathVariable @Parameter(description = "Layer name") layer: String,
            @RequestParam @Parameter(description = "Layer name") reference: String = SOURCE_LAYER,
            @RequestParam @Parameter(description = "Annotation") annotation: Annotation,
        ): JobConfusion = evaluationService.getLayerConfusion(corpus, layer, reference, annotation)

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
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @ApiResponse(
            responseCode = "403",
            description = "User needs read-access.",
            content =
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @ApiResponse(
            responseCode = "404",
            description = "The corpus or job was not found.",
            content =
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @GetMapping(Endpoints.Evaluation.Layer.Confusion.DOWNLOAD)
        fun getConfusionSamples(
            @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
            @PathVariable @Parameter(description = "Layer name") layer: String,
            @RequestParam @Parameter(description = "Layer name") reference: String = SOURCE_LAYER,
            @RequestParam
            @Parameter(description = "Annotation type for which to generate the confusion")
            annotation: Annotation,
            @RequestParam
            @Parameter(description = "Annotation head to filter on")
            hypFilter: String,
            @RequestParam
            @Parameter(description = "Annotation head to filter on")
            refFilter: String,
        ): ByteArray {
            setZipResponseHeader(corpus)
            return evaluationService.getConfusionSamples(
                hypFilter,
                refFilter,
                annotation,
                corpus,
                layer,
                reference,
            )
        }
    }

    @CrossOrigin
    @RestController
    inner class MetricsEvaluationController {

        @GetMapping(Endpoints.Evaluation.Corpus.Metrics.BASE)
        fun getCorpusMetrics(
            @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
            @RequestParam @Parameter(description = "Annotations") annotations: List<Annotation>,
            @RequestParam @Parameter(description = "Group") group: Annotation,
            @RequestParam
            @Parameter(description = "Analysis")
            analysis: Annotation.Analysis? = Annotation.Analysis.BOTH,
        ): CorpusMetrics =
            evaluationService.getCorpusMetrics(corpus, annotations, group, analysis!!)

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
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @ApiResponse(
            responseCode = "404",
            description = "The corpus or job was not found.",
            content =
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @GetMapping(Endpoints.Evaluation.Layer.Metrics.BASE)
        fun getLayerMetrics(
            @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
            @PathVariable @Parameter(description = "Layer name") layer: String,
            @RequestParam @Parameter(description = "Layer name") reference: String = SOURCE_LAYER,
            @RequestParam @Parameter(description = "Annotations") annotations: List<Annotation>?,
            @RequestParam @Parameter(description = "Group") group: Annotation?,
            @RequestParam
            @Parameter(description = "Analysis")
            analysis: Annotation.Analysis? = Annotation.Analysis.BOTH,
        ): Any {
            if (group != null && annotations != null) {
                return evaluationService.getLayerMetrics(
                    corpus,
                    layer,
                    reference,
                    annotations,
                    group,
                    analysis!!,
                )
            }
            return evaluationService.getLayerMetrics(corpus, layer, reference, analysis!!)
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
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @ApiResponse(
            responseCode = "403",
            description = "User needs read-access.",
            content =
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @ApiResponse(
            responseCode = "404",
            description = "The corpus or job was not found.",
            content =
                [
                    Content(
                        array = ArraySchema(schema = Schema(implementation = ErrorResponse::class))
                    )
                ],
        )
        @GetMapping(Endpoints.Evaluation.Layer.Metrics.DOWNLOAD)
        fun getMetricsSamples(
            @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
            @PathVariable @Parameter(description = "Layer name") layer: String,
            @RequestParam @Parameter(description = "Layer name") reference: String = SOURCE_LAYER,
            @RequestParam @Parameter(description = "Annotations") annotations: List<Annotation>,
            @RequestParam @Parameter(description = "Group") group: Annotation,
            @RequestParam
            @Parameter(description = "Analysis")
            analysis: Annotation.Analysis? = Annotation.Analysis.BOTH,
            @RequestParam
            @Parameter(description = "Classification type (e.g. true positive)")
            classification: String,
            @RequestParam @Parameter(description = "Group filter") groupFilter: String? = null,
        ): ByteArray { // TODO should this return unit and should we stream into the response out
            // stream?
            setZipResponseHeader(corpus)
            return evaluationService.getMetricsSamples(
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
}
