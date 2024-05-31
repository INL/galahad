package org.ivdnt.galahad.port.conllu.export

import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.conllu.ConlluFile
import org.ivdnt.galahad.port.tsv.export.TSVLayerMerger

/**
 * Merge a layer with a CoNLLU file. Uses _ for null values. Splits PoS in head and features.
 * Do not call directly. Use [ConlluFile.merge] instead.
 */
internal class ConlluLayerMerger(
    override val sourceFile: ConlluFile, transformMetadata: DocumentTransformMetadata,
) : TSVLayerMerger(sourceFile, transformMetadata) {

    override val hasHeader: Boolean = false

    override fun merge(): ConlluFile {
        sourceFile.parse() // parse the sourceFile if needed.
        parseByLine()
        return ConlluFile(outFile)
    }

    override fun replaceColumns(columns: MutableList<String>, layer: Layer, termIndex: Int) {
        columns[sourceFile.lemmaIndex!!] = layer.terms[termIndex].lemma ?: "_"
        columns[sourceFile.posIndex!!] = layer.terms[termIndex].pos ?: "_"
    }
}