package org.ivdnt.galahad.data.layer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.port.tsv.TSVEntry
import org.ivdnt.galahad.tagset.Tagset

/**
 * A layer represents the tokenization, lemma and pos tags produced by a tagger. One layer per tagger
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class Layer(
    /** The name of the tagger that tagged this layer, unless it's the source layer, in which case [SOURCE_LAYER_NAME]. */
    @JsonProperty("name") val name: String = "",
    /** The [Tagset] used by the Tagger that tagged this layer. */
    @JsonProperty("tagset") val tagset: Tagset = Tagset.UNKNOWN,
    @JsonProperty("wordForms") val wordForms: MutableList<WordForm> = mutableListOf(),
    @JsonProperty("terms") val terms: MutableList<Term> = mutableListOf(),
) {
    /** Numerical summary of the [terms] and [wordForms] in this [Layer] and their containing number of [Term.lemma] and [Term.pos]. */
    val summary
        get() = LayerSummary(numWordForms = wordForms.size, numTerms = terms.size,
            numLemma = terms.count { it.hasLemma }, numPOS = terms.count { it.hasPOS })

    /** A preview of the [terms] and [wordForms] in this [Layer] up to the first [Term] whose [Term.firstOffset] exceeds [LAYER_PREVIEW_LENGTH].
     * Note that this [Term.firstOffset] corresponds to a [WordForm.offset]. */
    val preview
        get() = LayerPreview(wordForms.filter { it.offset < LAYER_PREVIEW_LENGTH },
            terms.filter { it.firstOffset < LAYER_PREVIEW_LENGTH })

    /** Whether the [terms] of this [Layer] have been indexed in [lookup]. */
    private var isIndexed: Boolean = false

    /** Backing field for [lookup]*/
    private val _lookup: MutableMap<String, Term> = HashMap()

    /** An indexed map of [WordForm.id] to the corresponding [Term]. */
    private val lookup: MutableMap<String, Term>
        get() {
            if (!isIndexed) {
                terms.forEach { term ->
                    term.targets.forEach {
                        _lookup[it.id] = term
                    }
                }
                isIndexed = true
            }
            return _lookup
        }

    fun addWordFormWithAnonymousId(literal: String, offset: Int, length: Int): WordForm {
        // TODO There is an edge case here, if the original document uses the w1 id convention
        // and we add wordforms both anonymously add normally
        // id for some wordforms will be the same, and this will break further processing
        // TODO fix it
        val wf = WordForm(literal, offset, length, "w${wordForms.size}")
        wordForms.add(wf)
        return wf
    }

    /** Adds a [WordForm] and a [Term] to this [Layer] based on a [TSVEntry] */
    fun addTSVEntryOnOffset(tsvEntry: TSVEntry, offset: Int) {
        val wordForm = addWordFormWithAnonymousId(tsvEntry.literal, offset, tsvEntry.literal.length)
        val term = Term(
            lemma = tsvEntry.lemma, pos = tsvEntry.pos, targets = mutableListOf(wordForm)
        )
        terms.add(term)
    }

    /** The [Term] belonging to this [WordForm] according to the [Layer], or [Term.EMPTY] if not found. */
    fun termForWordForm(wordForm: WordForm): Term {
        return lookup[wordForm.id] ?: Term.EMPTY
    }

    /** Returns the first matching [WordForm]*/
    fun getWordFormByID(id: String): WordForm {
        return wordForms.first { it.id == id }
    }

    companion object {
        val EMPTY = Layer("EMPTY")
    }
}