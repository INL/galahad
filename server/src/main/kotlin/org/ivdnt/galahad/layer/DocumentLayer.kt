package org.ivdnt.galahad.layer

/** Layer storing a single document with its ID and paragraphs. */
class DocumentLayer(
    /** Document ID. (E.g. conllu: "newdoc id".) */
    val id: String,
    /** Paragraphs in this document. */
    val paragraphs: List<ParagraphLayer>,
) {
    /** Document as string, concatenating all paragraphs with an empty line in between. */
    override fun toString(): String = paragraphs.joinToString("\n\n")
}
