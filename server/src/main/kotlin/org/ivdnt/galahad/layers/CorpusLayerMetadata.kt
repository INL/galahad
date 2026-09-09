package org.ivdnt.galahad.layers

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.layer.Layer.Companion.SOURCE_LAYER
import org.ivdnt.galahad.layer.LayerAnnotations
import org.ivdnt.galahad.layer.LayerAnnotations.Companion.plus
import org.ivdnt.galahad.layer.LayerPreview
import org.ivdnt.galahad.layer.LayerStructure
import org.ivdnt.galahad.taggers.Tagger

/** Cache-able layer metadata. */
class CorpusLayerMetadata(
    /** Tagger metadata of this layer, may be a custom tagger. */
    val tagger: Tagger,
    /** Finished or uploaded documents in this layer. */
    val documents: Int = 0,
    /** First few terms of the first document. */
    val preview: LayerPreview = LayerPreview.EMPTY,
    /** Number of annotations per type in all documents combined. */
    val annotations: LayerAnnotations = LayerAnnotations.EMPTY,
    /** Statistics of structures in all documents combined. */
    val structure: LayerStructure = LayerStructure.EMPTY,
    /** Unix time last modified. */
    var modified: Long = 0,
) {
    companion object {
        /** Create layer metadata for the given [CorpusLayer]. */
        fun create(layers: CorpusLayer, corpus: Corpus): CorpusLayerMetadata {
            // If the layer name matches a tagger, use that.
            // Else it is either the source layer or a custom uploaded layer.
            val tagger =
                try {
                    Tagger.readOrThrow(layers.name)
                } catch (e: Exception) {
                    if (layers.name == SOURCE_LAYER) {
                        Tagger.createSourceTagger(corpus)
                    } else {
                        layers.customTagger
                    }
                }
            // Obtain preview from the first document
            // And annotations & structure as a sum of all.
            val docs = layers.documents.readAll()
            return CorpusLayerMetadata(
                tagger = tagger,
                documents = docs.size,
                preview = docs.firstOrNull()?.layer?.preview ?: LayerPreview.EMPTY,
                annotations =
                    docs
                        .takeUnless { it.isEmpty() }
                        ?.map { it.metadata.annotations }
                        ?.reduce { a, b -> a + b } ?: LayerAnnotations.EMPTY,
                structure =
                    docs
                        .takeUnless { it.isEmpty() }
                        ?.map { it.metadata.structure }
                        ?.reduce { a, b -> a + b } ?: LayerStructure.EMPTY,
                modified = System.currentTimeMillis(),
            )
        }
    }
}
