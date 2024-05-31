package org.ivdnt.galahad.port.tei.export

import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.LayerMerger
import org.ivdnt.galahad.port.LayerTransformer
import org.ivdnt.galahad.port.tei.TEIFile
import org.ivdnt.galahad.port.xml.BLFXMLParser
import org.ivdnt.galahad.util.getXmlBuilder
import org.w3c.dom.Document
import org.w3c.dom.Node
import java.io.OutputStream
import kotlin.io.path.createTempDirectory


class TEILayerMerger (
    teiFile: TEIFile,
    transformMetadata: DocumentTransformMetadata
) : LayerMerger<TEIFile>, LayerTransformer( transformMetadata ) {

    private var xmlDoc = getXmlBuilder().newDocument()
    private val sortedWordForms = result.wordForms.sortedBy { it.offset }
    private val wordFormIter = sortedWordForms.listIterator()

    private val deleteList = ArrayList<Node>()
    private var parser: BLFXMLParser

    override fun merge(): TEIFile {
        val result = createTempDirectory("teimerge").toFile().resolve( document.name )
        result.writeText( parser.xmlToString( false ) )
        return TEIFile(result, document.format)
    }

    init {
        parser = BLFXMLParser.forFileWithFormat(transformMetadata.document.format, teiFile.file, OutputStream.nullOutputStream()) {
            node: Node, offset: Int, document: Document ->
            val merger = TEITextMerger(node, offset, document, wordFormIter, deleteList, result, transformMetadata.document.format)
            merger.merge()
        }
        xmlDoc = parser.xmlDocument

        // add headers
        // typically we expect just 1 root node.
        for( i in 0 until parser.rootNodes.length ) {
            TEIMetadata(xmlDoc, parser.rootNodes.item(i), this).write(formatInPlace = true)
        }

        // Delete the marked notes
        deleteList.forEach { if (it.parentNode != null) it.parentNode.removeChild(it) }
    }

}