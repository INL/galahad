package org.ivdnt.galahad.evaluation.comparison

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term

interface TermFilter {
    fun filter(term: Term): Boolean
}

class DummyTermFilter : TermFilter {
    override fun filter(term: Term): Boolean = true
}

class CombinedTermFilter(private val filters: List<TermFilter>) : TermFilter {
    override fun filter(term: Term): Boolean = filters.all { it.filter(term) }
}

class BasicTermFilter(private val annotation: Annotation, private val value: String) : TermFilter {
    override fun filter(term: Term): Boolean = term.annotationOrMissing(annotation) == value
}

/**
 * A filter for annotation types that have a head, like pos. To be used with [LayerFilter]
 *
 * @param value When equal to the string [MULTIPLE_POS], will look for multi analysis like PD+NOU.
 *   When equal to [OTHER_POS], will look for the [OTHER_POS_REGEX] to filter on annotations that
 *   don't start with a-z (e.g. Gysseling pos, which is just a number).
 */
class HeadGroupTermFilter(private val annotation: Annotation, private val value: String) :
    TermFilter {
    // Filter methods
    private val multiFilter = { t: Term -> t.isMulti(annotation) }
    private val singleFilter = { t: Term -> t.annotationHeadOrMissing(annotation) == value }

    // Decide which filter to use on class initialization
    private val filterFunc: (Term) -> Boolean =
        when {
            (value.uppercase() == "MULTIPLE") -> multiFilter
            else -> singleFilter
        }

    override fun filter(term: Term): Boolean = filterFunc(term)
}
