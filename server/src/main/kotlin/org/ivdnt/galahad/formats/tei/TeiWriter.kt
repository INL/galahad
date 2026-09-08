package org.ivdnt.galahad.formats.tei

import java.io.OutputStream
import javax.xml.XMLConstants
import org.codehaus.stax2.XMLStreamWriter2
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.annotations.TermSpan
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerWriter
import org.ivdnt.galahad.formats.reader.PrettyXMLWriter
import org.ivdnt.galahad.layer.LayerAnnotations.Companion.contains
import org.ivdnt.galahad.util.XmlUtil.Companion.outputFactory

class TeiWriter(export: DocumentExport) : LayerWriter(export) {
    override fun convert(out: OutputStream) {
        val writer = PrettyXMLWriter(outputFactory.createXMLStreamWriter(out) as XMLStreamWriter2)

        writer.writeStartDocument("UTF-8", "1.0")
        writer.writeStartElement("TEI")
        writer.writeNamespace("", "http://www.tei-c.org/ns/1.0")
        writer.writeNamespace("xml", "http://www.w3.org/XML/1998/namespace")
        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", export.layer.id)
        TeiMetadataWriter(writer, export).write()

        export.layer.documents.forEach { doc ->
            writer.writeStartElement("text")
            writer.writeAttribute(XMLConstants.XML_NS_URI, "id", doc.id)

            writer.writeStartElement("body")

            doc.paragraphs.forEach { paragraph ->
                writer.writeStartElement("p")
                writer.writeAttribute(XMLConstants.XML_NS_URI, "id", paragraph.id)

                paragraph.sentences.forEach { sentence ->
                    writer.writeStartElement("s")
                    writer.writeAttribute(XMLConstants.XML_NS_URI, "id", sentence.id)
                    val ners = sentence.spans?.get(Annotation.NER)
                    sentence.terms.forEachIndexed { termI, t ->
                        // if the term is in a span, we output:
                        // <name type="ORG">
                        //     <w>...</w>
                        // </name>
                        ners
                            ?.firstOrNull<TermSpan> { termI == it.indices.first() }
                            ?.let {
                                writer.writeStartElement("name")
                                writer.writeAttribute("type", it.value)
                            }

                        val tag =
                            if (t.pos in punct && !t.token.contains(alphaNumeric)) "pc" else "w"
                        writer.writeStartElement(tag)
                        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", t.id)
                        if (tag == "w") {
                            t.lemma?.let { writer.writeAttribute("lemma", it) }
                        }
                        t.pos?.let { writer.writeAttribute("pos", it) }
                        t.upos?.let { Term.features(it)?.let { writer.writeAttribute("msd", it) } }
                        if (t.spaceAfter == false) writer.writeAttribute("join", "right")

                        if (t.group != null) {
                            writer.writeCharacters(t.token, true)
                            writer.writeNewLine()
                            writer.writeEmptyElement("join", mapOf("n" to t.group))
                        } else {
                            writer.writeCharacters(t.token)
                        }
                        writer.writeEndElement()
                        if (ners?.any { termI == it.indices.last() } == true) {
                            writer.writeEndElement() // name
                        }
                    }
                    // TODO at some point to be replaced by @depN and @depR
                    // at the end of a sentence, write deprels. Example:
                    // <linkGrp targFunc="head argument" type="UD-SYN">
                    //     <link target="#d1.p1.s1.w1 #d1.p1.s1.w2" ana="ud-syn:det"/>
                    // </linkGrp>
                    if (
                        Annotation.DEPREL in export.document.metadata.annotations &&
                            sentence.terms.any { it.deprel != null }
                    ) {
                        writer.writeStartElement("linkGrp")
                        writer.writeAttribute("targFunc", "head argument")
                        writer.writeAttribute("type", "UD-SYN")
                        sentence.terms.forEach { t ->
                            if (t.deprel != null && t.deprel?.lowercase() != "root") {
                                writer.writeStartElement("link")
                                writer.writeAttribute(
                                    "target",
                                    "#${sentence.terms[t.head!!.toInt() - 1].id} #${t.id}",
                                )
                                writer.writeAttribute("ana", "ud-syn:${t.deprel}")
                                writer.writeEndElement(false) // link
                            }
                        }
                        writer.writeEndElement() // linkGrp
                    }
                    writer.writeEndElement() // s
                }
                writer.writeEndElement() // p
            }
            writer.writeEndElement() // body
            writer.writeEndElement() // text
        }
        writer.writeEndElement() // TEI
        writer.writeEndDocument()
        writer.flush()
        writer.close()
    }

    companion object {
        private val punct = arrayOf("PC", "LET")
        private val alphaNumeric = Regex("""[a-zA-Z0-9]""")
    }
}
