package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.taggers.Principle
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class PrinciplesController : Logging {
    @Operation(
        summary = "List all principles",
        description = "List the metadata of all principles.",
    )
    @GetMapping(Endpoints.Principles.BASE)
    fun getPrinciples(): Iterable<Principle> = Principle.principles

    @Operation(summary = "Get principle by id", description = "Metadata of the principle.")
    @ApiResponse(responseCode = "200", description = "Metadata of the principle.")
    @ApiResponse(
        responseCode = "404",
        description = "The principle was not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Principles.PRINCIPLE)
    fun getPrinciple(
        @PathVariable @Parameter(description = "Principle name") principle: String
    ): Principle = Principle.readOrThrow(principle)
}
