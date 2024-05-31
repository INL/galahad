package org.ivdnt.galahad.port.tei

import org.ivdnt.galahad.app.executeAndLogTime
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.document.FormatInducer
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.tei.export.TEILayerMerger
import org.ivdnt.galahad.port.xml.AnnotatedFile
import org.ivdnt.galahad.port.xml.BLFXMLParser
import org.ivdnt.galahad.util.getXmlBuilder
import java.io.File
import java.io.Reader

class TEIFile(
    file: File,
    override val format: DocumentFormat,
) : AnnotatedFile(
    file
) {

    private var _sourceLayer: Layer = Layer.EMPTY
    private var isParsed = false
    private val plainTextFile = File.createTempFile("galahad-${file.name}-plaintext", ".txt")

    constructor(file: File) : this(file, FormatInducer.determineFormat(file))

    fun parse() {
        val xmlDocument = getXmlBuilder().parse(file)
        val xmlParser = BLFXMLParser.forFormat(format, plainTextFile.outputStream(), xmlDocument) { _, _, _ -> }
        _sourceLayer = xmlParser.sourceLayer
        isParsed = true
    }

    override fun plainTextReader(): Reader {
        if (!isParsed) parse()
        return plainTextFile.reader()
    }

    override fun sourceLayer(): Layer {
        if (!isParsed) parse()
        return _sourceLayer
    }

    override fun merge(transformMetadata: DocumentTransformMetadata): TEIFile {
        return executeAndLogTime("Twining TEI file ${file.name}") {
            TEILayerMerger(this, transformMetadata).merge()
        }
    }
}