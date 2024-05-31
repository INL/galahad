/**
 * API calls for fetching tagsets.
 */

// Libraries & stores
import axios, { AxiosResponse } from "axios"
// Types & API
import { Tagset } from "@/types/tagset"

const tagsetsPath = `/tagsets`

// Custom types
type TagsetsResponse = AxiosResponse<Tagset[]>
type TagsetResponse = AxiosResponse<Tagset>

// Public methods
/**
 * Get all tagsets.
 */
export function getTagsets(): Promise<TagsetsResponse> {
    return axios.get(tagsetsPath)
}

/**
 * Get single tagset by name.
 * @param tagset Tagset name.
 */
export function getTagset(tagset: string): Promise<TagsetResponse> {
    return axios.get(`${tagsetsPath}/${tagset}`)
}
