import axios from "axios"
import { endpoints } from "@/api"
import { getBlob } from "@/api/utils"

export function getGroupedMetrics(
    corpus: UUID,
    layer: string,
    reference: string,
    annotations: string[],
    group: string,
): Promise<MetricsResponse> {
    return axios.get(endpoints.evaluation.metrics.base({ corpus, layer }, { reference, annotations, group }))
}

export function getMetricsSamples(
    corpus: UUID,
    layer: string,
    reference: string,
    annotations: string[],
    group: string,
    classification: string,
    groupFilter?: string,
): Promise<BlobResponse> {
    return getBlob(
        endpoints.evaluation.metrics.download(
            { corpus, layer },
            { reference, annotations, group, classification, groupFilter },
        ),
    )
}
