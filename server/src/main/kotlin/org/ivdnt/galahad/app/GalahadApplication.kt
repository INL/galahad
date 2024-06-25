package org.ivdnt.galahad.app

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.servlet.http.HttpServletRequest
import org.apache.logging.log4j.kotlin.Logging
import org.apache.tomcat.util.http.fileupload.FileUploadException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.error.ErrorController
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.filter.CommonsRequestLoggingFilter
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.net.URI
import java.util.concurrent.TimeUnit
import java.util.regex.Matcher
import java.util.regex.Pattern

// This is a possibly incomplete list of all the endpoints
// For a complete overview better go to
// SWAGGER_API_URL
const val BASE_URL = "/"
const val SWAGGER_API_URL = "/swagger-ui/index.html"

const val TAGSETS_URL = "/tagsets"
const val BENCHMARKS_URL = "/benchmarks"
const val VERSION_URL = "/version"

const val TAGGERS_URL = "/taggers"
const val TAGGER_URL = "$TAGGERS_URL/{tagger}"
const val TAGGER_HEALTH_URL = "$TAGGER_URL/health"

const val ASSAYS_URL = "/assays"

const val INTERNAL_JOBS_URL = "/internal/jobs"
const val INTERNAL_JOBS_RESULT_URL = "$INTERNAL_JOBS_URL/result"
const val INTERNAL_JOBS_ERROR_URL = "$INTERNAL_JOBS_URL/error"

const val CORPORA_URL = "/corpora"
const val PUBLIC_CORPORA_URL = "/public_corpora"
const val DATASETS_CORPORA_URL = "/datasets_corpora"
const val CORPUS_URL = "$CORPORA_URL/{corpus}"

const val JOBS_URL = "$CORPUS_URL/jobs"
const val JOB_URL = "$JOBS_URL/{job}"
const val JOB_DOCUMENT_URL = "$JOB_URL/documents/{document}"

const val EVALUATION_URL = "$JOB_URL/evaluation"
const val ASSAY_URL = "$EVALUATION_URL/assay"
const val DISTRIBUTION_URL = "$EVALUATION_URL/distribution"
const val METRICS_URL = "$EVALUATION_URL/metrics"
const val METRICS_CSV_URL = "$METRICS_URL/download"
const val CONFUSION_URL = "$EVALUATION_URL/confusion"
const val CONFUSION_CSV_URL = "$CONFUSION_URL/download"
const val EVALUATION_CSV_URL = "$EVALUATION_URL/download"

const val DOCUMENTS_URL = "$CORPUS_URL/documents"
const val DOCUMENT_URL = "$DOCUMENTS_URL/{document}"
const val DOCUMENT_RAW_FILE_URL = "$DOCUMENT_URL/raw" // returns the blob of the raw document

var application_profile: String = System.getenv("spring.profiles.active") ?: "prod"
fun String.runCommand(workingDir: File, timeout: Long = 60): String? {
	try {
		val parts = this.split("\\s".toRegex())
		val proc = ProcessBuilder(*parts.toTypedArray())
			.directory(workingDir)
			.inheritIO()
			.start()

		proc.waitFor(timeout, TimeUnit.MINUTES)
		return proc.inputStream.bufferedReader().readText()
	} catch(e: IOException) {
		e.printStackTrace()
		return null
	}
}

@Configuration
@ConfigurationProperties(prefix = "")
class Config {

	lateinit var workDir: String

	@Bean
	fun getWorkingDirectory(): File {
		return File( workDir )
	}

}

@ComponentScan("org.ivdnt.galahad")
@SpringBootApplication
class GalahadApplication

fun main(args: Array<String>) {
	runApplication<GalahadApplication>(*args)
}

@RestController
class ApplicationController : ErrorController, Logging {

	@Autowired
	private val request: HttpServletRequest? = null

	@GetMapping( BASE_URL )
	@CrossOrigin
	fun getApplication(): ResponseEntity<Void> {
		// Since we have nothing to show at this URL, we redirect to the API UI instead
		logger.info( "Get root" )
		return ResponseEntity.status(HttpStatus.FOUND).location(URI.create(request?.contextPath + SWAGGER_API_URL)).build()
	}

	@GetMapping( "/user" )
	@CrossOrigin
	fun getUser(): User {
		logger.info( "Get user" )
		return User.getUserFromRequestOrThrow( request )
	}

	@GetMapping( BENCHMARKS_URL )
	@CrossOrigin
	fun getBenchmarks(): ByteArray? {
		logger.info( "Get benchmarks" )
		val file = File( "benchmarks.yml" )
		return if( file.exists() ) file.readBytes() else null
	}

	@GetMapping( VERSION_URL )
	@CrossOrigin
	fun getVersion(): ByteArray {
		logger.info( "Get version" )
		return this::class.java.classLoader.getResourceAsStream("version.yml")!!.readBytes()
	}


	data class ErrorResponse(
		@JsonProperty val statusCode: HttpStatus,
		@JsonProperty val message: String,
	)

	@RequestMapping("/error")
//	@ResponseBody
	@CrossOrigin
	fun handleError(request: HttpServletRequest): ErrorResponse {
		val statusCode = HttpStatus.valueOf( request.getAttribute("jakarta.servlet.error.status_code") as Int? ?: 500 )
		val exception = request.getAttribute("jakarta.servlet.error.exception") as Exception?
		if (exception?.cause is FileUploadException) {
			// The error message is good as is. No need to wrap it
			return ErrorResponse( statusCode, (exception.cause as FileUploadException).message ?: "exception inception" )
		}
		return ErrorResponse( statusCode, "${if (exception == null) "N/A" else exception.message}")
	}

}

@Configuration
@ConfigurationProperties(prefix = "spring.servlet.multipart")
class MultipartConfig {

	lateinit var maxFileSize: String
	lateinit var maxRequestSize: String

	val maxFilesSizeAsBytes: Long
		get() {
			return toBytes(maxFileSize)
		}

	companion object {
		fun toBytes(filesize: String?): Long {
			var returnValue: Long = -1
			val patt: Pattern = Pattern.compile("([\\d.]+)([GMK]B)", Pattern.CASE_INSENSITIVE)
			val matcher: Matcher = patt.matcher(filesize)
			val powerMap: MutableMap<String, Int> = HashMap()
			powerMap["GB"] = 3
			powerMap["MB"] = 2
			powerMap["KB"] = 1
			if (matcher.find()) {
				val number: String = matcher.group(1)
				val pow = powerMap[matcher.group(2).uppercase()]!!
				var bytes = BigDecimal(number)
				bytes = bytes.multiply(BigDecimal.valueOf(1024).pow(pow))
				returnValue = bytes.longValueExact()
			}
			return returnValue
		}
	}
}

@Configuration
class RequestLoggingFilterConfig {
	@Bean
	fun logFilter(): CommonsRequestLoggingFilter {
		val filter = CommonsRequestLoggingFilter()
		filter.setIncludeQueryString(true)
		filter.setIncludePayload(true)
		filter.setMaxPayloadLength(10000)
		filter.setIncludeHeaders(true)
		filter.setAfterMessagePrefix("REQUEST DATA : ")
		return filter
	}
}