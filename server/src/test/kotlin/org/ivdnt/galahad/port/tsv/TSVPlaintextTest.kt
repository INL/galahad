package org.ivdnt.galahad.port.tsv

import org.ivdnt.galahad.data.layer.Layer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class TSVPlaintextTest {
    @Test
    fun `Parsed plaintext and mapping`() {
        val tsvFile = TSVFile(File("src/test/resources/tsv/plaintext/peerle.tsv"))
        // Parsing creates entries and plaintext.
        tsvFile.parse()
        assertEquals(1812, tsvFile.entries.size)
        // Assert plaintext.
        val plainText = File("src/test/resources/tsv/plaintext/peerle.txt").readText()
        assertEquals(plainText, tsvFile.plainTextReader().readText())
        // Create annotation layer.
        val mappedLayer: Layer = tsvFile.mapOnPlainText(plainText, "mappedLayer")
        assertEquals(1812, mappedLayer.wordForms.size)
        assertEquals(1812, mappedLayer.terms.size)
    }
}