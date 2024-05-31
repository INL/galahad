/**
 * API calls for fetching assays.
 */

// Libraries & stores
import axios, { AxiosResponse } from "axios"
// Types & API
import { UUID } from "@/types/corpora"
import { Assays, IndividualAssay } from "@/types/assays"

const assaysPath = `/assays`
const assayPath = (corpus: UUID, job: string) => `/corpora/${corpus}/jobs/${job}/evaluation/assay`

// Custom types
type AssaysResponse = AxiosResponse<Assays>
type AssayResponse = AxiosResponse<IndividualAssay>

// Public methods
/**
 * Fetch all assays.
 */
export function getAssays(): Promise<AssaysResponse> {
    return axios.get(assaysPath)
}

/**
 * Fetch single assay of job on corpus.
 * @param uuid UUID of assay.
 * @param job tagger job name.
 */
// Currently unused
export function getAssay(corpus: UUID, job: string): Promise<AssayResponse> {
    return axios.get(assayPath(corpus, job))
}
