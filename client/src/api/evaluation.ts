/**
 * API calls for fetching evaluation metrics and downloading them as a zip report.
 */

import axios, { type AxiosResponse } from "axios"
import { getBlob, type BlobResponse } from "@/api/utils"
import type { UUID } from "@/types/corpora"
import type { Confusion, DistributionWrapper, Metrics, TermComparison } from "@/types/evaluation"
import type { DocumentEntities, JobEntities, JobsEntities } from "@/types/evaluation/entities"
import { endpoints } from "."

type ConfusionResponse = AxiosResponse<Confusion>
type DistributionResponse = AxiosResponse<DistributionWrapper>
type MetricsResponse = AxiosResponse<Metrics>
type DocumentEntitiesResponse = AxiosResponse<DocumentEntities>
type JobEntitiesResponse = AxiosResponse<JobEntities>
type JobsEntitiesResponse = AxiosResponse<JobsEntities>

const evaluationPath = (corpus: UUID): string => `/corpora/${corpus}/evaluation`
const layerPath = (corpus: UUID, layer: string): string => `/corpora/${corpus}/layers/${layer}/evaluation`
export const confusionPath = (corpus: UUID): string => `${evaluationPath(corpus)}/confusion`
const confusionSamplesPath = (corpus: UUID): string => `${confusionPath(corpus)}/download`
export const distributionPath = (corpus: UUID, layer: string): string => `${layerPath(corpus, layer)}/distribution`
export const metricsPath = (corpus: UUID, layer: string): string => `${layerPath(corpus, layer)}/metrics`
const metricsSamplesPath = (corpus: UUID): string => `${metricsPath(corpus)}/download`
const downloadPath = (corpus: UUID): string => `${evaluationPath(corpus)}/download`
const documentLayerComparisonPath = (corpus: UUID, document: string): string =>
    `/corpora/${corpus}/documents/${document}/evaluation/comparison`
const documentEntitiesPath = (corpus: UUID, job: string, document: string): string =>
    `/corpora/${corpus}/jobs/${job}/documents/${document}/entities`
const jobEntitiesPath = (corpus: UUID, job: string): string => `${evaluationPath(corpus, job)}/entities`
export const jobsEntitiesPath = (corpus: UUID): string => `/corpora/${corpus}/evaluation/entities`
/**
 * Fetch term frequency distribution.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagging job name as hypothesis layer.
 */
export function getDistribution(
    corpus: UUID,
    layer: string,
    annotation: string,
    group: string,
): Promise<DistributionResponse> {
    return axios.get(endpoints.evaluation.distribution({ corpus, layer }, { annotation, group }))
}

/**
 * Fetch term confusion matrix.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference Tagger job name as reference layer.
 */
export function getConfusion(
    corpus: UUID,
    layer: string,
    reference: string,
    annotation: string,
): Promise<ConfusionResponse> {
    return axios.get(endpoints.evaluation.confusion.base({ corpus, layer }, { reference, annotation }))
}

/**
 * Fetch Lemma & PoS accuracy metrics.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference Tagger job name as reference layer.
 */
export function getMetrics(corpus: UUID, hypothesis: string, reference: string): Promise<MetricsResponse> {
    return axios.get(endpoints.evaluation.metrics.base({ corpus, layer: hypothesis }, { reference }))
}

/**
 * Download evaluation zip.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference  Tagger job name as reference layer.
 */
export function getDownloadEvaluation(corpus: UUID, hypothesis: string, reference: string): Promise<BlobResponse> {
    return getBlob(endpoints.evaluation.download({ corpus, layer: hypothesis }, { reference }))
}

/**
 * Download confusion entries as zip.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference  Tagger job name as reference layer.
 * @param hypoAnnot PoS tag of the hypothesis layer to filter on.
 * @param refAnnot PoS tag of the reference layer to filter on.
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

// /**
//  * Fetch the document layer comparison of a single document comparing the job layer to the reference layer.
//  * @param corpus UUID of the corpus.
//  * @param job Job name.
//  * @param document Document name.
//  * @param reference Reference layer name.
//  * @returns Document layer comparison.
//  */
// export function getDocumentLayerComparison(
//     corpus: UUID,
//     job: string,
//     document: string,
//     reference: string,
// ): Promise<AxiosResponse<TermComparison[]>> {
//     return axios.get(documentLayerComparisonPath(corpus, document), { params: { reference, hypothesis: job } })
// }

// export function getDocumentEntities(corpus: UUID, job: string, document: string): Promise<DocumentEntitiesResponse> {
//     return axios.get(documentEntitiesPath(corpus, job, document))
// }

// export function getJobEntities(corpus: UUID, job: string): Promise<JobEntitiesResponse> {
//     return axios.get(jobEntitiesPath(corpus, job))
// }

// export function getJobsEntities(corpus: UUID): Promise<JobsEntitiesResponse> {
//     return axios.get(jobsEntitiesPath(corpus))
// }
