package org.ivdnt.galahad.documents

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.util.TestUtil
import org.ivdnt.galahad.util.TestUtil.get
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DocumentMetadataTest() {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    @Test
    fun `Properties for an unannotated file`() {
        corpus.documents.createOrThrow(get("formats/shared/converter/karel_en_martijn.txt"))
        val meta = corpus.documents.readOrThrow("karel_en_martijn").metadata
        assertEquals("karel_en_martijn", meta.name)
        assertEquals(DocumentFormat.Txt, meta.format)
        assert(meta.text.contains("Fraaie historie ende alwaer"))
        val total = meta.annotations.annotations[Annotation.TOKEN]
        assertEquals(39, total)
    }

    @Test
    fun `Properties for an annotated file`() {
        corpus.documents.createOrThrow(get("formats/shared/converter/karel_en_martijn.tei.xml"))
        val meta = corpus.documents.readOrThrow("karel_en_martijn").metadata
        assertEquals("karel_en_martijn", meta.name)
        assertEquals(DocumentFormat.TeiP5, meta.format)
        assert(meta.text.contains("Fraaie historie ende alwaer"))
        val total = meta.annotations.annotations[Annotation.TOKEN]
        assertEquals(52, total)
    }
}
