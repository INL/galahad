package org.ivdnt.galahad.evaluation.metrics

import org.ivdnt.galahad.util.notNaN

class ClassificationMetrics(
    val accuracy: Float = 0f,
    val precision: Float = 0f,
    val recall: Float = 0f,
    val f1: Float = 0f,
) {
    operator fun plus(o: ClassificationMetrics): ClassificationMetrics =
        ClassificationMetrics(
            accuracy + o.accuracy,
            precision + o.precision,
            recall + o.recall,
            f1 + o.f1,
        )

    operator fun div(f: Float): ClassificationMetrics =
        ClassificationMetrics(accuracy / f, precision / f, recall / f, f1 / f)

    companion object {

        fun from(classes: ClassificationClasses): ClassificationMetrics {
            val tp = classes.truePositive.count.toFloat()
            val fp = classes.falsePositive.count.toFloat()
            val fn = classes.falseNegative.count.toFloat()
            val accuracy = notNaN(tp / (tp + fp + fn))
            val precision = notNaN(tp / (tp + fp))
            val recall = notNaN(tp / (tp + fn))
            val f1 = notNaN(2f * (precision * recall) / (precision + recall))
            return ClassificationMetrics(accuracy, precision, recall, f1)
        }
    }
}
