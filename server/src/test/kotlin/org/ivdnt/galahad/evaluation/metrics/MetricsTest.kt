package org.ivdnt.galahad.evaluation.metrics

import org.ivdnt.galahad.evaluation.EvaluationEntry
import org.ivdnt.galahad.port.Resource
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MetricsTest {
    // @Test
    // fun `Global test`() {
    //     val metrics = dummyMetrics()
    //     val global: Metric = metrics.global
    //     assertEquals(2, global.count)
    //     assertEquals(2, global.bothAgree.count)
    //     assertEquals(0, global.lemmaAgree.count)
    // }
    //
    // @Test
    // fun `Per PoS`() {
    //     val metrics = dummyMetrics()
    //     val perPos: Set<Metric> = metrics.grouped
    //     assertEquals(2, perPos.size)
    //     for (metric in perPos) {
    //         assertEquals(1, metric.count)
    //         assertEquals(1, metric.bothAgree.count)
    //         assertEquals(0, metric.lemmaAgree.count)
    //     }
    //     assertEquals(setOf("NOU", "VRB"), perPos.map { it.name }.toSet())
    // }
    //
    // @Test
    // fun `Merge metrics`() {
    //     val metrics = dummyMetrics()
    //     val other = dummyMetrics()
    //     metrics.add(other)
    //     val global: Metric = metrics.global
    //     assertEquals(4, global.count)
    //     assertEquals(4, global.bothAgree.count)
    //     assertEquals(0, global.lemmaAgree.count)
    //     val perPos: Set<Metric> = metrics.grouped
    //     assertEquals(2, perPos.size)
    //     assertEquals(setOf("NOU", "VRB"), perPos.map { it.name }.toSet())
    // }
    //
    // @Test
    // fun `To CSV`() {
    //     assertEquals(Resource.get("evaluation/metrics/output.csv").readText(), dummyMetrics().toGlobalCsv())
    // }
    //
    // private fun dummyMetrics(): Metrics {
    //     val metrics = Metrics()
    //     val nou = Metric("NOU", 1, bothAgree = EvaluationEntry(1, listOf()))
    //     val verb = Metric("VRB", 1, bothAgree = EvaluationEntry(1, listOf()))
    //
    //     metrics.add(nou)
    //     metrics.add(verb)
    //     return metrics
    // }
}