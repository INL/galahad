package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.ivdnt.galahad.evaluation.EvaluationEntry
import org.ivdnt.galahad.port.csv.CSVFile
import org.ivdnt.galahad.port.csv.CSVHeader
import org.ivdnt.galahad.port.csv.CSVRecord
import org.ivdnt.galahad.util.toFixed

/**
 * The number of [Term]s between two [Layer]s that are equal, different or not present in the reference and hypothesis layers.
 * Based on lemma and part of speech.
 *
 * Note that normal [Metric]s always contain samples, which are cheap to get when we are calculating them anyway,
 * However for downstream applications like aggregates, we may want to omit the samples
 * Therefore we can use this utility class
 */

data class ClassificationMetrics(
    val precision: Float = 0f,
    val recall: Float = 0f,
    val f1: Float = 0f,
    val accuracy: Float = 0f,
) {
    operator fun plus(other: ClassificationMetrics): ClassificationMetrics = ClassificationMetrics(
        precision + other.precision,
        recall + other.recall,
        f1 + other.f1,
        accuracy + other.accuracy,
    )

    operator fun div(divisor: Int): ClassificationMetrics = this * (1.0f / divisor)

    operator fun times(factor: Float): ClassificationMetrics = ClassificationMetrics(
        precision * factor,
        recall * factor,
        f1 * factor,
        accuracy * factor,
    )

    companion object {
        fun calculate(cls: ClassificationClasses): ClassificationMetrics {
            return calculate(cls.flat)
        }

        fun calculate(cls: FlatClassificationClasses, micro: Boolean = false): ClassificationMetrics {
            val tp = cls.truePositive.toFloat()
            val fp = cls.falsePositive.toFloat()
            val fn = cls.falseNegative.toFloat()
            // When calculating micro-accuracy, tp and fp are the same, so don't double count.
            val total = if(micro) tp + fp else cls.count.toFloat()
            return calculate(tp, fp, fn, total)
        }

        private fun calculate(tp: Float, fp: Float, fn: Float, total: Float): ClassificationMetrics {
            fun notNaN(value: Float): Float = if (value.isNaN()) 0.0f else value

            val precision = notNaN(tp / (tp + fp))
            val recall = notNaN(tp / (tp + fn))

            return ClassificationMetrics(
                precision = precision,
                recall = recall,
                f1 = notNaN(2.0f * (precision * recall) / (precision + recall)),
                accuracy = notNaN(tp / (total)),
            )
        }
    }
}

/** Use for micro-averaging */
open class FlatClassificationClasses(
    var truePositive: Int = 0,
    var falsePositive: Int = 0,
    var falseNegative: Int = 0,
    // Similar to falseNegative.
    var noMatch: Int = 0,
    var count: Int = 0,
) {
    operator fun plus(other: FlatClassificationClasses) = FlatClassificationClasses(
        truePositive + other.truePositive,
        falsePositive + other.falsePositive,
        falseNegative + other.falseNegative,
        noMatch + other.noMatch,
        count + other.count,
    )
}

open class ClassificationClasses(
    var truePositive: EvaluationEntry = EvaluationEntry(),
    var falsePositive: EvaluationEntry = EvaluationEntry(),
    var falseNegative: EvaluationEntry = EvaluationEntry(),
    // Similar to falseNegative.
    var noMatch: EvaluationEntry = EvaluationEntry(),
    /** sample count without duplicates, for calculating accuracy. */
    @JsonIgnore var count: Int = 1,
) {
    open val classCount: Int
        get() = truePositive.count + falsePositive.count + falseNegative.count + noMatch.count

    fun add(other: ClassificationClasses, truncate: Boolean = true): ClassificationClasses {
        truePositive = EvaluationEntry.add(truePositive, other.truePositive, truncate)
        falsePositive = EvaluationEntry.add(falsePositive, other.falsePositive, truncate)
        falseNegative = EvaluationEntry.add(falseNegative, other.falseNegative, truncate)
        noMatch = EvaluationEntry.add(noMatch, other.noMatch, truncate)
        count += other.count
        return this
    }

    @get:JsonIgnore
    open val flat: FlatClassificationClasses
        get() = FlatClassificationClasses(
            truePositive = truePositive.count,
            falsePositive = falsePositive.count,
            falseNegative = falseNegative.count,
            noMatch = noMatch.count,
            count = count
        )
}

data class Metric(
    @JsonProperty("name") val name: String,
    @JsonProperty("classes") var cls: ClassificationClasses = ClassificationClasses(),
) {
    @get:JsonProperty("metrics")
    val clsMetrics
        get() = ClassificationMetrics.calculate(cls)

    fun add(other: Metric, truncate: Boolean): Metric {
        cls.add(other.cls, truncate)
        return this
    }

    fun toCSVRecord(): CSVRecord {
        return CSVFile.toCSVRecord(listOf(
            name,
            clsMetrics.precision.toFixed(),
            clsMetrics.recall.toFixed(),
            clsMetrics.f1.toFixed(),
            cls.classCount,
            cls.truePositive.count,
            cls.falsePositive.count,
            cls.falseNegative.count,
            cls.noMatch.count,
        ))
    }

    companion object {
        fun getCsvHeader(): CSVHeader {
            return CSVFile.toCSVHeader(listOf(
                "grouped by",
                "precision",
                "recall",
                "f1",
                "count",
                "true positive count",
                "false positive count",
                "false negative count",
                "no match count")
            )
        }
    }
}