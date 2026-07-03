/** API for fetching accuracy metrics. */

import axios, { type AxiosResponse } from "axios"
import { endpoints } from "@/api"
import { getBlob, type BlobResponse } from "@/api/utils"
import type { GlobalMetrics, Metrics } from "@/types/evaluation/metrics"
import type { UUID } from "@/types/corpora"

/**
 * Fetch grouped accuracy metrics between two corpus layers.
 * @param corpus Corpus UUID.
 * @param layer Layer name used as hypothesis.
 * @param reference Layer name used as reference.
 * @param annotations Annotation names for evaluation.
 * @param group Annotation name for grouping.
 */
export function getGroupedMetrics(
    corpus: UUID,
    layer: string,
    reference: string,
    annotations: string[],
    group: string,
    analysis: string,
): Promise<AxiosResponse<Metrics>> {
    return axios.get(endpoints.evaluation.metrics.base({ corpus, layer }, { reference, annotations, group, analysis }))
}

/**
 * Fetch global accuracy metrics between two corpus layers.
 * @param corpus Corpus UUID.
 * @param layer Layer name used as hypothesis.
 * @param reference Layer name used as reference.
 */
export function getGlobalMetrics(
    corpus: UUID,
    layer: string,
    reference: string,
): Promise<AxiosResponse<GlobalMetrics[]>> {
    return axios.get(endpoints.evaluation.metrics.base({ corpus, layer }, { reference }))
}

/**
 * Fetch global accuracy metrics over all layers of a corpus.
 * @param corpus Corpus UUID.
 * @param annotations Annotation names for evaluation.
 * @param group Annotation name for grouping.
 * @param analysis Annotation analysis type.
 */
export function getCorpusMetrics(
    corpus: UUID,
    annotations: string[],
    group: string,
    analysis: string,
): Promise<AxiosResponse<GlobalMetrics[]>> {
    return axios.get(endpoints.benchmarks({ corpus }, { annotations, group, analysis }))
}

/**
 * Download all samples from a metrics evaluation entry.
 * @param corpus Corpus UUID.
 * @param layer Layer name used as hypothesis.
 * @param reference Layer name used as reference.
 * @param annotations Annotation names for evaluation.
 * @param group Annotation name for grouping.
 * @param classification Classification class for filtering.
 * @param groupFilter Optional filter for the group annotation value.
 */
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
