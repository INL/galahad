package org.ivdnt.galahad.evaluation.distribution

import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.document.SOURCE_LAYER_NAME
import org.ivdnt.galahad.evaluation.EvaluationUtil
import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CorpusDistributionTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Distribution of two docs sum up`() {
        EvaluationUtil.add_two_docs_to_corpus(corpus)
        EvaluationUtil.addDocWithMissingMatches(corpus)
        val dist = CorpusDistribution(corpus, SOURCE_LAYER_NAME)
        assertEquals(6, dist.distribution.size)
        // chars
        assertEquals(18, dist.totalChars)
        assertEquals(15, dist.totalAlphabeticChars)
        assertEquals(64, dist.coveredChars)
        assertEquals(60, dist.coveredAlphabeticChars)
        assertFalse(dist.isTrimmed)
        // csv
        assertEquals(Resource.get("evaluation/distribution/output.csv").readText(), dist.toCSV())
        // Trimmed version
        val trimmed = dist.trim(2)
        assertEquals(2, trimmed.distribution.size)
        assertTrue(trimmed.isTrimmed)
        assertEquals(Resource.get("evaluation/distribution/trimmed.csv").readText(), trimmed.toCSV())
    }
}