package org.ivdnt.galahad.jobs

import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.app.JSONable

open class Progress(
    @JsonProperty("pending") val pending: Int = 0,
    @JsonProperty("processing") val processing: Int = 0,
    @JsonProperty("failed") val failed: Int = 0,
    @JsonProperty("finished") val finished: Int = 0,
    @JsonProperty("errors") val errors: Map<String, String> = mapOf(), // Map<doc name, error text>
) : JSONable {
    // is-prefixes for boolean are removed by the json parser, so do not call this "isBusy".
    @JsonProperty("busy")
    val busy: Boolean = processing > 0

    val total: Int = pending + processing + failed + finished

    @JsonProperty
    val untagged: Int = total - finished

    val hasError: Boolean = failed > 0
}