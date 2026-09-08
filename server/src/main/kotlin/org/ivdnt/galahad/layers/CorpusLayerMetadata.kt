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
    val tagger: Tagger,
    val documents: Int = 0,
    val preview: LayerPreview = LayerPreview.EMPTY,
    val annotations: LayerAnnotations = LayerAnnotations.EMPTY,
    val structure: LayerStructure = LayerStructure.EMPTY,
    var modified: Long = 0,
) {
    companion object {
        fun create(layers: CorpusLayer, corpus: Corpus): CorpusLayerMetadata {
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
