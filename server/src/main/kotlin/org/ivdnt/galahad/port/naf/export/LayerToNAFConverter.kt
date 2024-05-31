package org.ivdnt.galahad.port.naf.export

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.LayerConverter
import org.ivdnt.galahad.port.LayerTransformer
import org.ivdnt.galahad.util.getXmlBuilder
import java.io.OutputStream
import java.io.StringWriter
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class LayerToNAFConverter (
    transformMetadata: DocumentTransformMetadata
) : LayerConverter, LayerTransformer(transformMetadata) {

    override val format: DocumentFormat
        get() = DocumentFormat.Naf
    override fun convert( outputStream: OutputStream) {

        val plainText = transformMetadata.plainText
        val layer = result

        val xmlDoc = getXmlBuilder().newDocument()
        val root = xmlDoc.createElement("NAF")
        xmlDoc.appendChild(root)

        val xPlainText = xmlDoc.createElement("raw")
        root.appendChild(xPlainText)
        val cdata = xmlDoc.createCDATASection("")
        xPlainText.appendChild(cdata)
        cdata.textContent = plainText

        val text = xmlDoc.createElement("text")
        root.appendChild(text)

        layer
            .wordForms
            .forEachIndexed { _, wordForm ->
                //wordForm.id = "w$index"

                val wf = xmlDoc.createElement("wf")
                text.appendChild(wf)
                wf.setAttribute("offset", wordForm.offset.toString())
                wf.setAttribute("length", wordForm.length.toString())
                wf.setAttribute("id", wordForm.id)
                wf.textContent = wordForm.literal
            }

        val terms = xmlDoc.createElement("terms")
        root.appendChild(terms)

        layer
            .terms
            .forEachIndexed { index, term ->
                val xterm = xmlDoc.createElement("term")
                terms.appendChild(xterm)
                xterm.setAttribute("id", "t$index")
                xterm.setAttribute("lemma", term.lemma)
                xterm.setAttribute("pos", term.pos)

                val xspan = xmlDoc.createElement("span")
                xterm.appendChild( xspan )

                term.targets.forEach {target ->
                    val xtarget = xmlDoc.createElement("target")
                    xtarget.setAttribute("id", target.id )
                    xspan.appendChild( xtarget )
                }
            }

        // Convert to string
        val writer = StringWriter()
        val transformer = TransformerFactory.newInstance().newTransformer()

        // Pretty print breaks the <raw> because it adds extra whitespace inside
//            transformer.setOutputProperty( OutputKeys.INDENT, "yes")
//            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2")
        transformer.transform(DOMSource(xmlDoc), StreamResult(writer))
        val xmlString = writer.buffer.toString()

        return outputStream.write( xmlString.toByteArray() ) // TODO more efficient implementation instead of bulk
    }
}