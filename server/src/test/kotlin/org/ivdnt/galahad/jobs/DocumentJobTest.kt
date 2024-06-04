package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.layer.Layer
import org.ivdnt.galahad.port.LayerBuilder
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.util.*

class DocumentJobTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Create DocumentJob`() {
        // add a doc
        val name = corpus.documents.create(File.createTempFile("tmp", ".txt"))
        // create a job
        val job: Job = corpus.jobs.createOrThrow(TestConfig.TAGGER_NAME)

        val dj: DocumentJob = job.document(name)
        // verify
        assertEquals(name, dj.name)
        assertNull(dj.getError)
        assertNull(dj.getProcessingID)
        assertFalse(dj.isProcessing)
        assertEquals(Layer.EMPTY, dj.result)
        assertEquals(DocumentJob.DocumentProcessingStatus.PENDING, dj.status)

        // set error
        dj.setError("error")
        assertEquals("error", dj.getError)
        assertEquals(DocumentJob.DocumentProcessingStatus.ERROR, dj.status)

        // setting pid should delete error
        val id = UUID.randomUUID()
        dj.setProcessingID(id)
        assertNull(dj.getError)
        assertEquals(id, dj.getProcessingID)
        assertEquals(DocumentJob.DocumentProcessingStatus.PROCESSING, dj.status)

        // Cancel should delete pid
        dj.cancel()
        assertNull(dj.getProcessingID)
        assertNull(dj.getError)
        assertEquals(DocumentJob.DocumentProcessingStatus.PENDING, dj.status)

        // set result should finish
        val layer = LayerBuilder().loadDummies(100).build()
        dj.setResult(layer)
        assertEquals(100, dj.result.terms.size)
        assertNull(dj.getProcessingID)
        assertNull(dj.getError)
        assertEquals(DocumentJob.DocumentProcessingStatus.FINISHED, dj.status)

    }
}