package org.ivdnt.galahad.port.xml

import org.ivdnt.galahad.data.document.DocumentFormat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

internal class BLFXMLParserTest {

    @Test
    fun editorialNoteTest() {

        val plainTextFile = File.createTempFile("editorialnotes-plaintext", ".txt")
        val p = BLFXMLParser.forFileWithFormat(
            DocumentFormat.TeiP5,
            File(this::class.java.classLoader.getResource("tei/dummies/editorialnotes.xml")!!.toURI()),
            plainTextFile.outputStream()
        ) { _, _, _ -> }
        assertEquals(
            "\nthis text should be included. \n" + "\n" + " But this note ", plainTextFile.readText()
        )
    }
}