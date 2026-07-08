package org.ivdnt.galahad.util

import com.fasterxml.aalto.stax.InputFactoryImpl
import com.fasterxml.aalto.stax.OutputFactoryImpl
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.Transformer
import javax.xml.transform.TransformerFactory
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

abstract class XmlUtil(val xml: Document) {
    protected fun Node.getOrCreateChild(childTag: String, prepend: Boolean = false): Element {
        val child: Node? = this.childOrNull(childTag)
        if (child != null) return child as Element
        // No node found
        return createChild(childTag, prepend)
    }

    protected fun Node.createChild(name: String, prepend: Boolean = false): Element =
        xml.createElement(name).also {
            if (prepend) {
                insertBefore(it, firstChild)
            } else {
                appendChild(it)
            }
        }

    /**
     * Add a tag to [this] with [name], [text], and optional [attrValue] Defaults to writing
     * attribute @type.
     */
    protected fun Node.createChild(name: String, text: String, attrValue: String): Element =
        createChild(name, mapOf("type" to attrValue), text)

    protected fun Node.createChild(name: String, text: String): Element =
        createChild(name, mapOf(), text)

    protected fun Node.createChild(
        name: String,
        attr: Pair<String, String>,
        text: String = "",
    ): Element = createChild(name, mapOf(attr), text)

    protected fun Node.createChild(
        name: String,
        attrs: Map<String, String>,
        text: String = "",
    ): Element =
        // Create empty tag
        xml.createElement(name)
            .apply {
                // Set attributes
                attrs.forEach { (key, value) -> setAttribute(key, value) }
                // Set text content
                textContent = text
            }
            .also {
                // Add to parent
                appendChild(it)
            }

    companion object {
        val builder: DocumentBuilder =
            DocumentBuilderFactory.newInstance()
                .apply {
                    isIgnoringComments = true
                    isNamespaceAware = true
                    setFeature(
                        "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                        false,
                    )
                }
                .newDocumentBuilder()

        val transformer: Transformer =
            TransformerFactory.newInstance().newTransformer().apply {
                // Pretty print
                setOutputProperty(OutputKeys.INDENT, "yes")
                // For some reason needed to print the root on a new line instead of on the same
                // line as the doctype.
                setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "yes")
            }

        val inputFactory: InputFactoryImpl =
            InputFactoryImpl().apply {
                configureForSpeed()
                setProperty(InputFactoryImpl.SUPPORT_DTD, false)
            }
        val outputFactory: OutputFactoryImpl = OutputFactoryImpl().apply { configureForSpeed() }
    }
}
