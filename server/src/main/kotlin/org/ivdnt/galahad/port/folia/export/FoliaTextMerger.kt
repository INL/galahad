package org.ivdnt.galahad.port.folia.export

import org.ivdnt.galahad.data.document.DocumentFormat
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.data.layer.WordForm
import org.ivdnt.galahad.port.tei.export.TEITextMerger
import org.ivdnt.galahad.port.xml.reparseText
import org.ivdnt.galahad.port.xml.tagName
import org.ivdnt.galahad.util.insertAfter
import org.ivdnt.galahad.util.insertFirst
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node
import org.w3c.dom.NodeList

fun NodeList.deepcopy(): ArrayList<Node> {
    val copy = ArrayList<Node>()
    for (i in 0 until this.length) {
        copy.add(this.item(i))
    }
    return copy
}

class FoliaTextMerger(
    node: Node, offset: Int, document: Document, wordFormIter: ListIterator<WordForm>,
    deleteList: ArrayList<Node>, layer: Layer
) : TEITextMerger(node, offset, document, wordFormIter, deleteList, layer, DocumentFormat.Folia) {

    override fun merge() {
        if (node.tagName() == "t" || node.tagName()?.startsWith("t-") == true) {
            // We are going to add nodes when exporting, but we don't want to iterate over them
            // So better store references to the current nodes and use them
            val parent = node
            val oldChildNodes = node.childNodes.deepcopy()
            var endsWithSpace = true
            for (child in oldChildNodes) {
                node = child
                val text = reparseText(child.textContent)

                if (node.nodeType == Node.TEXT_NODE) {
                    child.textContent = text
                }
                // TODO reparseText overwrites embedded t-styles

                // never set the offset of more than one space.
                if (endsWithSpace && text.startsWith(" ")) {
                    offset -= 1
                }
                merge()

                // Keep track of the ending space
                if (text.isNotEmpty() && text.endsWith(" "))
                    endsWithSpace = true
                else if (text.isNotEmpty())
                    endsWithSpace = false

                if (child.nodeType == Node.TEXT_NODE)
                    offset += text.length
            }
            // Remove parent and transfer children.
            if(markForDeletion(parent)) {
                var last = parent
                for (i in parent.childNodes.length - 1 downTo 0) {
                    val c = parent.childNodes.item(i)
                    parent.parentNode.insertBefore(c, last)
                    last = c
                }
            }

        } else {
            super.merge()
        }
    }

    override fun createWTag(wf: WordForm): Element {
        val wTag = node.parentNode.cloneNode(false)
        return wTag as Element
    }

    override fun addWordForm(previousEndOffset: Int, wf: WordForm) {
        super.addWordForm(previousEndOffset, wf)
        // For Folia, newWTag is actually a <t> or <t-...> tag.
        var tTag: Node = newWTag!!
        // Make sure tTag points to a <t>. For e.g. a <t-style>, grab the first <t> parent.
        var parent =
            if (tTag.parentNode.tagName() == "t") tTag.parentNode
            else tTag.parentNode.parentNode // First iteration looks at grandparent, because t-style copied itself.
        while (tTag.tagName() != "t") {
            val clone = parent.cloneNode(false)
            tTag.parentNode.replaceChild(clone,tTag)
            clone.insertFirst(tTag)
            // Ready for next iter.
            parent = parent.parentNode
            tTag = clone
        }
        // Create the <w> which will contain the <t>
        val wTag =  document.createElement("w")
        val term = layer.termForWordForm(wf)
        wTag.addTerm(term)
        // Contain it.
        tTag.parentNode.replaceChild(wTag,tTag)
        wTag.insertFirst(tTag)
    }

    override fun handleElementNode() {
        val element = node as Element
        if (element.tagName != "w") return

        val wordFormToAdd = getWordFormForOffsetOrNull() ?: return
        val term = layer.termForWordForm(wordFormToAdd)
        element.addTerm(term)
    }

    override fun moveWTagUp(wTag: Element): Element {
        wTag.parentNode.parentNode.insertAfter(wTag, wTag.parentNode)
        val clone = wTag.parentNode.cloneNode(false)
        wTag.parentNode.replaceChild(clone, wTag)
        clone.appendChild(wTag)
        newWTag = clone as Element
        return clone
    }

    private fun Element.addTerm(term: Term) {
        this.addTermFeature("lemma", term.lemmaOrEmpty)
        this.addTermFeature("pos", term.posOrEmpty, term.posHeadGroupOrEmpty)
    }

    private fun Element.addTermFeature(name: String, value: String, head: String? = null) {
        /* If at some point we want to remove existing annotations layers (pos & lemma) in folia <w> tags
         * uncomment this. For now, multiple annotation layers are okay in the export.
        // Find the child elements of [name] and delete them
        val children = this.childNodes.deepcopy()
        for (child in children) {
            if (child.tagName() == name) {
                this.removeChild(child)
            }
        }*/

        // Create a new child element of [name]
        val child = this.ownerDocument.createElement(name)
        child.setAttribute("class", value)
        // For PoS
        if (head != null) child.setAttribute("head", head)
        // Folia metadata.
        child.setAttribute("processor", layer.name)
        child.setAttribute("set", layer.name)
        this.appendChild(child)
    }
}