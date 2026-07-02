package org.ivdnt.galahad.evaluation.metrics

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ivdnt.galahad.annotations.Annotation

class Metrics(
    val settings: Settings,
    val grouped: MutableMap<String, ClassificationClasses> = mutableMapOf(),
) {
    val classes: ClassificationClasses by lazy {
        grouped.values
            .reduce { a, b -> a.add(b, truncate = false) }
            .apply {
                truePositive.truncate()
                falsePositive.truncate()
                falseNegative.truncate()
                noMatch.truncate()
            }
    }

    val micro: ClassificationMetrics
        get() = classes.metrics

    val macro: ClassificationMetrics
        get() = grouped.values.map { it.metrics }.reduce { a, b -> a + b } / grouped.size.toFloat()

    class Settings(val annotations: List<Annotation>, val group: Annotation) {
        @JsonIgnore
        val name: String =
            "metrics-${Annotation.order(annotations).joinToString("-")}-${group.value}"
    }

    fun toGlobal(layer: String): GlobalMetrics =
        GlobalMetrics(layer, settings, classes, micro, macro)
}

class GlobalMetrics(
    val layer: String,
    val settings: Metrics.Settings,
    val classes: ClassificationClasses,
    val micro: ClassificationMetrics,
    val macro: ClassificationMetrics,
)
