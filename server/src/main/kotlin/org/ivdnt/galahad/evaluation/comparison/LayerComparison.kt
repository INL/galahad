package org.ivdnt.galahad.evaluation.comparison

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.Term

fun ListIterator<Term>.nextOrNull(): Term? {
    val iter = iterator()
    return if (iter.hasNext()) iter.next() else null
}

// Some hardcoded punctuation
val PUNCTUATION = listOf(",", ".", "?", "!", ":", ";", ")", "(", "'", "\"")

/**
 * Match the [Layer.terms] of two layers based on their position (offset and length), not on their actual value (literal/pos/lemma).
 * When pos filters are provide, only match [TermComparison] of that pos.
 */
class LayerComparison(
    private val hypothesisLayer: Layer,
    private val referenceLayer: Layer,
    private val layerFilter: LayerFilter? = null,
) {
    @JsonIgnore
    val matches: MutableList<TermComparison> = ArrayList()

    @JsonIgnore
    val referenceTermsWithoutMatches: MutableList<Term> = ArrayList()

    @JsonIgnore
    val hypothesisTermsWithoutMatches: MutableList<Term> = ArrayList()
    @JsonIgnore
    private val hypoIter: ListIterator<Term> = iterForTermsInLayer(hypothesisLayer)
    @JsonIgnore
    private val refIter: ListIterator<Term> = iterForTermsInLayer(referenceLayer)
    @JsonIgnore
    private var currentHypoTerm: Term? = Term.EMPTY
    @JsonIgnore
    private var currentRefTerm: Term? = Term.EMPTY

    init {
        if (refIter.hasNext() && hypoIter.hasNext()) {
            compare()
        } else {
            hypothesisTermsWithoutMatches.addAll(hypothesisLayer.terms)
            referenceTermsWithoutMatches.addAll(referenceLayer.terms)
        }
    }

    /** Iterate through the terms of both layers simultaneously and compare them. */
    private fun compare() {
        // First terms
        nextHypo()
        nextRef()
        // While there are next terms
        while (currentHypoTerm != null && currentRefTerm != null) {
            val comp = TermComparison(hypoTerm = currentHypoTerm!!, refTerm = currentRefTerm!!)
            compareTerm(comp)
        }
        // One of the two could be non-null. These are not included in the remaining refIter.
        currentHypoTerm?.let(::hypoNoMatch)
        currentRefTerm?.let(::refNoMatch)
        // The remaining terms have no matches
        hypoIter.forEachRemaining(::hypoNoMatch)
        refIter.forEachRemaining(::refNoMatch)
    }

    private fun compareTerm(comp: TermComparison) {
        // Act on the comparison
        if (comp.fullOverlap) {
            fullMatch(comp)
        } else {
            // Unequal first offset
            if (comp.hypoTerm.firstOffset < comp.refTerm.firstOffset) {
                hypoNoMatch()
            } else if (comp.hypoTerm.firstOffset > comp.refTerm.firstOffset) {
                refNoMatch()
            }
            // Equal first offset but no match.
            // Try to truncate either terms to see if the last char is punctuation.
            else if (symmetricTruncatedPcMatch(comp)) {
                // If so, still match it.
                fullMatch(comp)
            } else {
                hypoNoMatch()
                refNoMatch()
            }
        }
    }

    private fun fullMatch(termComparison: TermComparison) {
        if (layerFilter?.filter(termComparison) != false) {
            matches.add(termComparison)
        }
        nextHypo()
        nextRef()
    }

    private fun hypoNoMatch() {
        hypoNoMatch(currentHypoTerm!!)
        nextHypo()
    }

    private fun hypoNoMatch(t: Term) {
        // Note how layerFilter can be null, and both null and true != false.
        if (layerFilter?.hypoTermFilter?.filter(t) != false) {
            hypothesisTermsWithoutMatches.add(t)
        }
    }

    private fun refNoMatch() {
        refNoMatch(currentRefTerm!!)
        nextRef()
    }

    private fun refNoMatch(t: Term) {
        if (layerFilter?.refTermFilter?.filter(t) != false) {
            referenceTermsWithoutMatches.add(t)
        }
    }

    private fun nextHypo() {
        currentHypoTerm = hypoIter.nextOrNull()
    }

    private fun nextRef() {
        currentRefTerm = refIter.nextOrNull()
    }

    /** Iterate through the terms of the layer sorted on offset. */
    private fun iterForTermsInLayer(layer: Layer): ListIterator<Term> {
        return layer.terms
            // Terms can only be a match if their first offset is the same
            .sortedBy { it.firstOffset }.listIterator()
    }

    private fun symmetricTruncatedPcMatch(comp: TermComparison): Boolean {
        val aStr: String = comp.hypoTerm.literals
        val bStr: String = comp.refTerm.literals
        return truncatedPcMatch(aStr, bStr) || truncatedPcMatch(bStr, aStr)
    }

    companion object {
        fun truncatedPcMatch(aStr: String, bStr: String): Boolean {
            if (PUNCTUATION.contains(aStr.last().toString())) {
                if (aStr.slice(0 until aStr.lastIndex) == bStr) {
                    return true
                }
            }
            return false
        }
    }
}