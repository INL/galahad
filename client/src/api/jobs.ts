/** API for fetching, starting & cancelling jobs & job progress of a corpus.*/

import axios, { type AxiosResponse } from "axios"
import type { UUID } from "@/types/corpora"
import type { Job, Progress } from "@/types/jobs"
import { endpoints } from "@/api"

/**
 * Fetch all jobs of a corpus.
 * @param corpus Corpus UUID.
 */
export function getJobs(corpus: UUID): Promise<Job[]> {
    return axios.get(endpoints.jobs.base({ corpus }))
}

/**
 * Start a job.
 * @param corpus Corpus UUID.
 * @param job Tagger name.
 */
export function postJob(corpus: UUID, job: string): Promise<AxiosResponse> {
    return axios.post(endpoints.jobs.job({ corpus, job }))
}

/**
 * Cancel a job.
 * @param corpus Corpus UUID.
 * @param job Tagger name.
 */
export function cancelJob(corpus: UUID, job: string): Promise<AxiosResponse> {
    return axios.delete(endpoints.jobs.job({ corpus, job }))
}

/**
 * Fetch the progress of a job.
 * @param corpus Corpus UUID.
 * @param job Tagger name.
 */
export function getJobProgress(corpus: UUID, job: string): Promise<AxiosResponse<Progress>> {
    return axios.get(endpoints.jobs.progress({ corpus, job }))
}
