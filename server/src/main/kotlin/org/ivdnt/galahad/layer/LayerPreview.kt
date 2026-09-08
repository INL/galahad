package org.ivdnt.galahad.layer

import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.layer.LayerPreview.Companion.LAYER_PREVIEW_LENGTH

/** Preview of a [LAYER_PREVIEW_LENGTH] [org.ivdnt.galahad.annotations.Term]s of [Layer]. */
data class LayerPreview(val terms: List<Term>) {
    companion object {
        const val LAYER_PREVIEW_LENGTH: Int = 15
        val EMPTY: LayerPreview = LayerPreview(emptyList())
    }
}
