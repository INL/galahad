package org.ivdnt.galahad.port.tei.export

import org.ivdnt.galahad.port.LayerTransformer
import org.ivdnt.galahad.port.xml.XMLMetadata
import org.ivdnt.galahad.util.childOrNull
import org.ivdnt.galahad.util.getXmlBuilder
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.xml.sax.InputSource
import java.io.ByteArrayOutputStream
import java.io.OutputStream
import java.io.StringReader
import java.util.*
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class TEIMetadata(
        xmlDoc: Document,
        root: Node,
        layer: LayerTransformer,
) : XMLMetadata(xmlDoc, root, layer) {
    // There could be multiple root nodes in the document.
    // The caller specifies which one.
    fun write(formatInPlace: Boolean = false) {
        // Add namespace to root for Cobalt compatibility
        (root as Element).setAttribute("xmlns","http://www.tei-c.org/ns/1.0")

        val teiHeader = root.getOrCreateChild("teiHeader", true)
        // remove namespace for Cobalt compatibility
        (teiHeader as Element).removeAttribute("xmlns")
        (root.childOrNull("text") as Element?)?.removeAttribute("xmlns")

        val fileDesc = teiHeader.getOrCreateChild("fileDesc")
        val sourceDesc = fileDesc.getOrCreateChild("sourceDesc")
        val listBibl = xmlDoc.createElement("listBibl")
        listBibl.setAttribute("xml:id", "galahadMetadata")
        // toPlainXMLText looks for the first listBibl in the doc
        // So we want to be first
        sourceDesc.insertBefore(listBibl, sourceDesc.firstChild)
        val bibl = listBibl.getOrCreateChild("bibl")

        addGalahadMetadata(bibl)
        if (formatInPlace) {
            val stream = ByteArrayOutputStream()
            toPlainXMLText(stream)
            stream.close()
            val valueDoc: Document = getXmlBuilder().parse(
                InputSource(StringReader(stream.toString()))
            )
            val valueElement: Node = xmlDoc.importNode(valueDoc.documentElement, true)
            sourceDesc.replaceChild(valueElement, listBibl)
        }
    }

    fun toPlainXMLText(stream: OutputStream) {
        // For now, we only write the listBibl to stream.
        // Future metadata implementation might need something different?
        // In that case, perhaps use a `nodeName: string` parameter and a recursive node grabber.
        val teiHeader = root.getOrCreateChild("teiHeader")
        val fileDesc = teiHeader.getOrCreateChild("fileDesc")
        val sourceDesc = fileDesc.getOrCreateChild("sourceDesc")
        val listBibl = sourceDesc.getOrCreateChild("listBibl")

        val tf: Transformer = TransformerFactory.newInstance().newTransformer()
        tf.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes")
        tf.setOutputProperty(OutputKeys.INDENT, "yes")
        tf.transform(DOMSource(listBibl), StreamResult(stream))
    }

    private fun addGalahadMetadata(root: Node) {
        addKeyValuesTo(root, "model", layer.tagger.id )
        addKeyValuesTo(root, "tagset", layer.tagger.tagset ?: "undefined" )
        val metadata = layer.transformMetadata.corpus.metadata.expensiveGet()
        addKeyValuesTo(root, "corpus_name", metadata.name)
        addKeyValuesTo(root, "corpus_uuid", "${metadata.uuid}" )
        addKeyValuesTo(root, "date", layer.dateFormat.format( Date() ) )
        addKeyValuesTo(root, "user_id", layer.transformMetadata.user.id )
        addKeyValuesTo(root, "pid",  getPid())
        addKeyValuesTo(root, "title", getTitle())
    }

    private fun getPid(): String {
        val pidAttr = (root as Element).getAttribute("xml:id")
        return if (pidAttr.isNotEmpty()) pidAttr
        else layer.transformMetadata.document.uuid.toString()
    }

    /**
     * Return the title of the document as described in titleStmt,
     * or the filename without extension if the former is missing.
     */
    private fun getTitle(): String {
        val titleTag = root.childOrNull("teiHeader")
            ?.childOrNull("fileDesc")
            ?.childOrNull("titleStmt")
            ?.childOrNull("title")
        return if (titleTag != null) titleTag.textContent
        else layer.transformMetadata.document.getUploadedRawFile().nameWithoutExtension
    }


    private fun addKeyValuesTo(node: Node, key: String, value: String ) {
        addKeyValuesTo( node, key, listOf(value) )
    }

    private fun addKeyValuesTo(node: Node, key:String, values:List<String> ) {
        val xgrp = xmlDoc.createElement("interpGrp")
        node.appendChild(xgrp)
        xgrp.setAttribute("type", key)
        for (value in values) {
            val xvalue = xmlDoc.createElement("interp")
            xgrp.appendChild(xvalue)
            xvalue.textContent = value
        }
    }

}