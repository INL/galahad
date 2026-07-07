package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Hidden
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.exceptions.ErrorResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.error.ErrorController
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@CrossOrigin
class ErrorController : ErrorController, Logging {
    @Autowired private val response: HttpServletResponse? = null

    @RequestMapping("/error")
    @Hidden
    @ExceptionHandler
    private fun handleError(request: HttpServletRequest): ErrorResponse {

        // Get the default status code (probably 500), or override it with the actual status code if
        // it
        // is a RESTException.
        var statusCode =
            HttpStatus.valueOf(
                request.getAttribute("jakarta.servlet.error.status_code") as Int? ?: 500
            )
        val jakartaException = request.getAttribute("jakarta.servlet.error.exception") as Exception?
        response?.status = statusCode.value()

        // Error message
        var jakartaErrorMsg = request.getAttribute("jakarta.servlet.error.message") as String?
        if (jakartaErrorMsg.isNullOrBlank()) {
            jakartaErrorMsg = null
        }
        val springException =
            request.getAttribute("org.springframework.web.servlet.DispatcherServlet.EXCEPTION")
                as Exception?
        var errorMsg =
            jakartaErrorMsg
                ?: jakartaException?.cause?.message
                ?: springException?.message
                ?: jakartaException?.message
                ?: "No error message available."

        // Is it our fault?
        if (statusCode.value() >= 500) {
            // Don't reveal the exception to the user
            errorMsg = "Internal server error"
            // Log the stack trace
            val stackTrace =
                jakartaException?.stackTraceToString() ?: springException?.stackTraceToString()
            stackTrace?.let(logger::error)
        }

        // If this response normally responds with application/zip, change the content
        response?.contentType = "application/json"
        // and remove content disposition
        response?.setHeader("Content-Disposition", "")

        return ErrorResponse(statusCode, errorMsg)
    }
}
