package org.ivdnt.galahad.layer

/** Layer storing a single paragraph with its ID and sentences. */
class ParagraphLayer(
    /** Paragraph ID. (E.g. conllu: "newpar id".) */
    val id: String,
    /** Sentences in this paragraph. */
    val sentences: Array<SentenceLayer>,
) {
    /** Paragraph as string, concatenating all sentences with newlines. */
    override fun toString(): String = sentences.joinToString("\n")
}
