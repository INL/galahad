package org.ivdnt.galahad.layer

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.annotations.Term.Companion.toSpacedString
import org.ivdnt.galahad.annotations.TermSpan

/**
 * Layer storing a single sentence with its ID, terms, and spans over terms. Annotations in [spans]
 * are present in [terms]
 */
class SentenceLayer(
    /** Sentence ID. (E.g. conllu: "sent_id".) */
    val id: String,
    /** Terms in this sentence. */
    val terms: List<Term>,
    /** An empty map can be given as argument, for which we want to force this.spans to be null. */
    spans: Map<Annotation, List<TermSpan>>?,
) {
    /** TermSpans in this sentence per annotation type. */
    val spans: Map<Annotation, List<TermSpan>>? = spans?.ifEmpty { null }

    /**
     * Sentence as string, concatenating all terms with spaces when [Term.spaceAfter] isn't falsy.
     */
    override fun toString(): String = terms.toSpacedString()
}
