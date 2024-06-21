package org.ivdnt.galahad.port.naf

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.data.layer.WordForm
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.PlainTextableFile
import org.ivdnt.galahad.port.SourceLayerableFile
import org.ivdnt.galahad.port.xml.XMLFile
import org.ivdnt.galahad.util.getXmlBuilder
import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import java.io.File
import java.io.StringReader
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class NAFFile (
        file: File
) : XMLFile( file ), PlainTextableFile, SourceLayerableFile {

    override val format: DocumentFormat = DocumentFormat.Naf

    val xmlDoc: Document = getXmlBuilder().parse(file)

    private val xPathfactory = XPathFactory.newInstance()
    private val xpath = xPathfactory.newXPath()
    private val expr = xpath.compile("/NAF/raw")
    private val wfExpr = xpath.compile("/NAF/text/wf")
    private val termExpr = xpath.compile("/NAF/terms/term")

    override fun plainTextReader(): StringReader {
            return (expr.evaluate(xmlDoc, XPathConstants.NODE ) as Node).textContent.reader()
    }

    override fun sourceLayer(): Layer {
            val sourceLayer = Layer( SOURCE_LAYER_NAME )
            val xwfs = wfExpr.evaluate( xmlDoc, XPathConstants.NODESET ) as NodeList
            for ( i in 0 until xwfs.length) {
                val xwf = xwfs.item( i ) as org.w3c.dom.Element
                sourceLayer.wordForms.add(
                    WordForm(
                        literal = xwf.textContent,
                        id = xwf.getAttribute("id"),
                        offset = xwf.getAttribute("offset").toInt(),
                        length = xwf.getAttribute("length").toInt()
                    )
                )
            }
            val xterms = termExpr.evaluate( xmlDoc, XPathConstants.NODESET ) as NodeList
            for ( i in 0 until xterms.length) {
                val xterm = xterms.item( i ) as org.w3c.dom.Element
                val targets = ArrayList<WordForm>()
                val xtargets = xterm.getElementsByTagName("span").item(0).childNodes // we assume there is exactly one span
                for ( j in 0 until xtargets.length ) {
                    val xtarget = xtargets.item( j )
                    if( xtarget.nodeType == Node.ELEMENT_NODE ) {
                        // TODO: other implementation
                        targets.add( sourceLayer.getWordFormByID( (xtarget as org.w3c.dom.Element).getAttribute("id") ) )
                    }
                }
                sourceLayer.terms.add(
                        Term(
                                lemma = xterm.getAttribute("lemma"),
                                pos = xterm.getAttribute("pos"),
                                targets = targets
                        )
                )
            }
            return sourceLayer
        }

    override fun merge(transformMetadata: DocumentTransformMetadata): NAFFile {
        throw Exception("Not implemented")
    }
}
