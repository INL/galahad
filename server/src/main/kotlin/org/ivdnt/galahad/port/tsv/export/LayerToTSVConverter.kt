package org.ivdnt.galahad.port.tsv.export

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.LayerConverter
import org.ivdnt.galahad.port.LayerTransformer
import java.io.OutputStream

class LayerToTSVConverter(
    transformMetadata: DocumentTransformMetadata,
) : LayerConverter, LayerTransformer(transformMetadata) {

    override val format: DocumentFormat
        get() = DocumentFormat.Tsv

    override fun convert(outputStream: OutputStream) {
        // Header
        outputStream.write("word\tlemma\tpos\n".encodeToByteArray()) // 'word' is the blacklab default
        // Body
        result.terms.forEach {
            // Explicitly non-null.
            outputStream.write("${it.literals}\t${it.lemmaOrEmpty}\t${it.posOrEmpty}\n".encodeToByteArray())
        }
    }
}