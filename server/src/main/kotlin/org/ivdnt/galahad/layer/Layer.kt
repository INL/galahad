package org.ivdnt.galahad.layer

import com.fasterxml.jackson.annotation.JsonIgnore
import java.util.UUID
import org.ivdnt.galahad.annotations.Term

/** Annotation layer of a file. */
class Layer(
    /** Documents in this layer. (Formats like conllu support multiple documents.) */
    val documents: List<DocumentLayer>,
    /** ID of this layer. Ideally this is the file PID, so that documents may have different ids. */
    val id: String = UUID.randomUUID().toString(),
) {
    /** Terms in this layer. Documents, paragraphs, sentences flattened. */
    @get:JsonIgnore
    val terms: List<Term> by lazy {
        documents.flatMap {
            it.paragraphs.flatMap {
                it.sentences.flatMap { it.terms }
            }
        }
    }

    /** Number of each linguistic annotation. */
    @get:JsonIgnore
    val annotations: LayerAnnotations by lazy { LayerAnnotations.fromTerms(terms) }

    /** Number of documents, paragraphs and sentences. */
    @get:JsonIgnore
    val structure: LayerStructure by lazy { LayerStructure.fromDocuments(documents) }

    /** The first few terms of the layer */
    @get:JsonIgnore
    val preview: LayerPreview by lazy { LayerPreview(terms.take(LayerPreview.LENGTH).toList()) }

    /** Concatenate all documents with a newline in between. Unix EOF terminated (\n). */
    override fun toString(): String = documents.joinToString("\n\n") + "\n"

    companion object {
        /** Empty utility layer. */
        val EMPTY: Layer = Layer(emptyList(), "")

        /** Name of the user uploaded layer. */
        const val SOURCE_LAYER: String = "source"
    }
}
