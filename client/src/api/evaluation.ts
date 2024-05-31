/**
 * API calls for fetching evaluation metrics and downloading them as a zip report.
 */

// Libraries & stores
import axios, { AxiosResponse } from "axios"
// API & types
import { ConfusionWrapper, DistributionWrapper, Metrics } from "@/types/evaluation"
import { UUID } from "@/types/corpora"
import * as Utils from "@/api/utils"
import { BlobResponse } from "@/api/utils"

// Paths
const evaluationPath = (corpus: UUID, hypothesis: string) => `/corpora/${corpus}/jobs/${hypothesis}/evaluation`
const confusionPath = (corpus: UUID, hypothesis: string, reference: string) => {
    return `${evaluationPath(corpus, hypothesis)}/confusion?reference=${reference}`
}
const posConfusionPath = (corpus: UUID, hypothesis: string, reference: string, hypothesisPOS: string, referencePOS: string) => {
    return `${evaluationPath(corpus, hypothesis)}/confusion/download?reference=${reference}&hypoPosFilter=${hypothesisPOS}&refPosFilter=${referencePOS}`
}

const distributionPath = (corpus: UUID, hypothesis: string) => `${evaluationPath(corpus, hypothesis)}/distribution`
const metricsPath = (corpus: UUID, hypothesis: string, reference: string) => {
    return `${evaluationPath(corpus, hypothesis)}/metrics?reference=${reference}`
}
const metricsSamplesPath = (corpus: UUID, hypothesis: string, reference: string, setting: string, classType: string, group?: string) => {
    return `${evaluationPath(corpus, hypothesis)}/metrics/download?reference=${reference}&setting=${setting}&class=${classType}` + (group ? `&group=${group}` : '')
}

const downloadPath = (corpus: UUID, hypothesis: string, reference: string) => {
    return `${evaluationPath(corpus, hypothesis)}/download?reference=${reference}`
}

// Custom Types
type ConfusionResponse = AxiosResponse<ConfusionWrapper>
type DistributionResponse = AxiosResponse<DistributionWrapper>
type MetricsResponse = AxiosResponse<Metrics>

// Public methods
/**
 * Fetch term frequency distribution.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagging job name as hypothesis layer.
 */
export function getDistribution(corpus: UUID, hypothesis: string): Promise<DistributionResponse> {
    return axios.get(distributionPath(corpus, hypothesis))
}

/**
 * Fetch term confusion matrix.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference Tagger job name as reference layer.
 */
export function getConfusion(corpus: UUID, hypothesis: string, reference: string): Promise<ConfusionResponse> {
    return axios.get(confusionPath(corpus, hypothesis, reference))
}

/**
 * Fetch Lemma & PoS accuracy metrics.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference Tagger job name as reference layer.
 */
export function getMetrics(corpus: UUID, hypothesis: string, reference: string): Promise<MetricsResponse> {
    return axios.get(metricsPath(corpus, hypothesis, reference))
}

/**
 * Download evaluation zip.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference  Tagger job name as reference layer.
 */
export function getDownloadEvaluation(corpus: UUID, hypothesis: string, reference: string): Promise<BlobResponse> {
    return Utils.getBlob(downloadPath(corpus, hypothesis, reference))
}

/**
 * Download PoS confusion entries as zip.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference  Tagger job name as reference layer.
 * @param hypothesisPOS PoS tag of the hypothesis layer to filter on.
 * @param referencePOS PoS tag of the reference layer to filter on.
 */
export function getDownloadPosConfusion(corpus: UUID, hypothesis: string, reference: string, hypothesisPOS: string, referencePOS: string): Promise<BlobResponse> {
    return Utils.getBlob(posConfusionPath(corpus, hypothesis, reference, hypothesisPOS, referencePOS))
}

/**
 * Download metrics samples as zip.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference  Tagger job name as reference layer.
 * @param setting Setting for the metrics. E.g. 'posByPos'
 * @param classType Class type for the metrics. E.g. 'truePositive'.
 * @param group Group for the metrics. E.g. 'pos' or 'lemma'.
 */
export function getMetricsSamples(corpus: UUID, hypothesis: string, reference: string, setting: string, classType: string, group?: string): Promise<BlobResponse> {
    if (group) {
        group = encodeURIComponent(group)
    }
    return Utils.getBlob(metricsSamplesPath(corpus, hypothesis, reference, setting, classType, group))
}
