package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ivdnt.galahad.data.layer.Term
import org.ivdnt.galahad.evaluation.CsvSampleExporter
import org.ivdnt.galahad.evaluation.EvaluationEntry
import org.ivdnt.galahad.evaluation.comparison.TermComparison
import org.ivdnt.galahad.port.csv.CSVFile
import org.ivdnt.galahad.port.csv.CSVHeader
import org.ivdnt.galahad.util.toFixed

typealias FlatMetricTypeAssay = Map<String, FlatMetricType>
class FlatMetricType(
    val micro: ClassificationMetrics = ClassificationMetrics(),
    val macro: ClassificationMetrics = ClassificationMetrics(),
)

class MetricsType(
    val setting: MetricsSettings,
    @JsonIgnore var truncate: Boolean = true
) : CsvSampleExporter {
    @JsonIgnore
    var map: MutableMap<String, Metric> = HashMap()

    /** Metrics separated per POS. */
    val grouped: Set<Metric>
        get() {
            return if (map.size > TRUNCATE) {
                // sort mt.value.map on mt.value.map["someKey"].cls.classCount, take the first TRUNCATE elements, and then map
                map.entries.asSequence()
                        .sortedByDescending { it.value.cls.classCount }.take(TRUNCATE)
                        .associateBy({ it.key }, { it.value }).values.toSet()
            } else {
                map.values.toSet()
            }
        }

    val classes: ClassificationClasses
        get() = map.values.map { it.cls }.toMutableList().apply{ this.add(0, ClassificationClasses(count=0)) }.reduce { a, b -> a.add(b, truncate) }.apply { falsePositive = EvaluationEntry() }

    val macro: ClassificationMetrics
        get() {
            if (map.isEmpty()) {
                return ClassificationMetrics()
            }
            return map.values.map { it.clsMetrics }.reduce { a, b -> a + b } / map.size
        }

    val micro: ClassificationMetrics
        get() {
            if (map.isEmpty()) {
                return ClassificationMetrics()
            }
            return ClassificationMetrics.calculate(map.values.map { it.cls.flat }.reduce { a, b -> a + b }, micro = true)
        }

    fun toGlobalCsv(): String {
        // Expensive calculations.
        val microMetrics = micro
        val macroMetrics = macro

        return CSVFile.toCSVRecord(listOf(
            setting.annotation,
            setting.group,
            macroMetrics.precision.toFixed(),
            macroMetrics.recall.toFixed(),
            macroMetrics.f1.toFixed(),
            microMetrics.accuracy.toFixed(),
            classes.classCount,
            classes.truePositive.count,
            classes.falseNegative.count,
            classes.noMatch.count,
        ))
    }

    fun toFlat(): FlatMetricType {
        return FlatMetricType(micro, macro)
    }

    fun toGroupedCsv(): String {
        var csv = Metric.getCsvHeader()
        grouped.sortedBy { it.name }.forEach{ csv += it.toCSVRecord() }
        return csv
    }

    // Cumulative addition functions.
    private fun add(metric: Metric) {
        map.merge(metric.name, metric) { m1, m2 -> m1.add(m2, truncate) }
    }

    fun add(other: MetricsType) {
        other.map.values.toSet().forEach(this::add)
    }

    fun add(comp: TermComparison) {
        if (!setting.filterBy(comp)) {
            return
        }

        if (comp.hypoTerm == Term.EMPTY) {
            add (
                Metric(
                    name = setting.groupBy(comp.refTerm),
                    cls = ClassificationClasses(
                        noMatch = EvaluationEntry(1, mutableListOf(comp)),
                        count = 0
                    )
                )
            )
        }

        // One of these two will be empty, we don't know which.
        val (trues, falses) = truesFalses(comp, setting::termsEqual)
        val cls = ClassificationClasses(
            truePositive = trues,
            falseNegative = falses,
        )
        add(
            Metric(
                name = setting.groupBy(comp.refTerm),
                cls = cls
            )
        )
        if (falses.count != 0) {
            // This term is also be someone else's false positive, so switch around.
            val cls2 = ClassificationClasses(
                falsePositive = EvaluationEntry(count = falses.samples.size, falses.samples.toMutableList()),
                count = if (setting.groupBy(comp.hypoTerm) == setting.groupBy(comp.refTerm)) 0 else 1
            )
            add(
                Metric(
                    name = setting.groupBy(comp.hypoTerm), // Terms are switched, so hypo.
                    cls = cls2
                )
            )
        }
    }

    private fun truesFalses(comp: TermComparison, cond: (TermComparison) -> Boolean): Pair<EvaluationEntry, EvaluationEntry> {
        val trues = if (cond(comp)) {
            EvaluationEntry(1, mutableListOf(comp))
        } else {
            EvaluationEntry()
        }
        val falses = if (!cond(comp)) {
            EvaluationEntry(1, mutableListOf(comp))
        } else {
            EvaluationEntry()
        }
        return Pair(trues, falses)
    }

    fun samplesToCsv(group: String, classType: String): String {
        return when (classType) {
            "truePositive" -> samplesToCSV(map[group]?.cls?.truePositive?.samples)
            "falsePositive" -> samplesToCSV(map[group]?.cls?.falsePositive?.samples)
            "falseNegative" -> samplesToCSV(map[group]?.cls?.falseNegative?.samples)
            "noMatch" -> samplesToCSV(map[group]?.cls?.noMatch?.samples)
            else -> ""
        }
    }

    fun samplesToCsv(classType: String): String {
        return when (classType) {
            "truePositive" -> samplesToCSV(classes.truePositive.samples)
            "falseNegative" -> samplesToCSV(classes.falseNegative.samples)
            "noMatch" -> samplesToCSV(classes.noMatch.samples)
            else -> ""
        }
    }


    override fun samplesToCSV(): String {
        val cls = listOf(classes.falsePositive, classes.falseNegative, classes.truePositive)
        return samplesToCSV(cls.flatMap { it.samples })
    }
}