package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.taggers.Tagset
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class TagsetsController : Logging {
    @Operation(summary = "List all tagsets", description = "List the metadata of all tagsets.")
    @GetMapping(Endpoints.Tagsets.BASE)
    fun getTagsets(): Iterable<Tagset> = Tagset.tagsets.values

    @Operation(summary = "Get tagset by id", description = "Metadata of the tagset.")
    @ApiResponse(responseCode = "200", description = "Metadata of the tagset.")
    @ApiResponse(
        responseCode = "404",
        description = "The tagset was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Tagsets.TAGSET)
    fun getTagset(@PathVariable @Parameter(description = "Tagset name") tagset: String): Tagset =
        Tagset.readOrThrow(tagset)
}
