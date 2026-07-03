/** API for fetching and removing layers of a corpus. */

import axios, { type AxiosResponse } from "axios"
import type { UUID } from "@/types/corpora"
import type { LayerMetadata } from "@/types/layers"
import { endpoints } from "@/api"

/**
 * Fetch all layers for a corpus.
 * @param corpus Corpus UUID.
 */
export function getLayers(corpus: UUID): Promise<AxiosResponse<LayerMetadata[]>> {
    return axios.get(endpoints.layers.base({ corpus }))
}

/**
 * Remove a layer from a corpus.
 * @param corpus Corpus UUID.
 * @param layer Layer name.
 */
export function removeLayer(corpus: UUID, layer: string): Promise<AxiosResponse> {
    return axios.delete(endpoints.layers.layer({ corpus, layer }))
}
