package org.ivdnt.galahad.data.layer

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * A word form is a single word as it appears in the text.
 */
class WordForm(
    /** Literal is not part of the NAF spec, but it greatly speeds up internal processing */
    @JsonProperty("literal") val literal: String,
    @JsonProperty("offset") val offset: Int,
    @JsonProperty("length") val length: Int,
    @JsonProperty("id") var id: String,
) {
    @get:JsonIgnore
    val endOffset get() = offset + length
}