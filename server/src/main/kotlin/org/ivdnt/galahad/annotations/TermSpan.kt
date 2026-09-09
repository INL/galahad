package org.ivdnt.galahad.annotations

/** Defines an annotation value spanning multiple [Term]s in a sentence */
class TermSpan(
    /** Indices of the [org.ivdnt.galahad.layer.SentenceLayer.terms] that this span covers. */
    val indices: List<Int>,
    /** Annotation value, e.g. a named entity label. */
    val value: String,
)
