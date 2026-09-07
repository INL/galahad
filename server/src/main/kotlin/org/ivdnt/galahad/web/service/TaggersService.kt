package org.ivdnt.galahad.web.service

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import org.apache.logging.log4j.kotlin.Logging
import org.ivdnt.galahad.taggers.Tagger
import org.ivdnt.galahad.util.JsonUtil
import org.springframework.stereotype.Service

@Service
class TaggersService : Logging {

    fun read(tagger: String): Tagger? = Tagger.readOrThrow(tagger)

    // TODO: could do with a refactor sometime
    fun taggerHealth(tagger: String): Boolean {
        val client = HttpClient.newBuilder().build()
        val tagger = Tagger.readOrThrow(tagger)
        val request = HttpRequest.newBuilder().uri(URI.create("${tagger.url}/health")).build()

        return try {
            val response = client.send(request, HttpResponse.BodyHandlers.ofString())
            val json = JsonUtil.mapper.readTree(response.body())
            json.get("healthy").asBoolean()
        } catch (e: Exception) {
            logger.error(
                "Failed to connect to tagger ${tagger.name} on url ${request.uri()}. Error: $e"
            )
            // If we cannot connect, there is no use in tagging, so just return
            false
        }
    }
}
