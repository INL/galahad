package org.ivdnt.galahad.evaluation.confusion

import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.evaluation.EvaluationUtil
import org.ivdnt.galahad.evaluation.comparison.ConfusionLayerFilter
import org.ivdnt.galahad.evaluation.comparison.PosLemmaTermFilter
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.port.Resource
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class CorpusConfusionTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    @Test
    fun `Confusion of three docs summed`() {
        EvaluationUtil.add_two_docs_to_corpus(corpus)
        EvaluationUtil.addDocWithMissingMatches(corpus)
        val cc = CorpusConfusion(corpus, "pie-tdn") // default reference is SOURCE_LAYER_NAME
        // Table
        cc.table.forEach(::println)
        assertEquals(7, cc.table.size)
        assertEquals(1, cc.table["NOU"]?.size)
        assertEquals(2, cc.table["PD"]?.size)
        // missing should exist
        assertEquals(1, cc.table[TermComparison.MISSING_MATCH]?.size)
        assertEquals(4, cc.table[TermComparison.MISSING_MATCH]?.get("LET")?.count)

        // Matrix
        cc.matrix.forEach(::println)
        assertEquals(9, cc.matrix.size) // 4 matching pairs + 1 wrong
        // (VRB, VRB) from the 1st doc should exist
        assertEquals(2, cc.matrix["VRB" to "VRB"]?.count)
        // (PD, WRONG) from the 2nd doc should exist
        assertEquals(1, cc.matrix["WRONG" to "PD"]?.count)
        //
    }

    @Test
    fun `To CSV`() {
        EvaluationUtil.add_two_docs_to_corpus(corpus)
        EvaluationUtil.addDocWithMissingMatches(corpus)
        val cc = CorpusConfusion(corpus, "pie-tdn") // default reference is SOURCE_LAYER_NAME
        val csv: String = cc.countsToCSV()
        assertEquals(Resource.get("evaluation/confusion/output.csv").readText(), csv)
    }

    @Test
    fun `PoS confusion with filter`() {
        EvaluationUtil.addDocWithMissingMatches(corpus)
        val filter = ConfusionLayerFilter(
            hypoTermFilter = PosLemmaTermFilter(posHeadGroup = "LET"),
            refTermFilter = PosLemmaTermFilter(posHeadGroup = TermComparison.MISSING_MATCH),
        )

        val cc = CorpusConfusion(corpus, "pie-tdn", layerFilter = filter)
        assertEquals(Resource.get("evaluation/confusion/let-vs-missing.csv").readText(), cc.samplesToCSV())
    }
}