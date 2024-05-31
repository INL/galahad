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
    protected fun Node.getOrCreateChild(childTag: String, prepend: Boolean = false ): Node {
        val child: Node? = this.childOrNull(childTag)
        if (child != null) return child
        // No node found
        val newNode = xmlDoc.createElement(childTag)
        if( prepend && this.childNodes.length > 0 ) {
            this.insertBefore(newNode, this.firstChild)
        } else {
            this.appendChild(newNode)
        }
        return newNode
    }
}