package org.ivdnt.galahad.annotations

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.*
import org.ivdnt.galahad.annotations.LayerPreview.Companion.LAYER_PREVIEW_LENGTH
import org.ivdnt.galahad.annotations.Term.Companion.toSpacedString

/** Annotation layer of a file. */
class Layer(
    /** Documents in this layer. (Formats like conllu support multiple documents.) */
    val documents: Array<DocumentLayer>,
    /** ID of this layer. Ideally this is the file PID, so that documents may have different ids. */
    val id: String = UUID.randomUUID().toString(),
) {
    /** Terms in this layer. Documents, paragraphs, sentences flattened. */
    @get:JsonIgnore
    val terms: Sequence<Term> by lazy { sentences.flatMap { it.terms.asSequence() } }

    /** Sentences in this layer. Documents, paragraphs flattened. */
    private val sentences: Sequence<SentenceLayer> by lazy {
        documents.asSequence().flatMap { doc ->
            doc.paragraphs.asSequence().flatMap { par -> par.sentences.asSequence() }
        }
    }

    @get:JsonIgnore
    val summary: LayerAnnotations by lazy { LayerAnnotations.fromTerms(terms.asIterable()) }

    @get:JsonIgnore
    val structure: LayerStructure by lazy { LayerStructure.fromDocuments(documents) }

    @get:JsonIgnore
    val preview: LayerPreview by lazy { LayerPreview(terms.take(LAYER_PREVIEW_LENGTH).toList()) }

    /**
     * Layer as string, concatenating all documents with a newline in between. Unix EOF terminated
     * (\n).
     */
    override fun toString(): String = documents.joinToString("\n\n") + "\n"

    companion object {
        val EMPTY: Layer = Layer(emptyArray(), "")
        const val SOURCE_LAYER: String = "source"
    }
}

/** Layer storing a single document with its ID and paragraphs. */
class DocumentLayer(
    /** Document ID. (E.g. conllu: "newdoc id".) */
    val id: String,
    /** Paragraphs in this document. */
    val paragraphs: Array<ParagraphLayer>,
) {
    /** Document as string, concatenating all paragraphs with an empty line in between. */
    override fun toString(): String = paragraphs.joinToString("\n\n")
}

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

/**
 * Layer storing a single sentence with its ID, terms, and spans over terms. Annotations in [spans]
 * are present in [terms]
 */
class SentenceLayer(
    /** Sentence ID. (E.g. conllu: "sent_id".) */
    val id: String,
    /** Terms in this sentence. */
    val terms: Array<Term>,
    /** An empty map can be given as argument, for which we want to force this.spans to be null. */
    spans: Map<Annotation, Array<TermSpan>>?,
) {
    /** TermSpans in this sentence per annotation type. */
    val spans: Map<Annotation, Array<TermSpan>>? = spans?.ifEmpty { null }

    /**
     * Sentence as string, concatenating all terms with spaces when [Term.spaceAfter] isn't falsy.
     */
    override fun toString(): String = terms.toSpacedString()
}
