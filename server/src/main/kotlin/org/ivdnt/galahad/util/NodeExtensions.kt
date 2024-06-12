package org.ivdnt.galahad.util

import org.ivdnt.galahad.port.xml.tagName
import org.w3c.dom.Element
import org.w3c.dom.Node

/** Whether this node is contained in a node with name [tagName]*/
fun Node.containedIn(tagName: String): Boolean {
    if (this.parentNode == null)
        return false
    if (this.parentNode?.tagName() == tagName)
        return true
    // Recursion
    return this.parentNode.containedIn(tagName)
}

/** Insert [newChild] as a child of this node, placed after [refChild]. */
fun Node.insertAfter(newChild: Node, refChild: Node) {
    if (refChild.nextSibling != null) {
        this.insertBefore(newChild, refChild.nextSibling)
    } else {
        this.appendChild(newChild)
    }
}

/** Insert [newChild] as the first child of this node. */
fun Node.insertFirst(newChild: Node) {
    this.insertBefore(newChild, this.firstChild)
}

/** Returns the next sibling of the node that is not text. */
fun Node.nextNonTextSibling(): Node? {
    var next = this.nextSibling
    while (next != null && next.nodeType == Node.TEXT_NODE) {
        next = next.nextSibling
    }
    return next
}

/** Looks for the first child node, 1 deep, or null. */
fun Node.childOrNull(childTag: String): Node? {
    for (i in 0 until this.childNodes.length) {
        if (this.childNodes.item(i).nodeType == Node.ELEMENT_NODE) {
            if ((this.childNodes.item(i) as Element).tagName == childTag) {
                return this.childNodes.item(i)
            }
        }
    }
    return null
}

/** Looks for the first child node, 1 deep, or null. */
fun Element.childOrNull(childTag: String): Element? {
    return (this as Node).childOrNull(childTag) as Element?
}
