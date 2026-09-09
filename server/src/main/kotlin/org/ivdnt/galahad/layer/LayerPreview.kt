package org.ivdnt.galahad.layer

import org.ivdnt.galahad.annotations.Term

/** Preview of a [LENGTH] [org.ivdnt.galahad.annotations.Term]s of [Layer]. */
data class LayerPreview(val terms: List<Term>) {
    companion object {
        const val LENGTH: Int = 15
        val EMPTY: LayerPreview = LayerPreview(emptyList())
    }
}
