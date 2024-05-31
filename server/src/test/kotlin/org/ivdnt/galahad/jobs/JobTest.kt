package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.layer.LayerPreview
import org.ivdnt.galahad.evaluation.metrics.FlatMetricType
import org.ivdnt.galahad.port.LayerBuilder
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class JobTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Create a job`() {
        val job: Job = corpus.jobs.createOrThrow("pie-tdn")
        // verify
        assertEquals("pie-tdn", job.name)
        assertFalse(job.isActive)
        assertEquals(0, job.assay.read<Map<String,FlatMetricType>>().size)
        assertEquals(0, job.progress.total)
        assertEquals(LayerPreview.EMPTY, job.preview)
        // verify from state cache
        assertEquals(LayerPreview.EMPTY, job.state.preview)
        assertEquals(0, job.state.progress.total)
        assertEquals(0, job.state.resultSummary.numTerms)
    }

    @Test
    fun `Fake tagger result`(){
        // add a doc
        val name = corpus.documents.create(File.createTempFile("tmp", ".txt"))
        // create a job
        val job: Job = corpus.jobs.createOrThrow("pie-tdn")
        // fake a tagger result
        val layer = LayerBuilder().loadDummies(100).build()
        job.document(name).setResult(layer)
        // verify
        assertEquals(100, job.document(name).result.terms.size)
        assertEquals(1, job.progress.finished)
    }
}