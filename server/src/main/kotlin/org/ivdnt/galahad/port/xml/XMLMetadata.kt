package org.ivdnt.galahad.port.xml

import org.ivdnt.galahad.port.LayerTransformer
import org.ivdnt.galahad.util.childOrNull
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

open class XMLMetadata(
    val xmlDoc: Document,
    val root: Node,
    val layer: LayerTransformer
) {
    protected fun Node.getOrCreateChild(childTag: String, prepend: Boolean = false ): Element {
        val child: Node? = this.childOrNull(childTag)
        if (child != null) return child as Element
        // No node found
        return createChild(childTag, prepend)
    }

    protected fun Node.createChild(name: String, prepend: Boolean = false): Element {
        val newNode = xmlDoc.createElement(name)
        if (prepend && this.childNodes.length > 0) { // TODO checking length might not be necessary
            this.insertBefore(newNode, this.firstChild)
        } else {
            this.appendChild(newNode)
        }
        return newNode
    }

    /**
     * Add a tag to [this] with [name], [textContent], and optional [attrValue] and [targetAttr].
     * Defaults to writing attribute @type.
     */
    protected fun Node.createChild(
        name: String,
        textContent: String,
        attrValue: String,
    ) : Element {
        return this.createChild(name, mapOf("type" to attrValue), textContent)
    }

    protected fun Node.createChild(
        name: String,
        textContent: String,
    ) : Element {
        return this.createChild(name, mapOf(), textContent)
    }

    protected fun Node.createChild(
        name: String,
        attr: Pair<String, String>,
        textContent: String = "",
    ) : Element {
        return this.createChild(name, mapOf(attr), textContent)
    }

    protected fun Node.createChild(
        name: String,
        attrs: Map<String, String>,
        textContent: String = "",
    ) : Element {
        // Create empty tag
        return xmlDoc.createElement(name)
            // Add to parent
            .also { appendChild(it) }
            // Set attributes
            .apply { attrs.forEach { (key, value) -> this.setAttribute(key, value) } }
            // Set text content
            .apply { this.textContent = textContent}
    }
}