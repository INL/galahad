package org.ivdnt.galahad.port.tsv

import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

internal class TSVBodyTest {
    @Test
    fun `Skip empty lines`() {
        val tsvFile = TSVFile(File("src/test/resources/tsv/body/emptylines.tsv"))
        assertTSVFile(tsvFile)
    }

    // Technically, tsv files are not supposed to have comments,
    // but some corpora use # above a sentence/document to indicate provenance.
    @Test
    fun `Skip comments`() {
        val tsvFile = TSVFile(File("src/test/resources/tsv/body/comments.tsv"))
        assertTSVFile(tsvFile)
    }

    @Test
    fun `Body with extra columns`() {
        val tsvFile = TSVFile(File("src/test/resources/tsv/body/extra-columns.tsv"))
        assertTSVFile(tsvFile)
        val expected = "scholen loop " // Note the space.
        assertEquals(expected, tsvFile.plainTextReader().readText())
    }

    @Test
    fun `Missing values`() {
        // Contains 3 files with a missing value for lemma, pos and literal.
        for (file in File("src/test/resources/tsv/body-missing-values").listFiles()!!) {
            val tsvFile = TSVFile(file)
            tsvFile.parse()
            val type = file.nameWithoutExtension.split("-")[1]
            val entries = tsvFile.entries
            when (type) {
                // Empty string when lemma or pos is missing.
                "lemma" -> assertEquals(null, entries[0].lemma)
                "pos" -> assertEquals(null, entries[0].pos)
                // No entries when literal is missing.
                // After all, this would not generate plaintext.
                "word" -> assertEquals(0, tsvFile.entries.size)
            }
        }
    }

    // The files in the body/ folder have the same content, so reuse the same test.
    private fun assertTSVFile(tsvFile: TSVFile) {
        tsvFile.parse()
        assertEntries(tsvFile.entries)
        assertSourceLayer(tsvFile.sourceLayer())
    }

    private fun assertEntries(entries: ArrayList<TSVEntry>) {
        assertEquals(2, entries.size)
        val first = entries[0]
        assertEquals("scholen", first.literal)
        assertEquals("school", first.lemma)
        assertEquals("NOU", first.pos)
        val second = entries[1]
        assertEquals("loop", second.literal)
        assertEquals("lopen", second.lemma)
        assertEquals("VRB", second.pos)
    }

    private fun assertSourceLayer(layer: Layer) {
        assertEquals(SOURCE_LAYER_NAME, layer.name)
        // count
        assertEquals(2, layer.wordForms.size)
        assertEquals(2, layer.terms.size)
        // wordforms
        assertWordFormAndTerm(layer, 0, "scholen", "school", "NOU")
        assertWordFormAndTerm(layer, 1, "loop", "lopen", "VRB")
    }

    // We don't assert offsets here, because they will vary depending on the file due to newlines and such.
    private fun assertWordFormAndTerm(
        layer: Layer, i: Int, literal: String, lemma: String, pos: String,
    ) {
        // wordform
        val wf = layer.wordForms[i]
        assertEquals(literal, wf.literal)
        assertEquals("w$i", wf.id)
        assertEquals(literal.length, wf.length)
        // term
        val term = layer.terms[i]
        assertEquals(lemma, term.lemma)
        assertEquals(pos, term.pos)
        assertEquals(1, term.targets.size)
        assertEquals(wf, term.targets[0])
        assertEquals(false, term.isMultiTarget)
        assertEquals(literal, term.literals)
        assertEquals(null, term.posFeatures)
        assertEquals(pos, term.posHead)
    }
}