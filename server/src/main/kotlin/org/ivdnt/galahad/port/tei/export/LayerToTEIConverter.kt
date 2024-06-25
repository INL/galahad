package org.ivdnt.galahad.port.tei.export

import org.ivdnt.galahad.app.report.Report
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.data.layer.WordForm
import org.ivdnt.galahad.port.DocumentTransformMetadata
import org.ivdnt.galahad.port.LayerConverter
import org.ivdnt.galahad.port.LayerTransformer
import org.ivdnt.galahad.util.XMLWriter
import org.ivdnt.galahad.util.escapeXML
import org.ivdnt.galahad.util.getXmlBuilder
import java.io.OutputStream

/**
 * This class used to have a proper DOM-based XML writer, but that was too slow for large documents.
 * Therefore, we now use a simple string-based XML writer.
 */
class LayerToTEIConverter(
    transformMetadata: DocumentTransformMetadata,
) : LayerConverter, LayerTransformer(transformMetadata) {

    override val format: DocumentFormat
        get() = DocumentFormat.TeiP5 // what about p4?

    override fun convert(outputStream: OutputStream) {
        val writer = XMLWriter(outputStream)
        writer.writeLine("""<?xml version="1.0" encoding="UTF-8" standalone="no"?>""")
        writer.openTag("""<TEI xmlns="http://www.tei-c.org/ns/1.0" xmlns:tei="http://www.tei-c.org/ns/1.0" xmlns:xs="http://www.w3.org/2001/XMLSchema">""")
        addGalahadMetadata(outputStream)
        writer.openTag("<text>")
        writer.openTag("<body>")
        writer.openTag("<div>")
        writer.openTag("<p>") // No indent and newline because we want to preserve the plain text
        addText(result, document.plaintext, writer)
        writer.closeTag("</p>")
        writer.closeTag("</div>")
        writer.closeTag("</body>")
        writer.closeTag("</text>")
        writer.closeTag("</TEI>")
        outputStream.close()
    }

    private fun addGalahadMetadata(outputStream: OutputStream) {
        val xmlDoc = getXmlBuilder().newDocument()
        val root = xmlDoc.createElement("root")
        xmlDoc.appendChild(root)
        val metadata = TEIMetadata(xmlDoc, root, this, merging = false)
        metadata.toPlainXMLText(outputStream)
    }

    private fun addText(layer: Layer, plaintext: String, writer: XMLWriter) {
        val sortedTerms = layer.terms.iterator() // .sortedBy { it.firstOffset() }.iterator()
        var currentTerm = if (sortedTerms.hasNext()) sortedTerms.next() else null
        val openMultiTerms = mutableSetOf<OpenMultiTerm>()


        var offset = 0
        characterLoop@ while (offset < plaintext.length) { // It is assumed that there are no overlapping terms
            if (currentTerm?.firstOffset == offset) {
                // currentTerm is matched
                // either add it as a single target term
                // or add as a new multi target term
                if (currentTerm.isMultiTarget) {
                    addJoin(currentTerm)
                    val sorted = currentTerm.targets.sortedBy { it.offset }
                    openMultiTerms.add(OpenMultiTerm(sorted))
                    currentTerm = if (sortedTerms.hasNext()) sortedTerms.next() else null
                    // don't continue, but execute the next for loop
                } else {
                    // insert a single target term
                    addSingleTargetTerm(currentTerm, writer)
                    offset += currentTerm.targets[0].literal.length
                    currentTerm = if (sortedTerms.hasNext()) sortedTerms.next() else null
                    continue@characterLoop // we added a term, assuming no overlapping, we can go to the next character
                }
            }

            // check all open multi target terms
            openMultiTerms.removeIf { it.isEmpty }
            for (omt in openMultiTerms) {
                if (omt.current!!.offset == offset) {
                    addWordFromForMulti(omt.current!!, omt.previous, omt.next)
                    offset += omt.current!!.length
                    omt.skip()
                    continue@characterLoop // we added a wordform, assuming no overlapping, we can go to the next character
                }
            }
            // none of the case above happened, so the text is not covered
            // insert plain text
            writer.writeRaw(plaintext[offset].toString())
            offset += 1 // actually we can look at currentTerm etc. and add multiple characters at once
        }
        currentTerm = if (sortedTerms.hasNext()) sortedTerms.next() else null
        if (currentTerm != null) {
            Report.annotationAfterPlaintext(currentTerm.literals, offset)
        }
    }

    private fun addSingleTargetTerm(term: Term, writer: XMLWriter) {
        fun getLiteral(): String {
            if (term.targets.isEmpty() || term.targets.size > 1) {
                // This case should not occur, since term is supposed to have a single target if it has entered this function
                throw Exception("Only single target term are supported")
            } else {
                return term.targets[0].literal.escapeXML()
            }
        }
        if (punctuationTags.contains(term.pos)) {
            // Interpret as pc tag
            writer.writeRaw("<pc xml:id=\"${term.targets[0].id}\">${getLiteral()}</pc>")
        } else {
            // If it is not punctuation, safely assume it can be interpreted as <w>
            val lemma = term.lemmaOrEmpty.escapeXML()
            val pos = term.posOrEmpty.escapeXML()
            writer.writeRaw("<w lemma=\"$lemma\" pos=\"$pos\" xml:id=\"${term.targets[0].id}\">${getLiteral()}</w>")
        }
    }

    private fun addWordFromForMulti(wordForm: WordForm, previous: WordForm?, next: WordForm?) {
        // TODO: implement
        //        val xwf = xmlDoc.createElement("w")
//        textRoot.appendChild( xwf )
//        xwf.setAttribute("xml:id", wordForm.id )
//        if (previous != null) xwf.setAttribute("previous", previous.id)
//        if (next != null) xwf.setAttribute("next", next.id)
//
//        xwf.textContent = wordForm.literal
    }

    private fun addJoin(term: Term) {
        // TODO: implement
//        val xjoin = xmlDoc.createElement("join")
//        textRoot.appendChild(xjoin)
//        xjoin.setAttribute("result", "w")
//        xjoin.setAttribute("scope", "root")
//        xjoin.setAttribute("lemma", term.lemma)
//        xjoin.setAttribute("pos", term.pos)
//        xjoin.setAttribute("target", term.targets.map { it.id }.joinToString(" "))
    }

    private class OpenMultiTerm(
        val wordForms: List<WordForm>,
    ) {
        var index = 0
        val previous get() = wordForms.getOrNull(index - 1)
        val current get() = wordForms.getOrNull(index)
        val next get() = wordForms.getOrNull(index + 1)
        fun skip() {
            index++
        }

        val isEmpty get() = current == null
    }

}