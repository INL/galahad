package org.ivdnt.galahad.data.document

import org.ivdnt.galahad.data.DocumentWriteType
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.DocumentsController
import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach

class DocumentsTest {

    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    // Not implemented for now. Creating twice does the same job.
    @Test
    fun `Update not implemented`() {
        val file = Resource.get("all-formats/input/input.folia.xml")
        val docWriteType = DocumentWriteType(file.name, file.inputStream())
        assertThrows(Exception::class.java) { corpus.documents.update("input.folia.xml", docWriteType) }
    }

    @Test
    fun `Create and delete files`() {
        // Add two files
        addFile("all-formats/input/input.folia.xml")
        addFile("all-formats/input/input.conllu")
        // 2 files exist
        assertDocsinDocuments(setOf("input.folia.xml", "input.conllu"))
        // Delete the first
        deleteFile("input.folia.xml")
        // 1 is left
        assertDocsinDocuments(setOf("input.conllu"))
        // Try to access deleted file
        assertFileDeleted("input.folia.xml")
        // Delete the last file
        deleteFile("input.conllu")
        // 0 left
        assertDocsinDocuments(setOf())
        // Try to access deleted file
        assertFileDeleted("input.conllu")
    }

    fun addFile(path: String) {
        val file = Resource.get(path)
        // The file does not exist
        assertThrows(Exception::class.java) { corpus.documents.readOrThrow(file.name) }
        assertNull(corpus.documents.readOrNull(file.name))
        assertFalse(corpus.documents.allNames.contains(file.name))
        assertFalse(corpus.documents.readAll().map { it.name }.contains(file.name))
        // The file is created
        val name = corpus.documents.create(file)
        // It should exist
        assertEquals(file.name, name)
        val doc = corpus.documents.readOrThrow(name)
        assertEquals(file.name, doc.name)
        assert(corpus.documents.allNames.contains(file.name))
        assert(corpus.documents.readAll().map { it.name }.contains(file.name))
    }

    /**
     * Assert the given docs are in documents
     * @param docs Docs as a set, because the order of documents is not guaranteed
     */
    private fun assertDocsinDocuments(docs: Set<String>) {
        assertEquals(docs, corpus.documents.allNames.toSet())
        assertEquals(docs.size, corpus.documents.readAll().size)
    }

    private fun deleteFile(name: String) {
        System.gc() // Apparently, currently out of scope File() instances lock the file.
        corpus.documents.delete(name)
    }

    private fun assertFileDeleted(name: String) {
        assertNull(corpus.documents.readOrNull(name))
        assertFalse(corpus.documents.allNames.contains(name))
        assertFalse(corpus.documents.readAll().map { it.name }.contains(name))
        assertThrows(Exception::class.java) { corpus.documents.readOrThrow(name) }
    }
}