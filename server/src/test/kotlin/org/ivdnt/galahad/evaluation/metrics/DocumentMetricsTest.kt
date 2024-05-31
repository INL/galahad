package org.ivdnt.galahad.evaluation.metrics

import org.ivdnt.galahad.data.corpus.Corpus
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.port.LayerBuilder
import org.ivdnt.galahad.port.createCorpus
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DocumentMetricsTest {
    lateinit var corpus: Corpus

    @BeforeEach
    fun initCorpus() {
        corpus = createCorpus()
    }

    // @Test
    // fun `Matching layers`() {
    //     val builder = LayerBuilder().loadDummies(1, pos = "NOU").loadDummies(2, pos = "VRB").loadDummies(3, pos = "ADJ")
    //     val hypo = builder.build()
    //     val ref = builder.build()
    //     val metrics = DocumentMetrics(hypothesis = hypo, reference = ref)
    //     val global = metrics.global
    //     assertEquals(6, global.count)
    //     assertEquals(6, global.bothAgree.count)
    //     assertEquals(6, global.lemmaAgree.count)
    //     assertEquals(6, global.posAgree.count)
    //     assertEquals(0, global.lemmaDisagree.count)
    //     assertEquals(0, global.posDisagree.count)
    //     assertEquals(0, global.noMatch.count)
    //
    //     val perPos = metrics.grouped
    //     assertEquals(3, perPos.size)
    //     val posses = setOf("NOU", "VRB", "ADJ")
    //     // indexed for loop over posses
    //     for ((i, pos) in posses.withIndex()) {
    //         val count = i + 1
    //         val metric = perPos.find { it.name == pos }!!
    //         assertEquals(count, metric.count)
    //         assertEquals(count, metric.bothAgree.count)
    //         assertEquals(count, metric.lemmaAgree.count)
    //         assertEquals(count, metric.posAgree.count)
    //         assertEquals(0, metric.lemmaDisagree.count)
    //         assertEquals(0, metric.posDisagree.count)
    //         assertEquals(0, metric.noMatch.count)
    //     }
    // }
    //
    // @Test
    // fun `Missing match due to punctuation`() {
    //     val hypo = LayerBuilder().loadText("dummy, dummy").build()
    //     val ref = LayerBuilder().loadText("dummy").loadText(", dummy").build()
    //
    //     val metrics = DocumentMetrics(hypothesis = hypo, reference = ref)
    //     val global = metrics.global
    //
    //     assertEquals(3, global.count)
    //     assertEquals(1, global.bothAgree.count)
    //     // one lemma is "dummy," vs "dummy"
    //     assertEquals(1, global.lemmaAgree.count)
    //     assertEquals(1, global.lemmaDisagree.count)
    //     // loadText() defaults to "pos", so they will agree.
    //     assertEquals(2, global.posAgree.count)
    //     assertEquals(0, global.posDisagree.count)
    //     assertEquals(1, global.noMatch.count)
    //
    //     // The comma in the reference has no match to any hypothesis term
    //     val lemmaDisagreeSamples = global.noMatch.samples
    //     assertEquals(1, lemmaDisagreeSamples.size)
    //     val sample = lemmaDisagreeSamples[0]
    //     assertEquals(",", sample.refTerm.lemma)
    //     assertEquals(Term.EMPTY, sample.hypoTerm)
    // }
}