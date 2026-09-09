package org.ivdnt.galahad.formats.conllu

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.formats.tsv.TsvMerger

/**
 * Merge a layer with a CoNLLU file. Uses _ for null values. Splits PoS in head and features. Do not
 * call directly. Use [ConlluFile.merge] instead.
 */
class ConlluMerger(export: DocumentExport) : TsvMerger(export) {
    override val emptyValue: String = "_"

    override val columnIndices: MutableMap<Annotation, Int> =
        mutableMapOf(
            Annotation.TOKEN to 1,
            Annotation.LEMMA to 2,
            Annotation.UPOS to 3, // see GetUpos
            Annotation.POS to 4,
            Annotation.HEAD to 6,
            Annotation.DEPREL to 7,
            Annotation.NER to 9, // see GetNer
        )

    override fun replaceColumns(columns: MutableList<String>) {
        replaceMisc(columns)
        super.replaceColumns(columns)
    }

    private fun replaceMisc(columns: MutableList<String>) {
        // construct MISC by combining NER and MISC
        val term: Term = termComparisons[termIndex].hyp
        val ner: String? = term.annotations[Annotation.NER]?.let { "NamedEntity=$it" }
        val spaceAfter: String? = if (term.spaceAfter == false) "SpaceAfter=No" else null
        val miscField: String = listOfNotNull(spaceAfter, ner).joinToString("|")
        columns[columnIndices[Annotation.NER]!!] = miscField.ifEmpty { "_" }
    }

    override fun mergeSingleColumn(
        columns: MutableList<String>,
        annotation: Annotation,
        columnIndex: Int,
    ) {
        when (annotation) {
            Annotation.NER -> {
                // handled in replaceMisc
            }
            Annotation.UPOS -> {
                // Split UPOS into head and features
                val term: Term = termComparisons[termIndex].hyp
                val head: String = term.annotationHead(Annotation.UPOS) ?: "_"
                val features: String = term.features(Annotation.UPOS) ?: "_"
                columns[3] = head
                columns[5] = features
            }
            else -> super.mergeSingleColumn(columns, annotation, columnIndex)
        }
    }
}
