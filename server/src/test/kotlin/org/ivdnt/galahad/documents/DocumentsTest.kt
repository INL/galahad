package org.ivdnt.galahad.documents

import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.util.TestUtil
import org.ivdnt.galahad.util.withoutFormatExt
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DocumentsTest {

    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = TestUtil.createCorpus()
    }

    @Test
    fun `Create and delete files`() {
        // Add two files
        addFile("web/corpus/karel.tei.xml")
        addFile("web/corpus/martijn.tei.xml")
        // 2 files exist
        assertDocsinDocuments(setOf("karel", "martijn"))
        // Delete the first
        deleteFile("karel")
        // 1 is left
        assertDocsinDocuments(setOf("martijn"))
        // Try to access deleted file
        assertFileDeleted("karel")
        // Delete the last file
        deleteFile("martijn")
        // 0 left
        assertDocsinDocuments(setOf())
        // Try to access deleted file
        assertFileDeleted("martijn")
    }

    private fun addFile(path: String) {
        val file = TestUtil.get(path)
        // The file does not exist
        assertThrows(Exception::class.java) { corpus.documents.readOrThrow(file.name) }
        assertNull(corpus.documents.readOrNull(file.name))
        assertFalse(file.name in corpus.documents.readAll().map { it.name })
        // The file is created
        val doc = corpus.documents.createOrThrow(file)
        assertEquals(file.withoutFormatExt, doc.name)
        assert(file.withoutFormatExt in corpus.documents.readAll().map { it.name })
    }

    /**
     * Assert the given docs are in documents
     *
     * @param docs Docs as a set, because the order of documents is not guaranteed
     */
    private fun assertDocsinDocuments(docs: Set<String>) {
        assertEquals(docs, corpus.documents.readAll().map { it.name }.toSet())
        assertEquals(docs.size, corpus.documents.readAll().size)
    }

    private fun deleteFile(name: String) {
        System.gc() // Apparently, currently out of scope File() instances lock the file.
        corpus.documents.deleteOrThrow(name)
    }

    private fun assertFileDeleted(name: String) {
        assertNull(corpus.documents.readOrNull(name))
        assertFalse(name in corpus.documents.readAll().map { it.name })
        assertThrows(Exception::class.java) { corpus.documents.readOrThrow(name) }
    }
}
