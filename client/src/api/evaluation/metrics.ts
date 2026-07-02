import axios, { type AxiosResponse } from "axios"
import { endpoints } from "@/api"
import { getBlob, type BlobResponse } from "@/api/utils"
import type { GlobalMetrics, Metrics } from "@/types/evaluation/metrics"
import type { UUID } from "@/types/corpora"

export function getGroupedMetrics(
    corpus: UUID,
    layer: string,
    reference: string,
    annotations: string[],
    group: string,
): Promise<AxiosResponse<Metrics>> {
    return axios.get(endpoints.evaluation.metrics.base({ corpus, layer }, { reference, annotations, group }))
}

export function getGlobalMetrics(
    corpus: UUID,
    layer: string,
    reference: string,
): Promise<AxiosResponse<GlobalMetrics[]>> {
    return axios.get(endpoints.evaluation.metrics.base({ corpus, layer }, { reference }))
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
