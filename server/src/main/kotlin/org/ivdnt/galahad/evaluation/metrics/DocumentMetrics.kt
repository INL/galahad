package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Analysis
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.annotations.Term
import org.ivdnt.galahad.evaluation.comparison.EvaluationEntry
import org.ivdnt.galahad.evaluation.comparison.LayerComparison
import org.ivdnt.galahad.evaluation.comparison.TermComparison

class DocumentMetrics(@JsonValue val metrics: Metrics) {
    companion object {
        fun create(
            layerComparison: LayerComparison,
            annotations: List<Annotation>,
            group: Annotation,
            analysis: Analysis,
        ): DocumentMetrics =
            DocumentMetrics(
                Metrics(
                    Metrics.Settings(annotations, group, analysis),
                    buildMap<String, ClassificationClasses> {
                            layerComparison.matches
                                .filter { it.has(group, analysis) }
                                .forEach { tc ->
                                    val mapsToAdd =
                                        mutableListOf<MutableMap<String, ClassificationClasses>>()
                                    if (tc.hyp == Term.EMPTY || tc.ref == Term.EMPTY) {
                                        // handle missing match
                                        val cls =
                                            ClassificationClasses(
                                                noMatch = EvaluationEntry(1, mutableListOf(tc))
                                            )
                                        val group =
                                            if (tc.hyp == Term.EMPTY)
                                                tc.ref.annotationHeadOrMissing(group)
                                            else tc.hyp.annotationHeadOrMissing(group)
                                        mapsToAdd.add(mutableMapOf(group to cls))
                                    } else {
                                        // handle true positive & false negative
                                        var (trueEntry, falseEntry) =
                                            truesFalses(tc) { t -> annotations.all { t.equal(it) } }

                                        if (trueEntry.count > 0) {
                                            val cls =
                                                ClassificationClasses(truePositive = trueEntry)
                                            val groupTP = tc.hyp.annotationHeadOrMissing(group)
                                            val classesMap = mutableMapOf(groupTP to cls)
                                            mapsToAdd.add(classesMap)
                                        }

                                        if (falseEntry.count > 0) {
                                            // false negative
                                            val clsFN =
                                                ClassificationClasses(falseNegative = falseEntry)
                                            val groupFN = tc.ref.annotationHeadOrMissing(group)
                                            val classesMapFN = mutableMapOf(groupFN to clsFN)
                                            mapsToAdd.add(classesMapFN)
                                            // handle false positive
                                            // copy
                                            val falseEntry2 =
                                                EvaluationEntry(
                                                    falseEntry.count,
                                                    falseEntry.samples.toMutableList(),
                                                )
                                            val cls =
                                                ClassificationClasses(falsePositive = falseEntry2)
                                            val groupFP = tc.hyp.annotationHeadOrMissing(group)
                                            val classesMap = mutableMapOf(groupFP to cls)
                                            mapsToAdd.add(classesMap)
                                        }
                                    }
                                    for (map in mapsToAdd) {
                                        this.merge(map.keys.first(), map.values.first()) { x, y ->
                                            x.add(y, truncate = layerComparison.filter == null)
                                        }
                                    }
                                }
                        }
                        .toMutableMap(),
                )
            )

        private fun truesFalses(
            comp: TermComparison,
            cond: (TermComparison) -> Boolean,
        ): Pair<EvaluationEntry, EvaluationEntry> {
            val trues =
                if (cond(comp)) {
                    EvaluationEntry(1, mutableListOf(comp))
                } else {
                    EvaluationEntry()
                }
            val falses =
                if (!cond(comp)) {
                    EvaluationEntry(1, mutableListOf(comp))
                } else {
                    EvaluationEntry()
                }
            return Pair(trues, falses)
        }
    }
}
