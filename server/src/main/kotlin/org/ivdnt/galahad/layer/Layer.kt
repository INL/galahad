package org.ivdnt.galahad.layer

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.UUID
import org.ivdnt.galahad.annotations.Term

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
    val annotations: LayerAnnotations by lazy { LayerAnnotations.fromTerms(terms.asIterable()) }

    @get:JsonIgnore
    val structure: LayerStructure by lazy { LayerStructure.fromDocuments(documents) }

    @get:JsonIgnore
    val preview: LayerPreview by lazy {
        LayerPreview(terms.take(LayerPreview.LAYER_PREVIEW_LENGTH).toList())
    }

    /** Concatenate all documents with a newline in between. Unix EOF terminated (\n). */
    override fun toString(): String = documents.joinToString("\n\n") + "\n"

    companion object {
        /** Empty utility layer. */
        val EMPTY: Layer = Layer(emptyArray(), "")

        /** Name of the user uploaded layer. */
        const val SOURCE_LAYER: String = "source"
    }
}
