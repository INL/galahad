package org.ivdnt.galahad.evaluation.comparison

import org.ivdnt.galahad.data.layer.WordForm

/**
 * Helper class to compare two word forms.
 */
data class WordFormComparison(
    val wordForm1: WordForm,
    val wordForm2: WordForm,
) {
    val fullOverlap: Boolean
        get() = equalStart && equalEnd

    val partialOverlap: Boolean
        get() = !fullOverlap && (wordForm1.endOffset >= wordForm2.offset) && (wordForm1.offset <= wordForm2.endOffset)

    private val equalStart: Boolean
        get() = wordForm1.offset == wordForm2.offset

    private val equalEnd: Boolean
        get() = wordForm1.endOffset == wordForm2.endOffset
}