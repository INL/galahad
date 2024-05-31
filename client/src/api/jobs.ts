/**
 * API calls for getting existing jobs and job layer results, posting and cancelling jobs, and polling job progress.
 */

// Libraries & stores
import axios, { AxiosResponse } from "axios"
// Types & API
import { Job, Progress, JobDocumentResult } from "@/types/jobs"
import { UUID } from "@/types/corpora"

// Paths
const jobsPath = (corpus: UUID) => `/corpora/${corpus}/jobs`
const jobPath = (corpus: UUID, job: string) => `/corpora/${corpus}/jobs/${job}`
const jobDocumentResultPath = (corpus: UUID, job: string, document: string) => {
    return `/corpora/${corpus}/jobs/${job}/documents/${document}/result`
}
const jobHasErrorPath = (corpus: UUID, job: string) => `/corpora/${corpus}/jobs/${job}/hasError`
const jobIsBusyPath = (corpus: UUID, job: string) => `/corpora/${corpus}/jobs/${job}/isBusy`
const jobProgressPath = (corpus: UUID, job: string) => `/corpora/${corpus}/jobs/${job}/progress`

// Custom types
type JobsResponse = AxiosResponse<Job[]>
type JobResponse = AxiosResponse<Job>
type JobDocumentResultResponse = AxiosResponse<JobDocumentResult>
export type ProgressResponse = AxiosResponse<Progress>

// Public methods
/**
 * Fetch all jobs for a corpus.
 * @param corpus UUID of the corpus.
 * @param includePotentialJobs If true, will also include untagged jobs (so all jobs will be present).
 */
export function getJobs(corpus: UUID, includePotentialJobs: boolean): Promise<JobsResponse> {
    return axios.get(jobsPath(corpus), { params: { includePotentialJobs: includePotentialJobs } })
}

/**
 * Fetch a single job for a corpus.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 */
export function getJob(corpus: UUID, job: string) {
    return axios.get(jobPath(corpus, job)) as Promise<JobResponse>
}

/**
 * Post a job to start it. Will return an immediate ProgressResponse.busy=true to give the illusion of a started job.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name. 
 */
export function postJob(corpus: UUID, job: string): Promise<ProgressResponse> {
    return axios.post(jobPath(corpus, job))
}

/**
 * Cancel or delete a job.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param hard True to delete the job, false to cancel it.
 */
export function cancelOrDeleteJob(corpus: UUID, job: string, hard: boolean): Promise<ProgressResponse> {
    return axios.delete(jobPath(corpus, job), { params: { hard: hard } })
}

/**
 * Get the layer result of a tagger job. Only includes a preview (to spare bandwidth).
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param document Document name.
 */
export function getJobDocumentResult(corpus: UUID, job: string, document: string): Promise<JobDocumentResultResponse> {
    return axios.get(jobDocumentResultPath(corpus, job, document))
}

export function getJobHasError(corpus: UUID, job: string): Promise<AxiosResponse<boolean>> {
    return axios.get(jobHasErrorPath(corpus, job))
}

/**
 * Simplified job progress poll.
 */
export function getJobIsBusy(corpus: UUID, job: string): Promise<AxiosResponse<boolean>> {
    return axios.get(jobIsBusyPath(corpus, job))
}

/**
 * Poll for job progress.
 */
export function getJobProgress(corpus: UUID, job: string): Promise<ProgressResponse> {
    return axios.get(jobProgressPath(corpus, job))
}
