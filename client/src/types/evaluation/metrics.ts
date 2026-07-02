import type { EvaluationEntry } from "@/types/evaluation"

export type Metrics = {
    settings: MetricsSettings
    grouped: Record<string, ClassificationClasses>
    classes: ClassificationClasses
    micro: ClassificationMetrics
    macro: ClassificationMetrics
}
export type GlobalMetrics = {
    layer: string
    settings: MetricsSettings
    classes: ClassificationClasses
    micro: ClassificationMetrics
    macro: ClassificationMetrics
}
export type MetricsSettings = { annotations: string[]; group: string }
export type ClassificationClasses = {
    truePositive: EvaluationEntry
    falsePositive: EvaluationEntry
    falseNegative: EvaluationEntry
    noMatch: EvaluationEntry
    hypothesis: number
    reference: number
    metrics: ClassificationMetrics
}
export type ClassificationMetrics = { accuracy: number; precision: number; recall: number; f1: number }
