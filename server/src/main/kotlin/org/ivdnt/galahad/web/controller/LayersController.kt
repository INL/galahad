package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody as SwaggerRequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletResponse
import java.util.*
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.layers.CorpusLayerMetadata
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.web.service.LayersService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class LayersController(private val layersService: LayersService) : Logging {

    @Autowired private val response: HttpServletResponse? = null

    @Operation(
        summary = "List all layer metadata",
        description = "List all layer metadata in a corpus.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "CorpusLayerMetadata of all layers in the corpus.",
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
    @GetMapping(Endpoints.Layers.BASE)
    fun getLayers(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID
    ): List<CorpusLayerMetadata> = layersService.readAll(corpus)

    @Operation(
        summary = "Get single layer metadata",
        description = "Get the metadata of a single layer.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Layer metadata.",
        content = [Content(mediaType = "text/plain,*/*")],
    )
    @ApiResponse(
        responseCode = "403",
        description = "User needs read-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or layer not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Layers.LAYER)
    fun getLayer(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
    ): CorpusLayerMetadata = layersService.readOrThrow(corpus, layer)

    @Operation(summary = "Delete single layer", description = "Delete a layer and its jobs.")
    @ApiResponse(responseCode = "204", description = "Layer deleted.")
    @ApiResponse(
        responseCode = "403",
        description = "User needs write-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or layer not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @DeleteMapping(Endpoints.Layers.LAYER)
    fun deleteLayer(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
    ): ResponseEntity<String> {
        layersService.deleteOrThrow(corpus, layer)
        return ResponseEntity.noContent().build()
    }

    @Operation(
        summary = "Create a new layer",
        description = "Create a layer for the provided custom tagger.",
    )
    @ApiResponse(responseCode = "201", description = "Layer created.")
    @ApiResponse(
        responseCode = "403",
        description = "User needs write-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @PostMapping(Endpoints.Layers.BASE)
    fun postLayer(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @RequestBody
        @SwaggerRequestBody(
            description = "Tagger metadata.",
            content = [Content(schema = Schema(implementation = Tagger::class))],
        )
        tagger: Tagger,
    ) {
        response?.status = HttpServletResponse.SC_CREATED
        layersService.createOrThrow(corpus, tagger)
    }
}
