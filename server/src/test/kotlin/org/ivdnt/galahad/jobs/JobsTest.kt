package org.ivdnt.galahad.jobs

import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.port.createCorpus
import org.ivdnt.galahad.taggers.Taggers
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class JobsTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Create a job`() {
        val name = TestConfig.TAGGER_NAME
        // Check if empty
        assertEquals(0, corpus.jobs.readAll().size)
        val numTaggers = Taggers().summaries.size + 1 // +1 for source layer
        assertEquals(numTaggers, corpus.jobs.readAllJobStatesIncludingPotentialJobs().size)
        assertNull(corpus.jobs.readOrNull(name))
        assertThrows(Exception::class.java) { corpus.jobs.readOrThrow(name) }
        // Create
        val job = corpus.jobs.createOrNull(name)
        // Check if created
        assertNotNull(job)
        assertEquals(1, corpus.jobs.readAll().size)
        assertEquals(numTaggers, corpus.jobs.readAllJobStatesIncludingPotentialJobs().size)
        assertNotNull(corpus.jobs.readOrNull(name))
        assertNotNull(corpus.jobs.readOrThrow(name))
        // delete
        corpus.jobs.delete(name)
        // Check if deleted
        assertEquals(0, corpus.jobs.readAll().size)
        assertNull(corpus.jobs.readOrNull(name))
        assertThrows(Exception::class.java) { corpus.jobs.readOrThrow(name) }
    }
}