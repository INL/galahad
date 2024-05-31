/**
 * API calls for fetching taggers and their health status.
 */

// Libraries & stores
import axios, { AxiosResponse } from "axios"
// Types & API
import { Tagger, TaggerHealth } from "@/types/taggers"

// Paths
const taggersPath = `/taggers`
const taggerPath = (tagger: string) => `${taggersPath}/${tagger}`
const taggerHealthPath = (tagger: string) => `${taggerPath(tagger)}/health`

// Custom types
type TaggersResponse = AxiosResponse<Tagger[]>
type TaggerResponse = AxiosResponse<Tagger>
type TaggerHealthResponse = AxiosResponse<TaggerHealth>
type TaggersBusyResponse = AxiosResponse<number>

// Public methods
/**
 * Get all taggers.
 */
export function getTaggers(): Promise<TaggersResponse> {
    return axios.get(taggersPath)
}

/**
 * Get single tagger by name.
 * @param tagger Tagger name.
 */
export function getTagger(tagger: string): Promise<TaggerResponse> {
    return axios.get(taggerPath(tagger))
}

/**
 * Get tagger health status.
 * @param tagger Tagger name.
 */
export function getTaggerHealth(tagger: string): Promise<TaggerHealthResponse> {
    return axios.get(taggerHealthPath(tagger))
}

/**
 * Get how many docs are currently actively processing.
 * Summed over all taggers & corpora on the server.
 */
export function getDocsAtTaggers(): Promise<TaggersBusyResponse> {
    return axios.get(`${taggersPath}/active`)
}
