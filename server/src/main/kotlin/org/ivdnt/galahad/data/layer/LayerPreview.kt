package org.ivdnt.galahad.data.layer

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * The minimum length in chars of a [LayerPreview].
 * If the limit ends halfway a word, we incorporate the entire word.
 */
const val LAYER_PREVIEW_LENGTH = 100

/**
 * A preview of a [Layer] in terms of the first N [WordForm] and [Term], where N is in chars and ruled by [LAYER_PREVIEW_LENGTH].
 */
data class LayerPreview(
    @JsonProperty("wordforms") val wordforms: List<WordForm> = listOf(),
    @JsonProperty("terms") val terms: List<Term> = listOf(),
) {
    companion object {
        val EMPTY = LayerPreview(listOf(), listOf())
    }
}
