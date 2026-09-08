package org.ivdnt.galahad.formats.naf

import java.io.OutputStream
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.export.DocumentExport
import org.ivdnt.galahad.export.LayerWriter
import org.ivdnt.galahad.layer.LayerAnnotations.Companion.contains
import org.ivdnt.galahad.util.XmlUtil
import org.ivdnt.galahad.util.withoutFormatExt
import org.w3c.dom.Document
import org.w3c.dom.Element

class NafWriter(export: DocumentExport) : LayerWriter(export) {
    val now: Long = System.currentTimeMillis()

    override fun convert(out: OutputStream) {
        val xml = XmlUtil.builder.newDocument()
        val root =
            xml.createElement("NAF").apply {
                setAttribute("version", "v3.3")
                setAttribute("xml:lang", export.corpus.metadata.langCode)
            }
        xml.appendChild(root)

        addNafHeader(xml, root)
        addRaw(xml, root)
        addText(xml, root)
        addTerms(xml, root)
        if (Annotation.NER in export.document.metadata.annotations) {
            addEntities(xml, root)
        }
        if (Annotation.DEPREL in export.document.metadata.annotations) {
            addDependencies(xml, root)
        }

        XmlUtil.transformer.transform(DOMSource(root), StreamResult(out))
    }

    private fun addNafHeader(xml: Document, root: Element) {
        val nafHeader = xml.createElement("nafHeader")
        root.appendChild(nafHeader)

        val fileDesc =
            xml.createElement("fileDesc").apply {
                setAttribute("title", export.document.sourceFile.withoutFormatExt)
                setAttribute("author", export.user.name)
                setAttribute("creationtime", now.toString())
                setAttribute("filename", export.document.name)
                setAttribute("filetype", export.document.metadata.format.identifier)
            }
        nafHeader.appendChild(fileDesc)
        val public = xml.createElement("public").apply { setAttribute("publicId", export.layer.id) }
        nafHeader.appendChild(public)

        val lp =
            xml.createElement("lp").apply {
                setAttribute("name", export.tagger.name)
                setAttribute("timestamp", now.toString())
                setAttribute("hostname", "https://galahad.ivdnt.org")
                // export.tagger.version.ifBlank { null }?.let { setAttribute("version", it) }
            }
        val lpTerms =
            xml.createElement("linguisticProcessors").apply { setAttribute("layer", "terms") }
        lpTerms.appendChild(lp)
        nafHeader.appendChild(lpTerms)

        if (Annotation.NER in export.document.metadata.annotations) {
            val lpNer =
                xml.createElement("linguisticProcessors").apply {
                    setAttribute("layer", "entities")
                }
            lpNer.appendChild(lp.cloneNode(true))
            nafHeader.appendChild(lpNer)
        }

        if (Annotation.DEPREL in export.document.metadata.annotations) {
            val lpDep =
                xml.createElement("linguisticProcessors").apply { setAttribute("layer", "deps") }
            lpDep.appendChild(lp.cloneNode(true))
            nafHeader.appendChild(lpDep)
        }
    }

    private fun addRaw(xml: Document, root: Element) {
        val cdata = xml.createCDATASection(export.layer.toString())
        val raw = xml.createElement("raw")
        raw.appendChild(cdata)
        root.appendChild(raw)
    }

    private fun addText(xml: Document, root: Element) {
        val text = xml.createElement("text")
        root.appendChild(text)
        val paragraphs = export.layer.documents.flatMap { it.paragraphs.asSequence() }
        var iSent = 1
        var offset = 0
        paragraphs.forEachIndexed { iPar, paragraph ->
            paragraph.sentences.forEach { sentence ->
                sentence.terms.forEach { t ->
                    val wf =
                        xml.createElement("wf").apply {
                            setAttribute("id", t.id)
                            setAttribute("offset", offset.toString())
                            setAttribute("length", t.token.length.toString())
                            setAttribute("sent", iSent.toString())
                            setAttribute("para", (iPar + 1).toString())
                            textContent = t.token
                        }
                    text.appendChild(wf)
                    offset += t.token.length
                    if (t.spaceAfter == null) offset += 1
                }
                iSent++
            }
        }
    }

    private fun addTerms(xml: Document, root: Element) {
        val terms = xml.createElement("terms")
        root.appendChild(terms)
        export.layer.terms.forEachIndexed { i, it ->
            val term = xml.createElement("term").apply { setAttribute("id", "t${i + 1}") }
            it.lemma?.let { term.setAttribute("lemma", it) }
            it.pos?.let { term.setAttribute("pos", it) }
            terms.appendChild(term)

            // target span
            val target = xml.createElement("target").apply { setAttribute("id", it.id) }
            val span = xml.createElement("span").apply { appendChild(target) }
            term.appendChild(span)
        }
    }

    private fun addEntities(xml: Document, root: Element) {
        val entities = xml.createElement("entities")
        root.appendChild(entities)

        documents.forEach { doc ->
            doc.paragraphs.forEach { par ->
                par.sentences.forEach { sent ->
                    sent.spans?.get(Annotation.NER)?.forEach { termSpan ->
                        val spanEl = xml.createElement("span")
                        val terms = termSpan.indices.map { sent.terms[it] }
                        terms.forEach { t ->
                            val target =
                                xml.createElement("target").apply { setAttribute("id", t.id) }
                            spanEl.appendChild(target)
                        }
                        val references = xml.createElement("references")
                        references.appendChild(spanEl)
                        val termSpanId = terms.joinToString("_") { it.id }
                        val entity =
                            xml.createElement("entity").apply {
                                setAttribute("id", termSpanId) // Alternatively, e1, e2, etc.
                                setAttribute("type", termSpan.value)
                            }
                        entity.appendChild(references)
                        entities.appendChild(entity)
                    }
                }
            }
        }
    }

    private fun addDependencies(xml: Document, root: Element) {
        val deps = xml.createElement("deps")
        root.appendChild(deps)
        var runningTermIndex = 0
        var firstSentenceTermIndex = 0
        documents.forEach { doc ->
            doc.paragraphs.forEach { par ->
                par.sentences.forEach { sent ->
                    firstSentenceTermIndex = runningTermIndex
                    sent.terms.forEachIndexed { i, t ->
                        runningTermIndex++
                        if (t.deprel?.lowercase() != "root") {
                            xml.createElement("dep")
                                .apply {
                                    // <dep from="t5" to="t1" rfunc="nsubj"/>
                                    setAttribute(
                                        "from",
                                        "t${firstSentenceTermIndex + t.head!!.toInt()}",
                                    )
                                    setAttribute("to", "t$runningTermIndex")
                                    setAttribute("rfunc", t.deprel)
                                }
                                .also { deps.appendChild(it) }
                        }
                    }
                }
            }
        }
    }
}
