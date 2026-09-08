package org.ivdnt.galahad.evaluation.comparison

import org.ivdnt.galahad.annotations.Analysis
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term

data class TermComparison(
    val hyp: Term, // Hypothesis
    val ref: Term, // True reference
) {
    /**
     * Apply a removal regex transformation to the annotation before comparing. E.g. removing _ from
     * lemmas.
     */
    fun equal(annotation: Annotation, regex: Regex): Boolean {
        var refAnnot: String? = ref.annotations[annotation]
        var hypAnnot: String? = hyp.annotations[annotation]

        if (refAnnot != null) {
            refAnnot = regex.replace(refAnnot, "")
        }
        if (hypAnnot != null) {
            hypAnnot = regex.replace(hypAnnot, "")
        }

        return equal(refAnnot, hypAnnot)
    }

    fun equal(annotation: Annotation): Boolean {
        val refAnnot: String? = ref.annotations[annotation]
        val hypAnnot: String? = hyp.annotations[annotation]
        return equal(refAnnot, hypAnnot)
    }

    fun has(annotation: Annotation, analysis: Analysis): Boolean {
        return when (analysis) {
            Analysis.SINGLE -> !hyp.isMulti(annotation) && !ref.isMulti(annotation)
            Analysis.MULTIPLE -> hyp.isMulti(annotation) || ref.isMulti(annotation)
            Analysis.BOTH -> true
        }
    }

    companion object {
        const val MISSING_MATCH: String = "MISSING_MATCH"

        private fun equal(ref: String?, hyp: String?): Boolean {
            if (ref == null) return true
            if (ref.isEmpty()) return true
            if (hyp == null) return false
            return hyp.equals(ref, true)
        }
    }
}
