package org.ivdnt.galahad.port.conllu.export

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.LayerConverter
import org.ivdnt.galahad.port.LayerTransformer
import java.io.OutputStream

/**
 * Export a layer to a CoNLLU file. Uses _ for null values. Splits PoS in head and features.
 */
class LayerToConlluConverter(
    transformMetadata: DocumentTransformMetadata,
) : LayerConverter, LayerTransformer(transformMetadata) {

    override val format: DocumentFormat
        get() = DocumentFormat.Conllu

    override fun convert(outputStream: OutputStream) {
        result.terms.forEachIndexed { index, term ->
            val row = listOf(
                (index + 1).toString(), // index
                term.literals, // form
                term.lemma ?: "_", // lemma
                "_", // upos
                term.pos ?: "_", // xpos
                "_", // feats
                "_", // head
                "_", // deprel
                "_", // deps
                "_", // misc
            )
            outputStream.write("${row.joinToString("\t")}\n".encodeToByteArray())
        }
    }
}