package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonValue
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.corpora.Corpus
import org.ivdnt.galahad.evaluation.DocumentEvaluations
import org.ivdnt.galahad.evaluation.csv.CsvFile
import org.ivdnt.galahad.evaluation.csv.CsvString
import org.ivdnt.galahad.util.merge
import org.ivdnt.galahad.util.parallelMap

class JobMetrics(@JsonValue val metrics: Metrics) {

    fun toGlobalCsv(): CsvString = buildString {
        append(
            CsvFile.toCsvString(
                listOf(
                    "annotation",
                    "grouped by",
                    "macro precision",
                    "macro recall",
                    "macro f1",
                    "micro accuracy",
                    "count",
                    "true positive",
                    "false negative",
                    "no match",
                )
            )
        )
        append(
            CsvFile.toCsvString(
                listOf(
                    metrics.settings.group,
                    metrics.macro.precision,
                    metrics.macro.recall,
                    metrics.macro.f1,
                    metrics.accuracy, // todo other micros
                    metrics.classes.hypothesis,
                    metrics.classes.truePositive.count,
                    metrics.classes.falseNegative.count,
                    metrics.classes.noMatch.count,
                )
            )
        )
    }

    companion object {
        fun create(
            corpus: Corpus,
            docEvals: DocumentEvaluations,
            annotations: List<Annotation>,
            group: Annotation,
            analysis: Annotation.Analysis,
        ): JobMetrics =
            JobMetrics(
                corpus.documents
                    .readAll()
                    .parallelMap {
                        if (docEvals.jobs.filter == null) {
                            docEvals
                                .createOrThrow(it.name)
                                .getMetrics(annotations, group, analysis)
                                .metrics
                        } else {
                            DocumentMetrics.create(
                                    docEvals.createOrThrow(it.name).layerComparison,
                                    annotations,
                                    group,
                                    analysis,
                                )
                                .metrics
                        }
                    }
                    .reduce { map1, map2 ->
                        map1.grouped.merge(map2.grouped) { x, y ->
                            x.add(y, truncate = docEvals.jobs.filter == null)
                        }
                        map1
                    }
            )

        fun toCsv(metric: Metrics): CsvString = buildString {
            append(
                CsvFile.toCsvString(
                    listOf(
                        "group",
                        "precision",
                        "recall",
                        "f1",
                        "hypothesis count",
                        "reference count",
                        "true positive",
                        "false positive",
                        "false negative",
                        "no match",
                    )
                )
            )
            for ((key, value) in metric.grouped) {
                append(
                    CsvFile.toCsvString(
                        listOf(
                            key,
                            value.metrics.precision,
                            value.metrics.recall,
                            value.metrics.f1,
                            value.hypothesis,
                            value.reference,
                            value.truePositive.count,
                            value.falsePositive.count,
                            value.falseNegative.count,
                            value.noMatch.count,
                        )
                    )
                )
            }
        }
    }
}
