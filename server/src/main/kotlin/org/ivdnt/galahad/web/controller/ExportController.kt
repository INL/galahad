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
import org.ivdnt.galahad.documents.DocumentFormat
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.ivdnt.galahad.util.setContentDisposition
import org.ivdnt.galahad.web.service.ExportService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@CrossOrigin
@RestController
class ExportController(private val exportService: ExportService) : Logging {

    @Autowired private val response: HttpServletResponse? = null

    @Operation(
        summary = "Convert all documents of a layer",
        description = "Convert and export all documents of a layer to a specific format",
    )
    @ApiResponse(
        responseCode = "200",
        description =
            "The converted documents in a zip file (.zip). Only includes tagged documents.",
        content = [Content(mediaType = "application/zip,*/*")],
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid format",
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
        description = "Corpus or layer not found",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ResponseBody
    @GetMapping(Endpoints.Export.CONVERT)
    fun getCorpusConversion(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
        @RequestParam("format") @Parameter(description = "Export format") format: DocumentFormat,
        @RequestParam(defaultValue = "false")
        @Parameter(description = "Only export the head of PoS (e.g. PD in PD(type=art))")
        posHeadOnly: Boolean = false,
    ) {
        setZipResponseHeader(corpus)
        exportService.convertOrMergeCorpus(corpus, layer, format, merge = false, posHeadOnly)
    }

    @Operation(
        summary = "Merge all layer document",
        description =
            "Merge and export all document of a layer to a specific format. Any documents matching the selected format will be merged, others simply converted.",
    )
    @ApiResponse(
        responseCode = "200",
        description =
            "The converted documents in a zip file (.zip). Only includes tagged documents.",
        content = [Content(mediaType = "application/zip,*/*")],
    )
    @ApiResponse(
        responseCode = "400",
        description = "The format is not supported for merging.",
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
        description = "Corpus or layer not found",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @ResponseBody
    @GetMapping(Endpoints.Export.MERGE)
    fun getCorpusMerge(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
        @RequestParam("format") @Parameter(description = "Export format") format: DocumentFormat,
        @RequestParam(defaultValue = "false")
        @Parameter(description = "Only export the head of PoS (e.g. PD in PD(type=art))")
        posHeadOnly: Boolean = false,
    ) {
        setZipResponseHeader(corpus)
        exportService.convertOrMergeCorpus(corpus, layer, format, merge = true, posHeadOnly)
    }

    @Operation(
        summary = "Convert document of a layer",
        description = "Convert and export a document to a specific format",
    )
    @ApiResponse(
        responseCode = "200",
        description = "The converted document",
        content = [Content(mediaType = "text/plain,*/*")],
    )
    @ApiResponse(
        responseCode = "400",
        description = "Invalid format",
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
        description = "Corpus, layer or document not found",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Export.Documents.CONVERT)
    fun getDocumentConversion(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
        @PathVariable @Parameter(description = "Document name") document: String,
        @RequestParam @Parameter(description = "Export format") format: DocumentFormat,
        @RequestParam(defaultValue = "false")
        @Parameter(description = "Only export the head of PoS (e.g. PD in PD(type=art))")
        posHeadOnly: Boolean = false,
    ) {
        response?.contentType = "text/plain"
        response?.setContentDisposition(exportService.getDocumentName(corpus, document, format))
        exportService.convertDocument(corpus, layer, document, format, posHeadOnly)
    }

    @Operation(
        summary = "Merge layer of a document",
        description = "Merge and export a document, retaining its original format.",
    )
    @ApiResponse(
        responseCode = "200",
        description = "The converted document",
        content = [Content(mediaType = "text/plain,*/*")],
    )
    @ApiResponse(
        responseCode = "400",
        description = "The format of the original document is not supported for merging.",
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
        description = "Corpus, layer or document not found",
        content =
            [Content(array = ArraySchema(schema = Schema(implementation = ErrorResponse::class)))],
    )
    @GetMapping(Endpoints.Export.Documents.MERGE)
    fun getDocumentMerge(
        @PathVariable @Parameter(description = "Corpus UUID") corpus: UUID,
        @PathVariable @Parameter(description = "Layer name") layer: String,
        @PathVariable @Parameter(description = "Document name") document: String,
        @RequestParam(defaultValue = "false")
        @Parameter(description = "Only export the head of PoS (e.g. PD in PD(type=art))")
        posHeadOnly: Boolean = false,
    ) {
        response?.contentType = "text/plain"
        response?.setContentDisposition(exportService.getDocumentName(corpus, document))
        return exportService.mergeDocument(corpus, layer, document, posHeadOnly)
    }

    private fun setZipResponseHeader(corpus: UUID) {
        response!!.contentType = "application/zip"
        val corpusName = exportService.getCorpusName(corpus)
        response.setContentDisposition("$corpusName.zip")
    }
}
