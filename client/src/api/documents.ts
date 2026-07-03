/** API for fetching uploading and deleting documents of a corpus layer. */

import axios, { type AxiosResponse } from "axios"
import { getBlob, type BlobResponse } from "@/api/utils"
import type { UUID } from "@/types/corpora"
import type { DocumentMetadata } from "@/types/documents"
import { endpoints } from "@/api"
import { SOURCE_LAYER } from "@/types/jobs"

/**
 * Fetch all documents of a corpus layer.
 * @param corpus Corpus UUID.
 * @param layer Layer name.
 */
export function getDocuments(corpus: UUID, layer: string): Promise<DocumentMetadata[]> {
    return axios.get(endpoints.documents.base({ corpus, layer }))
}

/**
 * Upload a document.
 * @param corpus Corpus UUID.
 * @param document Document FormData.
 * @param contentType Content type of the document. Must be explicitly set for tsv-files. Others are induced.
 */
export function postDocument(
    corpus: UUID,
    document: FormData,
    contentType?: Record<string, string>,
): Promise<AxiosResponse> {
    return axios.post(endpoints.documents.base({ corpus, layer: SOURCE_LAYER }), document, { headers: contentType })
}

/**
 * Delete a document.
 * @param corpus Corpus UUID.
 * @param document Document name.
 */
export function deleteDocument(corpus: UUID, document: string): Promise<AxiosResponse> {
    return axios.delete(endpoints.documents.document({ corpus, document, layer: SOURCE_LAYER }))
}

/**
 * Download the uploaded source document.
 * @param corpus Corpus UUID.
 * @param document Document name.
 */
export function getRawDocument(corpus: UUID, document: string): Promise<BlobResponse> {
    return getBlob(endpoints.documents.download({ corpus, document, layer: SOURCE_LAYER }))
}
