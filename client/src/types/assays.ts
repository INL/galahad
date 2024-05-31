export type ClassificationMetrics = {
    accuracy: number
    precision: number
    recall: number
    f1: number
}

export type TaggerAssay = {
    micro: ClassificationMetrics
    macro: ClassificationMetrics
}

export type MetricTypeAssay = {
    [taggerName: string]: TaggerAssay
}

export type DatasetAssay = {
    [metricName: string]: MetricTypeAssay
}

export type Assays = {
    [datasetName: string]: DatasetAssay
}

export type IndividualAssay = {
    [metricName: string]: TaggerAssay
}