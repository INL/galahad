package org.ivdnt.galahad.port.folia

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.folia.export.FoliaLayerMerger
import org.ivdnt.galahad.port.xml.AnnotatedFile
import java.io.File
import java.io.Reader

class FoliaFile(
    file: File,
) : AnnotatedFile(
    file
) {
    override val format: DocumentFormat = DocumentFormat.Folia
    private var isParsed: Boolean = false
    private var reader: FoliaReader? = null

    override fun merge(transformMetadata: DocumentTransformMetadata): FoliaFile {
        return FoliaLayerMerger(this, transformMetadata).merge()
    }

    private fun parse() {
        reader = FoliaReader(file) { _, _, _ -> }
        reader?.read()
        isParsed = true
    }

    override fun plainTextReader(): Reader {
        if (!isParsed) parse()
        // TODO: make this an efficient implementation
        return reader!!.plainTextBuilder.toString().reader()
    }

    override fun sourceLayer(): Layer {
        if (!isParsed) parse()
        return reader!!.sourceLayer
    }
}