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
import org.ivdnt.galahad.documents.DocumentMetadata
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.util.setContentDisposition
import org.ivdnt.galahad.web.service.DocumentsService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

// Note that some API responses have */* as content type.
// For swagger to work, all media types have to be defined on the 200 response.
@CrossOrigin
@RestController
class DocumentsController(private val documentsService: DocumentsService) : Logging {

    @Autowired private val response: HttpServletResponse? = null

    @Operation(
        summary = "List all documents metadata",
        description = "List all documents metadata in a layer.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "DocumentMetadata of all documents in the layer.",
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
    @GetMapping(Endpoints.Layers.Documents.BASE)
    fun getDocuments(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
    ): List<DocumentMetadata> = documentsService.readAll(corpus, layer)

    @Operation(
        summary = "Get single document",
        description = "Get the metadata of a single document.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "Document metadata.",
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
        description = "Corpus, layer or document not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Layers.Documents.DOCUMENT)
    fun getDocument(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
        @PathVariable @Parameter(description = "Layer name") layer: String,
    ): DocumentMetadata = documentsService.readOrThrow(corpus, layer, document).metadata

    @Operation(
        summary = "Get source document",
        description = "Get the raw file of a document, as originally uploaded by the user.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "The raw file of the document.",
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
        description = "Corpus, layer or document not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Layers.Documents.DOWNLOAD)
    fun getSourceDocument(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
        @PathVariable @Parameter(description = "Layer name") layer: String,
    ): ByteArray {
        val file = documentsService.readOrThrow(corpus, layer, document).sourceFile
        response?.contentType =
            "text/plain" // Default for text files. Even if it really means "unknown text file"
        response?.setContentDisposition(file.name)
        return file.readBytes()
    }

    @Operation(
        summary = "Upload document or zip file",
        description = "Upload a document or zip file (.zip) with documents.",
    )
    @ApiResponse(responseCode = "201", description = "Document/zip uploaded.")
    @ApiResponse(
        responseCode = "400",
        description = "The uploaded file is invalid.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
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
    @PostMapping(Endpoints.Layers.Documents.BASE, consumes = ["multipart/form-data"])
    fun postDocument(
        @RequestBody
        @SwaggerRequestBody(description = "Document or zip file to upload.")
        file: MultipartFile,
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
    ) {
        response?.status = HttpServletResponse.SC_CREATED
        documentsService.createOrThrow(corpus, layer, file)
    }

    @Operation(summary = "Delete single document", description = "Delete a document and its jobs.")
    @ApiResponse(responseCode = "204", description = "Document deleted.")
    @ApiResponse(
        responseCode = "403",
        description = "User needs write-access.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ApiResponse(
        responseCode = "404",
        description = "Corpus or document not found.",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @DeleteMapping(Endpoints.Layers.Documents.DOCUMENT)
    fun deleteDocument(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Document name") document: String,
        @PathVariable @Parameter(description = "Layer name") layer: String,
    ): ResponseEntity<String> {
        documentsService.deleteOrThrow(corpus, layer, document)
        return ResponseEntity.noContent().build()
    }
}
