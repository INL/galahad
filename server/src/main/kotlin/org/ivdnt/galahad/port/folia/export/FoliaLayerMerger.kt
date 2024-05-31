package org.ivdnt.galahad.port.folia.export

import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.LayerMerger
import org.ivdnt.galahad.port.LayerTransformer
import org.ivdnt.galahad.port.folia.FoliaFile
import org.ivdnt.galahad.port.folia.FoliaMetadata
import org.ivdnt.galahad.port.folia.FoliaReader
import org.ivdnt.galahad.port.xml.BLFXMLParser
import org.w3c.dom.Document
import org.w3c.dom.Node
import kotlin.io.path.createTempDirectory

class FoliaLayerMerger(
    foliaFile: FoliaFile,
    transformMetadata: DocumentTransformMetadata,
) : LayerMerger<FoliaFile>, LayerTransformer(transformMetadata) {

    private val sortedWordForms = result.wordForms.sortedBy { it.offset }
    private val wordFormIter = sortedWordForms.listIterator()
    private val deleteList = ArrayList<Node>()
    private var reader: FoliaReader? = null

    init {
        reader = FoliaReader(foliaFile.file) { node: Node, offset: Int, document: Document ->
            val merger = FoliaTextMerger(node, offset, document, wordFormIter, deleteList, transformMetadata.layer)
            merger.merge()
        }
        reader!!.read()

        // add headers
        // typically we expect just 1 root node.
        FoliaMetadata(reader!!.xmlDoc, reader!!.xmlDoc.firstChild, this).write()

        // Delete the marked notes
        deleteList.forEach { if (it.parentNode != null) it.parentNode.removeChild(it) }
    }

    override fun merge(): FoliaFile {
        val result = createTempDirectory("foliamerge").toFile().resolve(document.name)
        result.writeText(BLFXMLParser.xmlToString(false, reader!!.xmlDoc))
        return FoliaFile(result)
    }
}