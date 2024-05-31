package org.ivdnt.galahad.data.layer

import com.fasterxml.jackson.annotation.JsonProperty

/**
 * Stores the size of the [Layer] in terms of number of [WordForm], [Term], lemma and pos.
 */
data class LayerSummary(
    @JsonProperty("numWordForms") val numWordForms: Int = 0,
    @JsonProperty("numTerms") val numTerms: Int = 0,
    @JsonProperty("numLemma") val numLemma: Int = 0,
    @JsonProperty("numPOS") val numPOS: Int = 0,
)

operator fun LayerSummary.plus(b: LayerSummary): LayerSummary {
    return LayerSummary(
        numWordForms = this.numWordForms + b.numWordForms,
        numTerms = this.numTerms + b.numTerms,
        numLemma = this.numLemma + b.numLemma,
        numPOS = this.numPOS + b.numPOS
    )
}