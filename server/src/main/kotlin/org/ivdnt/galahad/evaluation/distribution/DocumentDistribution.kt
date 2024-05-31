package org.ivdnt.galahad.evaluation.distribution

import org.ivdnt.galahad.data.document.DocumentMetadata
import org.ivdnt.galahad.data.layer.Layer

/**
 * The frequency distribution of terms in a document for a specific tagger layer.
 */
class DocumentDistribution(
    hypothesis: Layer,
    meta: DocumentMetadata,
) : Distribution() {
    init {
        totalChars = meta.numChars
        totalAlphabeticChars = meta.numAlphabeticChars
        hypothesis.terms.forEach(::add)
    }
}