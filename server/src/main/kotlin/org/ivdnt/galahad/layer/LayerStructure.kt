package org.ivdnt.galahad.layer

/** Statistics of the layer structure. */
class LayerStructure(
    /** Number of documents in the layer. */
    val documents: Int,
    /** Number of paragraphs in the layer. */
    val paragraphs: Int,
    /** Number of sentences in the layer. */
    val sentences: Int,
) {
    /** Sum individual structures. */
    operator fun plus(b: LayerStructure): LayerStructure =
        LayerStructure(documents + b.documents, paragraphs + b.paragraphs, sentences + b.sentences)

    companion object {
        /** Empty utility structure. */
        val EMPTY: LayerStructure = LayerStructure(0, 0, 0)

        /** Obtain the structure from an array of DocumentLayers. */
        fun fromDocuments(documents: List<DocumentLayer>): LayerStructure =
            LayerStructure(
                documents.size,
                documents.sumOf { it.paragraphs.size },
                documents.sumOf {
                    it.paragraphs.sumOf { it.sentences.size }
                },
            )
    }
}
