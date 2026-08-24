package org.ivdnt.galahad.formats.pdf

import com.itextpdf.text.pdf.PdfReader as PdfReaderIText
import com.itextpdf.text.pdf.parser.PdfTextExtractor
import java.io.InputStream
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Layer
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.formats.reader.LayerReader

class PdfReader(stream: InputStream) : LayerReader() {
    val reader = PdfReaderIText(stream)

    override fun read(): Layer {
        for (i in 1..reader.numberOfPages) {
            val text = PdfTextExtractor.getTextFromPage(reader, i)
            text
                .ifBlank { null }
                ?.split(whitespace)
                ?.filter { it.isNotBlank() }
                ?.forEach { word ->
                    terms += Term(wordID(), mapOf(Annotation.TOKEN to word))
                }
        }
        newDocument()
        return Layer(documents.toTypedArray())
    }

    companion object {
        val whitespace: Regex = Regex("""\s+""")
    }
}
