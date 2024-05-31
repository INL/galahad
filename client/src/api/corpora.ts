/**
 * API calls for creating, updating, deleting a corpus, and fetching the list of corpora.
 */

// Libraries & stores
import axios, { AxiosResponse } from "axios"
// Types & API
import { UUID, CorpusMetadata, MutableCorpusMetadata } from "@/types/corpora"

// Paths
const corporaPath = `/corpora`
const datasetCorporaPath = `/datasets_corpora`
const publicCorporaPath = `/public_corpora`
const corpusPath = (uuid: UUID) => `${corporaPath}/${uuid}`

// Custom types
type CorporaResponse = AxiosResponse<CorpusMetadata[]>
type CorpusResponse = AxiosResponse<CorpusMetadata>

// Public methods
/**
 * Fetch all corpora for which the user has read access:
 * public (datasets), shared (collaborator/viewer), and owned.
 */
export function getCorpora(): Promise<CorporaResponse> {
    return axios.get(corporaPath)
}

/**
 * Fetch a single corpus by UUID.
 * @param uuid UUID of corpus.
 */
export function getCorpus(uuid: UUID): Promise<CorpusResponse> {
    return axios.get(corpusPath(uuid))
}

/**
 * Fetch all datasets. Note that all datasets are public. Cheaper than getCorpora().
 */
export function getDatasetCorpora(): Promise<CorporaResponse> {
    return axios.get(datasetCorporaPath)
}

/**
 * Fetch all public corpora. Cheaper than getCorpora().
 */
export function getPublicCorpora(): Promise<CorporaResponse> {
    return axios.get(publicCorporaPath)
}

/**
 * Create a new corpus with the given metadata.
 * @param corpus Metadata of new corpus.
 */
export function postCorpus(corpus: MutableCorpusMetadata): Promise<AxiosResponse<UUID>> {
    return axios.post(corporaPath, corpus)
}

/**
 * Delete a corpus by UUID.
 * @param uuid UUID of corpus to delete.
 */
export function deleteCorpus(uuid: UUID) {
    return axios.delete(corpusPath(uuid))
}

/**
 * Update the metadata of a corpus.
 * @param uuid UUID of corpus to update.
 * @param metadata New metadata.
 */
export function patchCorpus(uuid: UUID, metadata: MutableCorpusMetadata) {
    return axios.patch(corpusPath(uuid), metadata)
}
