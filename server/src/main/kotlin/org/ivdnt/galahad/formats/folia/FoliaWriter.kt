package org.ivdnt.galahad.formats.folia

import java.io.OutputStream
import javax.xml.XMLConstants
import org.codehaus.stax2.XMLStreamWriter2
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerWriter
import org.ivdnt.galahad.formats.reader.PrettyXMLWriter
import org.ivdnt.galahad.layer.LayerAnnotations.Companion.contains
import org.ivdnt.galahad.util.XmlUtil.Companion.outputFactory

class FoliaWriter(export: DocumentExport) : LayerWriter(export) {
    override fun convert(out: OutputStream) {
        val writer = PrettyXMLWriter(outputFactory.createXMLStreamWriter(out) as XMLStreamWriter2)

        writer.writeStartDocument("UTF-8", "1.0")
        writer.writeStartElement("FoLiA")
        writer.writeNamespace("", "http://ilk.uvt.nl/folia")
        writer.writeNamespace("xml", "http://www.w3.org/XML/1998/namespace")
        writer.writeAttribute("", "generator", "galahad.ivdnt.org")
        writer.writeAttribute("", "version", "2.5.3")
        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", export.layer.id)

        FoliaMetadataWriter(writer, export).write()

        documents.forEach { doc ->
            writer.writeStartElement("text")
            writer.writeAttribute(XMLConstants.XML_NS_URI, "id", doc.id)

            doc.paragraphs.forEach { paragraph ->
                writer.writeStartElement("p")
                writer.writeAttribute(XMLConstants.XML_NS_URI, "id", paragraph.id)

                paragraph.sentences.forEach { sentence ->
                    writer.writeStartElement("s")
                    writer.writeAttribute(XMLConstants.XML_NS_URI, "id", sentence.id)

                    sentence.terms.forEachIndexed { termI, t ->
                        val wClass =
                            if (t.pos == "PC" && !t.token.contains(alphaNumeric)) "PUNCTUATION"
                            else "WORD"

                        writer.writeStartElement("w")
                        writer.writeAttribute(XMLConstants.XML_NS_URI, "id", t.id)
                        writer.writeAttribute("class", wClass)
                        if (t.spaceAfter == false) writer.writeAttribute("space", "no")

                        writer.writeStartElement("t")
                        writer.writeCharacters(t.token)
                        writer.writeEndElement()

                        if (wClass == "WORD" && t.lemma != null) {
                            writer.writeStartElement("lemma")
                            writer.writeAttribute("class", t.lemma)
                            writer.writeEndElement(indent = false)
                        }

                        if (t.pos != null) {
                            writer.writeStartElement("pos")
                            writer.writeAttribute("class", t.pos)
                            writer.writeAttribute("head", t.annotationHead(Annotation.POS))
                            writer.writeEndElement(indent = false)
                        }

                        writer.writeEndElement() // w
                    }

                    sentence.spans?.get(Annotation.NER)?.let { nerSpans ->
                        writer.writeStartElement("entities")
                        nerSpans.forEachIndexed { spanI, span ->
                            writer.writeStartElement("entity")
                            val spanId = "${sentence.id}.e${spanI + 1}"
                            writer.writeAttribute(XMLConstants.XML_NS_URI, "id", spanId)
                            writer.writeAttribute("class", span.value)
                            span.indices.forEach { termI ->
                                val term = sentence.terms[termI]
                                writer.writeStartElement("wref")
                                writer.writeAttribute("id", term.id)
                                writer.writeAttribute("t", term.token)
                                writer.writeEndElement(indent = false)
                            }
                            writer.writeEndElement() // entity
                        }
                        writer.writeEndElement() // entities
                    }

                    if (Annotation.DEPREL in export.document.metadata.annotations) {
                        writer.writeStartElement("dependencies")
                        sentence.terms.forEach { t ->
                            if (t.deprel?.lowercase() != "root") {
                                writer.writeStartElement("dependency")
                                writer.writeAttribute("class", t.deprel)
                                writer.writeStartElement("dep")

                                writer.writeStartElement("wref")
                                writer.writeAttribute("id", t.id)
                                writer.writeAttribute("t", t.token)
                                writer.writeEndElement(false) // wref
                                writer.writeEndElement() // dep

                                val head = sentence.terms[t.head!!.toInt() - 1]
                                writer.writeStartElement("hd")
                                writer.writeStartElement("wref")
                                writer.writeAttribute("id", head.id)
                                writer.writeAttribute("t", head.token)
                                writer.writeEndElement(false) // wref
                                writer.writeEndElement() // head
                                writer.writeEndElement() // dependency
                            }
                        }
                        writer.writeEndElement() // linkGrp
                    }

                    writer.writeEndElement() // s
                }
                writer.writeEndElement() // p
            }
            writer.writeEndElement() // text
        }
        writer.writeEndElement() // TEI
        writer.writeEndDocument()
        writer.flush()
        writer.close()
    }

    companion object {
        private val alphaNumeric = Regex("""[a-zA-Z0-9]""")
    }
}
