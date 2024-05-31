package org.ivdnt.galahad.data.document

import org.ivdnt.galahad.util.getXmlBuilder
import java.io.File
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * Static helper class to induce the format of a document based on its file extension and content (e.g. root XML node).
 */
object FormatInducer {

    fun determineFormat(file: File): DocumentFormat {
        val format = when (file.extension) {
            "tsv" -> DocumentFormat.Tsv
            "conllu" -> DocumentFormat.Conllu
            "xml" -> determineXmlFormat(file)
            "txt" -> DocumentFormat.Txt
            "naf" -> DocumentFormat.Naf
            else -> DocumentFormat.Unknown
        }
        println("Induced format $format for file ${file.name}")
        return format
    }

    /**
     * Differentiate based on the root node.
     */
    private fun determineXmlFormat(file: File): DocumentFormat {
        val xmlDoc: org.w3c.dom.Document = getXmlBuilder().parse(file)
        return when (xmlDoc.documentElement.tagName) {
            "FoLiA" -> DocumentFormat.Folia
            "TEI.2", "teiCorpus.2" -> DocumentFormat.TeiP4Legacy
            "TEI", "teiCorpus" -> determineTeiP5Format(xmlDoc)
            "NAF" -> DocumentFormat.Naf
            else -> DocumentFormat.Unknown
        }
    }

    /** Differentiate between TeiP5 and TeiP5Legacy by the presence of pos as an XML attribute.
     * - 1 or more pos are present, it's TeiP5
     * - if no pos are present, but at least one type is present, it's TeiP5Legacy
     * - if no pos or type are present, it's unannotated and we default to TeiP5
     */
    private fun determineTeiP5Format(xmlDoc: org.w3c.dom.Document): DocumentFormat {
        val xPath: XPath = XPathFactory.newInstance().newXPath()
        val numPos = xPath.compile("count(.//w[@pos])").evaluate(xmlDoc, XPathConstants.NUMBER) as Double
        val numTypes = xPath.compile("count(.//w[@type])").evaluate(xmlDoc, XPathConstants.NUMBER) as Double
        if (numTypes == 0.0 || numPos > 0) return DocumentFormat.TeiP5
        return DocumentFormat.TeiP5Legacy // No pos but at least one type: assume legacy mode
    }
}
