package org.ivdnt.galahad.app

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.servers.Server
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import java.io.File
import java.util.*
import org.apache.catalina.connector.Connector
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.documents.DocumentFormat
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory
import org.springframework.boot.web.server.servlet.ServletWebServerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

val application_profile: String = System.getenv("spring.profiles.active") ?: "prod"

fun main(args: Array<String>) {
    runApplication<Galahad>(*args)
}

@Configuration
@ConfigurationProperties(prefix = "")
class Config {
    lateinit var workDir: String

    @Bean fun getWorkingDirectory(): File = File(workDir)

    companion object {
        val galahadVersionYaml: Properties by lazy {
            Properties().apply { load(Config::class.java.getResourceAsStream("/version.yml")) }
        }
        val galahadVersion: String by lazy { galahadVersionYaml.getProperty("GITHUB_REF_NAME") }
    }
}

@SpringBootApplication(scanBasePackages = ["org.ivdnt.galahad"])
@EnableScheduling
class Galahad {

    /** Customize OpenAPI documentation header. */
    @Bean
    fun customOpenAPI(): OpenAPI {
        var api =
            OpenAPI()
                .components(Components())
                .info(
                    Info()
                        .title("GaLAHaD API")
                        .version(Config.galahadVersion)
                        .license(
                            License().name("Apache 2.0").url("https://www.apache.org/licenses/")
                        )
                        .description("Generating Linguistic Annotations for Historical Dutch")
                        .contact(
                            Contact()
                                .name("GaLAHaD GitHub")
                                .url("https://github.com/instituutnederlandsetaal/galahad")
                        )
                )
        if ("prod" in application_profile) {
            api = api.servers(listOf(Server().url("/galahad/api").description("GaLAHaD API")))
        }
        return api
    }
}

@Configuration
class DocumentFormatConverter : Converter<String, DocumentFormat> {
    override fun convert(source: String): DocumentFormat = DocumentFormat.fromString(source)
}

@Configuration
class AnnotationConverter : Converter<String, Annotation> {
    override fun convert(source: String): Annotation = Annotation.fromString(source)
}

@Configuration
class AnalysisConverter : Converter<String, Annotation.Analysis> {
    override fun convert(source: String): Annotation.Analysis =
        Annotation.Analysis.valueOf(source.uppercase())
}

@Configuration
class InternalPortConfig {

    @Bean
    fun servletContainer(): ServletWebServerFactory {
        val factory = TomcatServletWebServerFactory()

        val internal =
            Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL).apply {
                port = 8081
            }

        factory.addAdditionalConnectors(internal)
        return factory
    }
}

@Component
class InternalPortFilter : OncePerRequestFilter() {

    override fun shouldNotFilter(request: HttpServletRequest): Boolean =
        !request.requestURI.startsWith("/internal/")

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        if (request.localPort != 8081) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN)
            return
        }

        filterChain.doFilter(request, response)
    }
}
