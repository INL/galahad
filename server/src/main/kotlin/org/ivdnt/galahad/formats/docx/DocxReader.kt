package org.ivdnt.galahad.formats.docx

import java.io.InputStream
import org.apache.poi.xwpf.usermodel.XWPFDocument
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.formats.reader.LayerReader

class DocxReader(stream: InputStream) : LayerReader() {
    val doc: XWPFDocument = XWPFDocument(stream)

    override fun read(): Layer {
        doc.paragraphs.forEach { paragraph ->
            paragraph.text
                .ifBlank { null }
                ?.split(whitespace)
                ?.forEach { word ->
                    terms += Term(wordID(), mapOf(Annotation.TOKEN to word))
                }
            newParagraph()
        }
        newDocument()
        return Layer(documents.toTypedArray())
    }

    companion object {
        val whitespace: Regex = Regex("""\s+""")
    }
}
