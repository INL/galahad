package org.ivdnt.galahad.evaluation.comparison

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.data.layer.WordForm

fun <T> symmetricDifference(
    set1: Set<T>,
    set2: Set<T>,
    equals: (T, T) -> Boolean,
): Set<T> {

    val mset1: MutableSet<T> = HashSet(set1)
    val mset2: MutableSet<T> = HashSet(set2)

    for (v1 in set1) {
        for (v2 in set2) {
            if (equals(v1, v2)) {
                mset1.remove(v1)
                mset2.remove(v2)
            }
        }
    }

    return mset1 union mset2
}

data class TermComparison(
    val hypoTerm: Term, // Hypothesis
    val refTerm: Term, // True reference
) {
    /** Full overlap dependent on the word forms. Overlap of position, not lemma/pos. */
    @get:JsonIgnore
    val fullOverlap: Boolean
        get() = symmetricDifference(hypoTerm.targets.toSet(), refTerm.targets.toSet(),
                                    equals = { wf1: WordForm, wf2: WordForm ->
                WordFormComparison(wf1, wf2).fullOverlap
            }).isEmpty()

    /** Partial overlap dependent on the word forms. Overlap of position, not lemma/pos. */
    // Currently not used.
    @get:JsonIgnore
    val partialOverlap: Boolean
        get() {
            hypoTerm.targets.forEach { target1 ->
                refTerm.targets.forEach { target2 ->
                    if (WordFormComparison(target1, target2).partialOverlap) {
                        return true
                    }
                }
            }
            return false
        }

    @get:JsonIgnore
    val equalPosLemma: Boolean
        get() {
            return (equalLemma) && (equalPOS)
        }

    /** Whether the lemma is equal. When the reference lemma is empty or null, any hypothesis lemma is fine. */
    @get:JsonIgnore
    val equalLemma: Boolean
        get() {
            if (refTerm.lemma == null) return true
            if (refTerm.lemma.isEmpty()) return true
            if (hypoTerm.lemma == null) return false
            return hypoTerm.lemma.equals(refTerm.lemma, true)
        }

    /** Whether the pos is equal. When the reference pos is empty or null, any hypothesis pos is fine. */
    @get:JsonIgnore
    val equalPOS: Boolean
        get() {
            if (refTerm.pos == null) return true
            if (refTerm.pos.isEmpty()) return true
            if (hypoTerm.pos == null) return false
            return hypoTerm.pos.equals(refTerm.pos, true)
        }

    @get:JsonIgnore
    val equalGroupPosHead: Boolean
        get() {
            if (refTerm.posHeadGroup == null) return true
            if (refTerm.posHeadGroup!!.isEmpty()) return true
            if (hypoTerm.posHeadGroup == null) return false
            return hypoTerm.posHeadGroup.equals(refTerm.posHeadGroup, true)
        }

    companion object {
        const val MISSING_MATCH = "Missing match"
    }
}