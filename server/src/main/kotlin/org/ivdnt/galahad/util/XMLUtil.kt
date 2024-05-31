package org.ivdnt.galahad.util

import java.io.OutputStream
import javax.xml.parsers.DocumentBuilder
import javax.xml.parsers.DocumentBuilderFactory

/**
 * Helper class for writing xml to a stream and keeping track of the indents between tags.
 * No validation checks are in place. Adds newlines.
 */
class XMLWriter(private val stream: OutputStream) {
    private var indent = 0 // XML indentation

    /**
     * Write unindented text.
     */
    fun writeNoIndent(str: String) {
        stream.write((str + "\n").toByteArray())
    }

    /**
     * Write indented text between tags.
     */
    fun write(str: String) {
        writeNoIndent("    ".repeat(indent) + str)
    }

    /**
     * Open a new tag. Affects subsequent indenting.
     * @param str an XML tag, including angular brackets.
     */
    fun openTag(str: String) {
        write(str)
        indent++
    }

    /**
     * Close a tag. Affects subsequent indenting.
     * @param str an XML tag, including angular brackets.
     */
    fun closeTag(str: String) {
        indent--
        write(str)
    }
}

/**
 * Get a new XML builder with external DTD loading disabled. Needed for loading some TEIp4 files.
 */
fun getXmlBuilder(): DocumentBuilder {
    val dbf = DocumentBuilderFactory.newInstance()
    dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false)
    return dbf.newDocumentBuilder()
}