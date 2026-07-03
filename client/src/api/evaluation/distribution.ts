/** API for fetching type-token distribution. */

import axios, { type AxiosResponse } from "axios"
import type { UUID } from "@/types/corpora"
import type { TypeToken } from "@/types/evaluation/distribution"
import { endpoints } from "@/api"

/**
 * Fetch the type-token distribution of a corpus layer.
 * @param corpus Corpus UUID.
 * @param layer Layer name.
 * @param annotation Annotation name for evaluation.
 * @param group Annotation name for grouping.
 */
export function getDistribution(
    corpus: UUID,
    layer: string,
    annotation: string,
    group: string,
): Promise<AxiosResponse<TypeToken[]>> {
    return axios.get(endpoints.evaluation.distribution({ corpus, layer }, { annotation, group }))
}
