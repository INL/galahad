package org.ivdnt.galahad.data.layer

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty

/** Avoid empty strings in the CSV representation. */
fun Term.toNonEmptyPair(): Pair<String, String> {
    return (this.pos ?: Term.NO_POS) to (this.lemma ?: Term.NO_LEMMA)
}

/**
 * A term in a [Layer]. A term has a [lemma], a [pos] and refers to one or multiple [WordForm].
 * Referring to multiple [WordForm] is used to represent multi-word terms, although it is currently not used.
 * Lemma and pos can be null.
 */
data class Term(
    @JsonProperty("lemma") val lemma: String?,
    @JsonProperty("pos") val pos: String?,
    @JsonProperty("targets") val targets: MutableList<WordForm>,
) {
    /** Whether the lemma is not null. */
    @get:JsonIgnore
    val hasLemma: Boolean = lemma != null

    /** Whether the pos is not null. */
    @get:JsonIgnore
    val hasPOS: Boolean = pos != null

    @get:JsonIgnore
    val posHeadGroupOrDefault
        get() = posHeadGroup ?: NO_POS

    @get:JsonIgnore
    val lemmaOrDefault
        get() = lemma ?: NO_LEMMA

    @get:JsonIgnore
    val lemmaOrEmpty
        get() = lemma ?: ""

    @get:JsonIgnore
    val posOrEmpty
        get() = pos ?: ""

    /** Whether this term refers to multiple [WordForm]. */
    @get:JsonIgnore
    val isMultiTarget = targets.size > 1

    /** The head of the first [pos]. E.g. "PD" for "PD(type=art)+NOU(num=sg)". */
    @get:JsonIgnore
    val posHead: String? = posToPosHead(pos)

    @get:JsonIgnore
    val isMultiPos: Boolean = pos?.contains("+") ?: false

    /** The head of all [pos]. E.g. "PD+NOU" for "PD(type=art)+NOU(num=sg)". */
    @get:JsonIgnore
    val posHeadGroup: String? = run {
        // Split on +
        if (!isMultiPos) return@run posHead
        val result: String? = pos?.split("+")?.mapNotNull { posToPosHead(it) }?.joinToString("+")
        result
    }

    @get:JsonIgnore
    val posHeadGroupOrEmpty
        get() = posHeadGroup ?: ""

    /** The features of [pos]. E.g. "num=sg" for "NOU(num=sg)". Does not support multi-pos. */
    @get:JsonIgnore
    val posFeatures: String?
        get() {
            if (pos == null) return null
            val featureStart: Int = pos.indexOf('(') ?: -1
            val featureEnd: Int = pos.indexOf(')') ?: -1
            return if (featureStart != -1 && featureEnd != -1) {
                return pos.slice(featureStart + 1 until featureEnd)
            } else null
        }

    /** Offset of the first [WordForm] in [targets].*/
    @get:JsonIgnore
    val firstOffset get() = targets.minOfOrNull { it.offset } ?: -1

    /** String constructed from all the [WordForm] in [targets]. */
    @get:JsonIgnore
    val literals: String
        get() = targets.joinToString(" ") { it.literal }

    companion object {
        const val NO_POS = "NO_POS"
        const val NO_LEMMA = "NO_LEMMA"
        val EMPTY = Term(null, null, mutableListOf())
        private fun posToPosHead(pos: String?): String? {
            return if (pos == null) {
                null
            } else if (pos.contains('(')) {
                // pos contains a non-letter non-digit character
                val headEnd = pos.indexOf('(')
                val head = pos.slice(0 until headEnd)
                if (head.isEmpty()) {
                    pos // pos is non-empty and starts with a non-letter character, e.g.: _
                } else {
                    head
                }
            } else {
                // pos is 0 or more letters only
                pos
            }
        }
    }
}