package org.ivdnt.galahad.annotations

class LayerStructure(
    val sentences: Int,
    val paragraphs: Int,
    val documents: Int,
) {
    operator fun plus(b: LayerStructure): LayerStructure {
        return LayerStructure(
            sentences = this.sentences + b.sentences,
            paragraphs = this.paragraphs + b.paragraphs,
            documents = this.documents + b.documents,
        )
    }

    companion object {
        val EMPTY: LayerStructure = LayerStructure(0, 0, 0)

        fun fromDocuments(documents: Array<DocumentLayer>): LayerStructure {
            val paragraphs = documents.sumOf { it.paragraphs.size }
            val sentences = documents.sumOf { doc ->
                doc.paragraphs.sumOf { par -> par.sentences.size }
            }
            return LayerStructure(sentences, paragraphs, documents.size)
        }
    }
}
