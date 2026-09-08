package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonCreator

/**
 * Defines an annotation value spanning multiple [Term]s in
 * [org.ivdnt.galahad.layer.SentenceLayer.terms].
 */
class TermSpan(
    /** Indices of the [org.ivdnt.galahad.layer.SentenceLayer.terms] that this span covers. */
    val indices: IntArray,
    /** Annotation value, e.g. a named entity label. */
    val value: String,
) {
    @JsonCreator constructor(indices: List<Int>, value: String) : this(indices.toIntArray(), value)
}
