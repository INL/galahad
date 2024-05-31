package org.ivdnt.galahad.port.conllu

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.conllu.export.ConlluLayerMerger
import org.ivdnt.galahad.port.tsv.TSVFile
import java.io.File

/**
 * CoNLL-U is basically TSV, so use it as a basis.
 */
class ConlluFile(file: File) : TSVFile(file) {
    override val format = DocumentFormat.Conllu
    override var literalIndex: Int? = 1
    override var lemmaIndex: Int? = 2
    override var posIndex: Int? = 4 // XPOS

    // CoNLL-U has a fixed order of columns.
    override fun getColumnIndices(headers: List<String>, errors: MutableList<String>) {}

    /**
     * For CoNLL-U we need to manually combine the head pos with its features.
     */
    override fun getPos(values: List<String>): String? {
        return getColumn(posIndex, values)
    }

    override fun getColumn(index: Int?, values: List<String>): String? {
        val value = super.getColumn(index, values)
        return if (value == "_") null else value
    }

    override fun merge(transformMetadata: DocumentTransformMetadata): ConlluFile {
        // Sets header indices needed to merge.
        parse()
        return ConlluLayerMerger(this, transformMetadata).merge()
    }
}