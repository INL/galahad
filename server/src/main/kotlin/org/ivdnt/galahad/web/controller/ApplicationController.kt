package org.ivdnt.galahad.web.controller

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import java.net.URI
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.Config
import org.ivdnt.galahad.app.User
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@CrossOrigin
@RestController
class ApplicationController : Logging {
    @Autowired private val request: HttpServletRequest? = null

    @Operation(
        summary = "Get version information",
        description = "Get version information and GitHub build information and commit version.",
        responses = [ApiResponse(description = "Version information.")],
    )
    @GetMapping(Endpoints.VERSION)
    fun getVersion(): Map<String, String> =
        Config.galahadVersionYaml.entries.associate { it.key.toString() to it.value.toString() }

    @Hidden
    @GetMapping(Endpoints.BASE)
    fun getApplication(): ResponseEntity<Void> =
        // Since we have nothing to show at this URL, we redirect to the API UI instead
        ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(request?.contextPath + Endpoints.SWAGGER))
            .build()

    @Operation(
        summary = "Get user information",
        description = "Get the username and whether the user is an admin.",
        responses = [ApiResponse(description = "User information.")],
    )
    @GetMapping(Endpoints.USER)
    fun getUser(): User = User.fromRequest(request)
}
