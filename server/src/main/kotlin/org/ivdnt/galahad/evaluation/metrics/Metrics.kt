package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.evaluation.csv.CsvFile
import org.ivdnt.galahad.evaluation.csv.CsvString
import org.ivdnt.galahad.util.notNaN

class Metrics(
    val settings: Settings,
    val grouped: MutableMap<String, ClassificationClasses> = mutableMapOf(),
) {
    val classes: ClassificationClasses by lazy {
        grouped.values
            .takeIf { it.isNotEmpty() }
            ?.reduce { a, b -> a + b }
            ?.apply { // TODO truncate in web controller
                truePositive.truncate()
                falsePositive.truncate()
                falseNegative.truncate()
                noMatch.truncate()
            } ?: ClassificationClasses()
    }

    @get:JsonIgnore
    val untruncatedClasses: ClassificationClasses by lazy {
        grouped.values.takeIf { it.isNotEmpty() }?.reduce { a, b -> a + b }
            ?: ClassificationClasses()
    }

    val accuracy: Float
        get() = notNaN(classes.truePositive.count / classes.hypothesis.toFloat())

    val macro: ClassificationMetrics
        get() =
            grouped.values
                .takeIf { it.isNotEmpty() }
                ?.map { it.metrics }
                ?.reduce { a, b -> a + b }
                ?.div(grouped.size.toFloat()) ?: ClassificationMetrics()

    class Settings(
        val annotations: List<Annotation>,
        val group: Annotation,
        val analysis: Annotation.Analysis,
    ) {
        @JsonIgnore
        val name: String =
            "metrics-${Annotation.order(annotations).joinToString("-")}-$analysis-${group}"
    }

    fun toGlobal(layer: String): GlobalMetrics =
        GlobalMetrics(layer, settings, classes, accuracy, macro)
}

class GlobalMetrics(
    val layer: String,
    val settings: Metrics.Settings,
    val classes: ClassificationClasses,
    val accuracy: Float,
    val macro: ClassificationMetrics,
) {
    fun toCsv(): CsvString =
        CsvFile.toCsvString(
            listOf(
                settings.annotations.joinToString(" "),
                settings.group,
                accuracy,
                macro.accuracy,
                macro.precision,
                macro.recall,
                macro.f1,
                classes.hypothesis,
                classes.truePositive.count,
                classes.falseNegative.count,
                classes.noMatch.count,
            )
        )

    companion object {
        fun getCsvHeader(): CsvString =
            CsvFile.toCsvString(
                listOf(
                    "annotations",
                    "group",
                    "micro accuracy",
                    "macro accuracy",
                    "macro precision",
                    "macro recall",
                    "macro f1",
                    "count",
                    "true positive",
                    "false negative",
                    "no match",
                )
            )
    }
}
