package org.ivdnt.galahad.evaluation.metrics

import org.ivdnt.galahad.annotations.Annotation
import org.ivdnt.galahad.evaluation.comparison.TermComparison

class Metrics(
    val settings: Settings,
    val grouped: MutableMap<String, ClassificationClasses> = mutableMapOf(),
) {
    val classes: ClassificationClasses by lazy {
        grouped.filter { it.key != TermComparison.MISSING_MATCH }.values.reduce { a, b -> a + b }
    }

    val micro: ClassificationMetrics
        get() = classes.metrics

    val macro: ClassificationMetrics
        get() = grouped.values.map { it.metrics }.reduce { a, b -> a + b } / grouped.size.toFloat()

    class Settings(val annotations: List<Annotation>, val group: Annotation) {
        val name: String =
            "metrics-${Annotation.order(annotations).joinToString("-")}-${group.value}"
    }
}
