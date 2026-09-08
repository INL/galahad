package org.ivdnt.galahad.evaluation.comparison

import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.util.TermIterator

/**
 * Match the [Layer.terms] of two layers based on their [WordForm] position (offset and length) When
 * filters are provided, only match [TermComparison] that match the filter (on annotation value). In
 * this way, matches only contain samples that you want to download. (Still, aggregating these
 * matches is up to the (corpus/documents) evaluation classes)
 */
open class LayerComparison(hypothesis: Layer, reference: Layer, val filter: LayerFilter? = null) {
    constructor(export: DocumentExport) : this(export.layer, export.sourceLayer)

    val matches: MutableList<TermComparison> = mutableListOf()
    private val hypoIter = TermIterator(hypothesis.terms.iterator())
    private val refIter = TermIterator(reference.terms.iterator())

    init {
        // Iterate through the terms of both layers simultaneously while non-null and compare them.
        while (hypoIter.current != null && refIter.current != null) {
            if (hypoIter.chars == refIter.chars) {
                match(TermComparison(hypoIter.current!!, refIter.current!!))
                hypoIter.next()
                refIter.next()
            } else {
                // Mismatch: advance the iterator that is behind.
                if (refIter.chars < hypoIter.chars) {
                    match(TermComparison(Term.EMPTY, refIter.current!!))
                    refIter.next()
                } else {
                    match(TermComparison(hypoIter.current!!, Term.EMPTY))
                    hypoIter.next()
                }
            }
        }
        // Either one, or both are null. Add any remaining terms.
        refIter.current?.let { match(TermComparison(Term.EMPTY, it)) }
        hypoIter.current?.let { match(TermComparison(it, Term.EMPTY)) }
        refIter.forEachRemaining { match(TermComparison(Term.EMPTY, it!!)) }
        hypoIter.forEachRemaining { match(TermComparison(it!!, Term.EMPTY)) }
    }

    private fun match(comp: TermComparison) {
        if (filter?.filter(comp) != false) {
            matches.add(comp)
        }
    }
}
