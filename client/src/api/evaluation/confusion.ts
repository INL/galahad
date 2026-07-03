/** API for fetching confusion matrix. */

import axios, { type AxiosResponse } from "axios"
import { getBlob, type BlobResponse } from "@/api/utils"
import type { UUID } from "@/types/corpora"
import type { Confusion } from "@/types/evaluation/confusion"
import { endpoints } from "@/api"

/**
 * Fetch the confusion matrix of two corpus layer2.
 * @param corpus Corpus UUID.
 * @param layer Layer name used as hypothesis.
 * @param reference Layer name used as reference.
 * @param annotation Annotation name for evaluation.
 */
export function getConfusion(
    corpus: UUID,
    layer: string,
    reference: string,
    annotation: string,
): Promise<AxiosResponse<Confusion>> {
    return axios.get(endpoints.evaluation.confusion.base({ corpus, layer }, { reference, annotation }))
}

/**
 * Download all samples from a confusion matrix entry.
 * @param corpus Corpus UUID.
 * @param layer Layer name used as hypothesis.
 * @param reference Layer name used as reference.
 * @param annotation Annotation name for evaluation.
 * @param hypFilter Filter for the hypothesis annotation value.
 * @param refFilter Filter for the reference annotation value.
 */
export function getConfusionSamples(
    corpus: UUID,
    layer: string,
    reference: string,
    annotation: string,
    hypFilter: string,
    refFilter: string,
): Promise<BlobResponse> {
    return getBlob(
        endpoints.evaluation.confusion.samples({ corpus, layer }, { reference, annotation, hypFilter, refFilter }),
    )
}
