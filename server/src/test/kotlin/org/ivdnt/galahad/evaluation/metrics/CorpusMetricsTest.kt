package org.ivdnt.galahad.evaluation.metrics

import jakarta.servlet.GenericFilter
import org.ivdnt.galahad.TestConfig
import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.evaluation.EvaluationUtil
import org.ivdnt.galahad.evaluation.comparison.MetricsLayerFilter
import org.ivdnt.galahad.evaluation.comparison.PosLemmaTermFilter
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CorpusMetricsTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Metrics of two docs sum up`() {
        EvaluationUtil.add_two_docs_to_corpus(corpus)

        // Get the metrics
        val metrics = CorpusMetrics(corpus, listOf(PosByPosMetricsSettings()), TestConfig.TAGGER_NAME) // default reference is SOURCE_LAYER_NAME
        // Check the global metrics
        val global = metrics.metricTypes.values.first()
        assertEquals(10, global.classes.classCount)
        assertEquals(0, global.classes.noMatch.count)
        assertEquals(9, global.classes.truePositive.count)
        assertEquals(1, global.classes.falseNegative.count)

        // Check the per-POS metrics
        val perPos = metrics.metricTypes.values.first().grouped
        assertEquals(5, perPos.size) // posses (4) + WRONG (1)
        val posses = setOf("NOU", "VRB", "ADJ", "PD")
        for ((i, pos) in posses.withIndex()) {
            val count = i + 1
            val metric = perPos.find { it.name == pos }!!
            assertEquals(count, metric.cls.count)
            assertEquals(0, metric.cls.noMatch.count)
        }
        // check false negative and false positive
        val pdMetric = perPos.find { it.name == "PD" }!!
        assertEquals(1, pdMetric.cls.falseNegative.count)
        val wrongMetric = perPos.find { it.name == "WRONG" }!!
        assertEquals(1, wrongMetric.cls.falsePositive.count)
    }

    @Test
    fun `Filtered CorpusMetrics`() {
        EvaluationUtil.add_two_docs_to_corpus(corpus)
        EvaluationUtil.addDocWithMatchingMultiPosLemma(corpus)

        val termFilter = PosLemmaTermFilter(posHeadGroup = "PD+NOU-C", lemma = null)
        val filter = MetricsLayerFilter(termFilter,termFilter)
        val filteredMetrics = CorpusMetrics(corpus, listOf(PosByPosMetricsSettings()), TestConfig.TAGGER_NAME, layerFilter = filter)
        val filteredGlobal = filteredMetrics.metricTypes.values.first()
        assertEquals(1, filteredGlobal.classes.classCount)
    }
}