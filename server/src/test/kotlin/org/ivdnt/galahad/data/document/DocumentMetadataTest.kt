package org.ivdnt.galahad.data.document

import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class DocumentMetadataTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Properties for an unannotated file`() {
        val path = "all-formats/input/input.txt"
        val file = Resource.get(path)
        val plaintext = file.readText()
        val doc = Resource.getDoc(path)
        val meta = doc.metadata.expensiveGet()
        assertEquals("input.txt", meta.name)
        assertEquals(DocumentFormat.Txt.identifier, meta.format)
        assertEquals(plaintext.count { it.isLetter() }, meta.numAlphabeticChars)
        assertEquals(plaintext.length, meta.numChars)
        assertEquals(plaintext, meta.preview) // This works because the preview is < MAX_PREVIEW_LENGTH
        val layer = meta.layerSummary
        val total = layer.numLemma + layer.numPOS + layer.numTerms + layer.numWordForms
        assertEquals(0, total)
        assert(meta.valid)
    }

    @Test
    fun `Properties for an annotated file`() {
        val path = "all-formats/input/input.tei.xml"
        val file = Resource.get(path)
        val doc = Resource.getDoc(path)
        val plaintext = doc.plaintext
        val meta = doc.metadata.expensiveGet()
        assertEquals("input.tei.xml", meta.name)
        assertEquals(DocumentFormat.TeiP5.identifier, meta.format)
        assertEquals(plaintext.count { it.isLetter() }, meta.numAlphabeticChars)
        assertEquals(plaintext.length, meta.numChars)
        assertEquals(plaintext, meta.preview) // This works because the preview is < MAX_PREVIEW_LENGTH
        val layer = meta.layerSummary
        assertEquals(13, layer.numLemma)
        assertEquals(13, layer.numPOS)
        assertEquals(21, layer.numTerms)
        assertEquals(21, layer.numWordForms)
        assert(meta.valid)
    }
}