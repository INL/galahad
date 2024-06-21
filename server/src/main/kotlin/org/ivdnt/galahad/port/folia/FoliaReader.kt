package org.ivdnt.galahad.port.folia

import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.data.layer.WordForm
import org.ivdnt.galahad.port.folia.export.deepcopy
import org.ivdnt.galahad.port.xml.reparseText
import org.ivdnt.galahad.port.xml.tagName
import org.ivdnt.galahad.util.containedIn
import org.ivdnt.galahad.util.getXmlBuilder
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.io.File

class FoliaReader(
    val file: File,
    val nodeHandler: (node: Node, offset: Int, document: Document) -> Unit,
) {
    val xmlDoc: Document = getXmlBuilder().parse(file)
    val plainTextBuilder: StringBuilder = StringBuilder()
    val sourceLayer = Layer(SOURCE_LAYER_NAME)

    // Used to add spaces between words, but not between punctuation and words.
    private var previousWasW = false

    fun read() {
        val plaintextBuilder = StringBuilder()
        // Parsable nodes.
        val texts = xmlDoc.getElementsByTagName("text").deepcopy()
        val speeches = xmlDoc.getElementsByTagName("speech").deepcopy()
        texts.addAll(speeches)

        for (child in texts) {
            processText(child)
            plaintextBuilder.append(child.textContent)
        }
    }

    /** Within correction & speech, we only enter certain tags. Outside, we enter everything. */
    private fun shouldParse(node: Node): Boolean {
        // Don't parse notes and morphology.
        if (node.tagName() == "note" || node.tagName() == "morphology") return false

        val inCorrection: Boolean = node.parentNode?.tagName() == "correction"
        val parseableCorrections: Boolean = node.tagName() == "new" || node.tagName() == "current"

        val inSpeech: Boolean = node.parentNode?.tagName() == "speech"
        val parsableSpeech: Boolean = node.tagName() == "utt" || node.tagName() == "s"

        return if (inCorrection) parseableCorrections
        else if (inSpeech) return parsableSpeech
        else true
    }

    private fun processText(node: Node) {

        fun recurse() {
            val oldChildNodes = node.childNodes.deepcopy()
            for (child in oldChildNodes) {
                if (shouldParse(child)) processText(child)
            }
        }

        if (node.nodeType == Node.ELEMENT_NODE) {
            val elem = node as Element
            when (elem.tagName) {
                "w" -> {
                    processW(elem)
                    previousWasW = true
                }
                // Extract plaintext that isn't in a <w>.
                "t" -> {
                    if (!elem.containedIn("w")) {
                        // Extract text first, before nodeHandler changes anything.
                        val text = elem.textContent.addNonFloatingSpace()
                        nodeHandler(node, plainTextBuilder.length, xmlDoc)
                        // Trim to correct for cases like: <t>abc\n       </t>
                        plainTextBuilder.append(text)
                    }
                }

                "s" -> {
                    nonFloatingNL()
                    recurse()
                    nonFloatingNL()
                    previousWasW = false
                }
                "p" -> {
                    nonFloatingDoubleNL()
                    recurse()
                    nonFloatingDoubleNL()
                    previousWasW = false
                }

                else -> {
                    recurse()
                }
            }
        }
    }

    /** Adds a newline if the last character exists and is a non newline.*/
    private fun nonFloatingNL() {
        if (plainTextBuilder.isNotEmpty() && !plainTextBuilder.endsWith("\n")) {
            plainTextBuilder.append("\n")
        }
    }

    private fun nonFloatingDoubleNL() {
        nonFloatingNL()
        if (plainTextBuilder.isNotEmpty() && !plainTextBuilder.endsWith("\n\n")) {
            plainTextBuilder.append("\n")
        }
    }

    private fun String.addNonFloatingSpace(): String {
        return if (this.isEmpty()) this
        else {
            val text = reparseText(this).trim()
            // Sometimes, <t>'s follow each other up without e.g. an opening or closing <p>.
            // So we need to add spacing ourselves.
            "$text "
        }
    }

    private fun processW(w: Element) {
        var literal = ""
        val id = w.getAttribute("xml:id")
        var lem: String? = null
        var pos: String? = null

        fun recurse(w: Node) {
            for (i in 0 until w.childNodes.length) {
                // Recurse
                val childNode = w.childNodes.item(i)
                if (shouldParse(childNode)) recurse(childNode)

                if (childNode.nodeType == Node.ELEMENT_NODE) {
                    val childElem = childNode as Element
                    when (childElem.tagName) {
                        // Trim to correct for cases like: <t>abc\n       </t>
                        "t" -> literal = childElem.textContent.trim()
                        "lemma" -> lem = childElem.getAttribute("class")
                        "pos" -> pos = childElem.getAttribute("class")
                    }
                }
            }
        }
        // Extract the information
        recurse(w)

        if (previousWasW && pos?.startsWith("LET") != true) {
            plainTextBuilder.append(" ")
        }
        // We need the " " for the correct offset to give to nodeHandler.
        nodeHandler(w as Node, plainTextBuilder.length, xmlDoc)

        val wordForm = WordForm(literal, plainTextBuilder.length, literal.length, id)
        sourceLayer.wordForms.add(wordForm)
        val term = Term(lem, pos, mutableListOf(wordForm))
        sourceLayer.terms.add(term)

        plainTextBuilder.append(literal)
    }
}