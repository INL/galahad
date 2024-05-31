package org.ivdnt.galahad.port.tei.export

import org.ivdnt.galahad.app.report.Report
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.port.folia.export.deepcopy
import org.ivdnt.galahad.port.xml.getPlainTextContent
import org.ivdnt.galahad.util.insertFirst
import org.ivdnt.galahad.util.containedIn
import org.ivdnt.galahad.util.matchesUpTo
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import java.util.*
import kotlin.collections.ArrayList
import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.WordForm

fun HashSet<String>.contains(s: String?, ignoreCase: Boolean = false): Boolean {
    return any { it.equals(s, ignoreCase) }
}

fun Node.wrapChildrenIn(newNode: Node) {
    val children = this.childNodes.deepcopy()
    for (child in children) {
        newNode.appendChild(child)
    }
    // this.childnodes.length is now zero
    newNode.parentNode.insertBefore(this, newNode)
    this.appendChild(newNode)
}

open class TEITextMerger(
    var node: Node,
    var offset: Int,
    var document: Document,
    val wordFormIter: ListIterator<WordForm>,
    private val deleteList: ArrayList<Node>,
    val layer: Layer,
    val format: DocumentFormat,
) {
    var newWTag: Element? = null

    open fun merge() {
        if (node.nodeType == Node.TEXT_NODE) {
            handeTextNode()
        } else if (node.nodeType == Node.ELEMENT_NODE) {
            handleElementNode()
        }
    }

    private fun handeTextNode() {
        // Check whether there are wordforms to add
        val wordFromsToAdd = getWordFormsToAdd().sortedBy { it.offset }
        if (wordFromsToAdd.isNotEmpty()) {
            // add the wordforms, mark this node for deletion
            markForDeletion(node)
            var previousEndOffset = offset
            for (wf in wordFromsToAdd) {
                if (wf.offset < offset) {
                    previousEndOffset = updateEndOffset(wf, previousEndOffset)
                    continue // It has been merged with a previous <w>.
                }
                addWordForm(previousEndOffset, wf)
                previousEndOffset = wf.endOffset
            }
            addTrailingChars(wordFromsToAdd)
        }
    }

    protected fun markForDeletion(n: Node): Boolean {
        // If not contained in w, delete it.
        val shouldDelete = !n.containedIn("w")
        if (shouldDelete) deleteList.add(n)
        return shouldDelete
    }

    private fun addTrailingChars(wordFromsToAdd: List<WordForm>) {
        var startIndex: Int = wordFromsToAdd.last().endOffset - offset
        if (startIndex > node.textContent.length) {
            startIndex = node.textContent.length
        }
        if (node.nodeType == Node.TEXT_NODE) {
            val trailingText = node.textContent.substring(startIndex, node.textContent.length)
            if (trailingText.isNotEmpty()) {
                val trailingChars = document.createTextNode(trailingText)
                node.parentNode.insertBefore(trailingChars, node)
            }
        }
    }

    private fun updateEndOffset(wf: WordForm, previousEndOffset: Int): Int {
        val words = node.textContent.split(Regex("""\s+"""))
        if (words[0].isNotEmpty()) {
            var i = 0
            var startIndex: Int
            do {
                startIndex = wf.literal.lastIndexOf(words[0].substring(0, words[0].length - i))
                i++
            } while (startIndex == -1)
            return previousEndOffset + wf.literal.length - startIndex
        }
        return previousEndOffset
    }

    protected open fun addWordForm(previousEndOffset: Int, wf: WordForm) {
        // add leading characters
        val leadingCharsText = node.textContent.substring(previousEndOffset - offset, wf.offset - offset)
        if (leadingCharsText.isNotEmpty()) {
            val leadingCharsNode = document.createTextNode(leadingCharsText)
            node.parentNode.insertBefore(leadingCharsNode, node)
        }

        // add <w> tag or <pc> tag, depending on pos-tag
        newWTag = createWTag(wf)
        var endOffset: Int = wf.endOffset - offset
        if (endOffset > node.textContent.length) {
            endOffset = node.textContent.length
        }

        // If our current text matches only partially, we will have to merge nodes.
        val baseText = node.textContent.substring(wf.offset - offset, endOffset)
        if (wf.literal == baseText) {
            val newTextNode: Node = document.createTextNode(wf.literal)
            node.parentNode.insertBefore(newWTag, node)
            newWTag!!.insertFirst(newTextNode)
        } else {
            mergeTextNodes(baseText, newWTag!!, wf)
        }
    }

    private fun mergeTextNodes(baseText: String, wTag: Element, wf: WordForm) {
        // Create a newTextNode, because node is in deleteList.
        val newTextNode: Node = document.createTextNode(baseText)
        node.parentNode.replaceChild(wTag, node)
        // node is now dangling, so give it a textable parent.
        val tmp = document.createElement("w")
        tmp.appendChild(node)
        // Now it is safe to overwrite node.
        node = newTextNode
        wTag.appendChild(node)
        // Start merging the <w> with what comes next.
        mergeWTagWithSiblings(wTag, wf)
    }

    private fun mergeWTagWithSiblings(wTag: Element, wf: WordForm) {
        var wTag: Element = wTag
        do {
            // Go up in the tree until we have a nextSibling.
            while (wTag.nextSibling == null) {
                //wTag.wrapChildrenIn(wTag.parentNode)
                wTag = moveWTagUp(wTag)
            }
            // Text contents.
            val sibText = wTag.nextSibling.getPlainTextContent()
            val plainText = wTag.getPlainTextContent()
            var wText = ""
            for (i in plainText.length-1 downTo 0) {
                val tmp = plainText.substring(i)
                if (!wf.literal.contains(tmp)) break
                wText = tmp
            }

            val stillNeeded = wf.literal.substring(wText.length)
            // Determine up to where the sibText matches stillNeeded.
            val matchingIndex = sibText.matchesUpTo(stillNeeded)
            // Append the part that matches
            if (sibText.length == matchingIndex) wTag.appendChild(wTag.nextSibling)
            else {
                val sibClone = wTag.nextSibling.cloneNode(true)
                val sibTextToMatch = sibText.substring(0, matchingIndex)
                val textToMatch = wText + sibTextToMatch
                while (wText + sibClone.getPlainTextContent() != textToMatch) {
                    // get last element
                    var lastChild = sibClone
                    while (lastChild.lastChild != null) {
                        lastChild = lastChild.lastChild
                    }
                    // cut or delete
                    if (lastChild.textContent.isNotEmpty()) {
                        val matchesUpTo: Int = lastChild.textContent.matchesUpTo(sibTextToMatch)
                        lastChild.textContent = lastChild.textContent.substring(0, matchesUpTo)
                    } else {
                        lastChild.parentNode.removeChild(lastChild)
                    }
                }
                wTag.appendChild(sibClone)
            }
            // Continue while the literal is still only partially found
        } while (!wTag.textContent.contains(wf.literal))
    }

    protected open fun moveWTagUp(wTag: Element): Element {
        // TODO this still breaks in some cases.
        wTag.wrapChildrenIn(wTag.parentNode)
        return wTag
    }

    protected open fun createWTag(wf: WordForm): Element {
        val termToAdd = layer.termForWordForm(wf)
        val wTag = if (layer.tagset.punctuationTags.contains(termToAdd.pos)) {
            val n = document.createElement("pc")
            n
        } else {
            val n = document.createElement("w")
            n.setAttribute("lemma", termToAdd.lemma)
            n
        }
        // Both <w> and <pc> have a pos.
        wTag.setAttribute(posType(), termToAdd.pos)
        return wTag
    }

    private fun posType(): String {
        // For now always write pos to the @pos attribute.
        // Even for legacy formats, because we want to update to TEIp5.
        return "pos"
    }

    protected fun getWordFormForOffsetOrNull(): WordForm? {
        while(wordFormIter.hasNext()) {
            val wf = wordFormIter.next()
            if (wf.offset == offset) {
                return wf
            } else if (wf.offset > offset) {
                // overstepped
                wordFormIter.previous()
                break
            }
        }
        return null
    }

    protected open fun handleElementNode() {
        val element = node as Element
        if (element.tagName == "w" || element.tagName == "pc") {
            var wordFormToAdd: WordForm? = getWordFormForOffsetOrNull()
            if (wordFormToAdd != null) {
                // remove all whitespace within a <w>-tag (although this rarely occurs anyway).
                val sourceLiteral = node.getPlainTextContent().replace(Regex("""\s"""), "")
                if (wordFormToAdd.literal == sourceLiteral) {
                    // This is a simple case since the tokenization matches
                    mergeWTag(wordFormToAdd, element)
                } else {
                    // Tokenization mismatch, report it
                    Report.spottedIncompatibleTokenization(
                        wordFormToAdd, WordForm(
                            node.textContent, offset, node.textContent.length,
                            node.attributes?.getNamedItem("xml:id")?.textContent ?: ""
                        )
                    )

                    // Best effort to fix it
                    if (wordFormToAdd.length > node.getPlainTextContent().length) {
                        // Add the term to this node,
                        // The excess length is ignored, which might result in the following token missing it's match
                        mergeWTag(wordFormToAdd, element)
                    } else { // wordFormToAdd.length < node.textContent.length
                        // This is a tricky case, we might want to split up the node
                        // Will leave this case for now
                        Report.tokenMissingAnnotation(node.textContent, offset)
                    }
                }
            } else {
                // This is strange, we would expect an annotation, report it
                Report.tokenMissingAnnotation(node.textContent, offset)
            }
        }
    }

    private fun mergeWTag(wordFormToAdd: WordForm, element: Element) {
        val termToAdd = layer.termForWordForm(wordFormToAdd)
        // <pc> tags do not have a lemma.
        if (element.tagName == "w") {
            element.setAttribute("lemma", termToAdd.lemma)
        }
        element.setAttribute(posType(), termToAdd.pos)
        element.removeAttribute("type") // Update legacy formats to TEI p5
    }

    private fun getWordFormsToAdd(): List<WordForm> {
        val string = node.textContent // TODO or getplaintextcontent() ???
        val textEndOffset = offset + string.length
        val result = mutableListOf<WordForm>()
        // Go to the previous, if there is any.
        // To fix scenarios like: abc<hi/>def ghi, when parsing the text node 'def ghi'.
        if (wordFormIter.hasPrevious()) {
            val prev = wordFormIter.previous()
            if (endOfTermWithinText(prev, textEndOffset)) {
                result.add(prev)
            }
            wordFormIter.next()
        }
        while (wordFormIter.hasNext()) {
            val it = wordFormIter.next()
            // Note: 'start'. The end might be in a following node.
            val startOfTermWithinText = (it.offset >= offset) && (it.offset < textEndOffset)
            if (startOfTermWithinText || endOfTermWithinText(it, textEndOffset)) {
                result.add(it)
            } else {
                wordFormIter.previous()
                break
            }
        }
        return result
    }

    private fun endOfTermWithinText(it: WordForm, textEndOffset: Int): Boolean {
        return (it.endOffset > offset) && (it.endOffset <= textEndOffset)
    }
}