package org.ivdnt.galahad.formats.reader

import org.ivdnt.galahad.annotations.*
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.layer.DocumentLayer
import org.ivdnt.galahad.layer.Layer
import org.ivdnt.galahad.layer.ParagraphLayer
import org.ivdnt.galahad.layer.SentenceLayer

/**
 * Base implementation of a file parser.
 *
 * Should read individual terms and their annotations,
 * as well as structures like sentences, paragraphs and documents.
 * Reads IDs where relevant or assigns default IDs.
 */
abstract class LayerReader {
    val layer: Layer by lazy { read() }

    protected val documents: MutableList<DocumentLayer> = mutableListOf()
    protected val paragraphs: MutableList<ParagraphLayer> = mutableListOf()
    protected val sentences: MutableList<SentenceLayer> = mutableListOf()
    protected val terms: MutableList<Term> = mutableListOf()
    protected val spans: MutableMap<Annotation, MutableList<TermSpan>> = mutableMapOf()

    protected var docID: String? = null
    protected var parID: String? = null
    protected var sentID: String? = null
    protected var wordID: String? = null

    protected fun docID(): String = docID ?: "d$dIndex"

    protected fun parID(): String = parID ?: "${docID()}.p$pIndex"

    protected fun sentID(): String = sentID ?: "${parID()}.s$sIndex"

    protected fun wordID(): String = wordID ?: "${sentID()}.w$wIndex"

    private val wIndex: Int
        get() = terms.size + 1

    private val sIndex: Int
        get() = sentences.size + 1

    private val pIndex: Int
        get() = paragraphs.size + 1

    private val dIndex: Int
        get() = documents.size + 1

    protected abstract fun read(): Layer

    protected open fun newDocument() {
        newParagraph()
        if (paragraphs.isNotEmpty()) {
            // toList for copying
            documents.add(DocumentLayer(docID(), paragraphs.toList()))
            paragraphs.clear()
        }
    }

    protected open fun newParagraph() {
        newSentence()
        if (sentences.isNotEmpty()) {
            // toList for copying
            paragraphs.add(ParagraphLayer(parID(), sentences.toList()))
            sentences.clear()
        }
    }

    protected open fun newSentence() {
        newWordform()
        if (terms.isNotEmpty()) {
            // toList for copying
            sentences.add(
                SentenceLayer(
                    sentID(),
                    terms.toList(),
                    spans.mapValues { it.value },
                )
            )
            terms.clear()
            spans.clear()
        }
    }

    protected open fun newWordform() {}
}
