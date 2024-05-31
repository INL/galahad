package org.ivdnt.galahad.taggers

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.beust.klaxon.Parser.Companion.default
import com.fasterxml.jackson.annotation.JsonProperty
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.app.TAGGERS_URL
import org.ivdnt.galahad.app.TAGGER_HEALTH_URL
import org.ivdnt.galahad.app.TAGGER_URL
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.CrossOrigin
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import java.net.URL

@RestController
class TaggersController : Logging {

    @Autowired private val request: HttpServletRequest? = null
    @Autowired private val response: HttpServletResponse? = null

    private val taggers = Taggers()

    @GetMapping( TAGGERS_URL ) @CrossOrigin fun getTaggers(): Set<Taggers.Summary> =
        taggers.summaries.map { it.expensiveGet() }.toSet()

    @GetMapping( TAGGER_URL ) @CrossOrigin fun getTagger( @PathVariable tagger: String ): Taggers.Summary? =
        taggers.getSummaryOrNull( tagger, null ).expensiveGet() // Note: sourceLayer is not a tagger here

    @GetMapping( TAGGER_HEALTH_URL ) @CrossOrigin fun getTaggerHealth( @PathVariable tagger: String ): TaggerHealth =
        expensiveGetHealthFor( tagger )

    fun expensiveGetHealthFor( tagger: String ): TaggerHealth {
        // If there are multiple replicas for the same service, we only get health check response from one replica.
        // However, we still think it is representative/informative
        val client = HttpClient.newBuilder().build()
        val request = HttpRequest.newBuilder()
            .uri(URI.create("${taggers.getURL(tagger)}/health"))
            .build()

        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val parser: Parser = default(  )
            val json: JsonObject = parser.parse( StringBuilder( response.body() ) ) as JsonObject

            /**
             * Note that this queuesize only represents the queue present at a single instance of the tagger,
             * also the processing speed is of a single tagger
             * not any pending documents on the server
             * We could get this by count all pending document for this tagger in all corpora
             */
            TaggerHealth(
                status = if( json.boolean("healthy") == true ) TaggerHealthStatus.HEALTHY else TaggerHealthStatus.NOT_HEALTHY,
                queueSizeAtTagger = json.int("queueSizeAtTagger") ?: 0,
                processingSpeed = json.int("processingSpeed") ?: 0,
                message = "Can connect to tagger. Taggers health response: ${response.body()}"
            )
        } catch ( e: Exception ) {
            logger.error("Failed to connect to tagger $tagger on url ${taggers.getURL(tagger)}. Error: $e")
            // If we cannot connect, there is no use in tagging, so just return
            return TaggerHealth( status = TaggerHealthStatus.ERROR, message = "Cannot connect to tagger" )
        }
    }

    /**
     * Get the active number of documents actively being tagged by retrieving the taggers' status dicts
     * and counting the number of pending and busy docs.
     */
    @GetMapping("$TAGGERS_URL/active")
    @CrossOrigin
    fun getActiveDocsAtTaggers(): Int {
        var count = 0
        for (tagger in taggers.summaries) {
            val name = tagger.expensiveGet().id

            val restTemplate = RestTemplate()
            val endpoint = URL("${taggers.getURL(name)}/status")
            val builder = UriComponentsBuilder.fromUri(endpoint.toURI())
            try {
                val res = restTemplate.exchange(
                    builder.build().encode().toUri(), HttpMethod.GET, null, String::class.java
                )
                val jsonStr: String? = res.body
                val json: JsonObject = default().parse(StringBuilder(jsonStr!!)) as JsonObject
                // Json is a map of uuid -> status dict. Iterate on the uuids.
                for (key in json.keys) {
                    val status = json.obj(key)
                    if (status?.boolean("pending") == true || status?.boolean("busy") == true) {
                            count++
                    }
                }
            }
            catch (e: Exception) {
                logger.error("Failed to connect to tagger $name. Error: $e")
            }
        }
        return count
    }

    enum class TaggerHealthStatus {
        ERROR,
        HEALTHY,
        NOT_HEALTHY,
        UNKNOWN
    }

    class TaggerHealth (
        @JsonProperty val status: TaggerHealthStatus = TaggerHealthStatus.UNKNOWN,
        @JsonProperty val queueSizeAtTagger: Int = 0, // bytes
        @JsonProperty val processingSpeed: Int = 0, // 'chars/s'
        @JsonProperty val message: String = ""
    )
}