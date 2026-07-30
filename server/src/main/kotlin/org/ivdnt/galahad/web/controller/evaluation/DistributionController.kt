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
import org.ivdnt.galahad.evaluation.distribution.TypeToken
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.web.controller.Endpoints
import org.ivdnt.galahad.web.service.evaluation.DistributionService
import org.springframework.web.bind.annotation.*

@RestController
@CrossOrigin
class DistributionController(private val distributionService: DistributionService) : Logging {

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
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "The corpus or layer was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Evaluation.Layer.Distribution.BASE)
    fun getLayerDistribution(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
        @RequestParam @Parameter(description = "Annotation") annotation: Annotation,
        @RequestParam @Parameter(description = "Group") group: Annotation,
    ): List<TypeToken> = distributionService.getLayerDistribution(corpus, layer, annotation, group)
}
