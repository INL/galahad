/**
 * API calls for fetching documents for a corpus, uploading and deleting documents, 
 * and downloading the uploaded source document.
 */

// Libraries & stores
import axios, { AxiosResponse } from "axios"
// Types & API
import { UUID } from "@/types/corpora"
import { DocumentMetadata } from "@/types/documents"
import * as Utils from "@/api/utils"
import { BlobResponse } from "@/api/utils"

// Paths
const documentsPath = (corpus: UUID) => `/corpora/${corpus}/documents`
const documentPath = (corpus: UUID, document: string) => `${documentsPath(corpus)}/${document}`
const rawDocumentPath = (corpus: UUID, document: string) => `${documentPath(corpus, document)}/raw`

// Custom types
type DocumentsResponse = AxiosResponse<DocumentMetadata[]>
type DocumentResponse = AxiosResponse<DocumentMetadata>

// Public methods
/**
 * Fetch all documents for a corpus.
 * @param corpus UUID of the corpus.
 */
export function getDocuments(corpus: UUID): Promise<DocumentsResponse> {
    return axios.get(documentsPath(corpus))
}

// unused
export function getDocument(corpus: UUID, document: string): Promise<DocumentResponse> {
    return axios.get(documentPath(corpus, document))
}

/**
 * Upload new document.
 * @param corpus UUID of the corpus.
 * @param document Document name.
 * @param contentType Content type of the document. Must be explicitly set for tsv-files. Others are induced.
 */
export function postDocument(corpus: UUID, document: FormData, contentType?: any) {
    return axios.post(documentsPath(corpus), document, { headers: contentType })
}

/**
 * Delete uploaded document.
 * @param corpus UUID of the corpus.
 * @param document Document name.
 */
export function deleteDocument(corpus: UUID, document: string) {
    return axios.delete(documentPath(corpus, document))
}

// unused
export function patchDocument(corpus: UUID, document: string, documentData: DocumentMetadata) {
    return axios.patch(documentPath(corpus, document), documentData)
}

/**
 * Download the uploaded source document.
 * @param corpus UUID of the corpus.
 * @param document Document name.
 */
export function getRawDocument(corpus: UUID, document: string): Promise<BlobResponse> {
    return Utils.getBlob(rawDocumentPath(corpus, document))
}
