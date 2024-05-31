/**
 * API calls for exporting corpora and documents. 
 * Either converted to a certain format or merged with their original file if the format supports it.
 */

// Types & API
import * as Utils from "@/api/utils"
import { BlobResponse } from "@/api/utils"
import { Format } from "@/types/documents"
import { UUID } from "@/types/corpora"

// Paths
const convertCorpusPath = (corpus: UUID, job: string, format: Format, posHeadOnly: Boolean) => {
    return `/corpora/${corpus}/jobs/${job}/export/convert?format=${format}&posHeadOnly=${posHeadOnly}`
}
const mergeCorpusPath = (corpus: UUID, job: string, format: Format, posHeadOnly: Boolean) => {
    return `/corpora/${corpus}/jobs/${job}/export/merge?format=${format}&posHeadOnly=${posHeadOnly}`
}
const convertDocumentPath = (corpus: UUID, job: string, document: string, format: Format) => {
    return `/corpora/${corpus}/jobs/${job}/documents/${document}/export/convert?format=${format}`
}
const mergeDocumentPath = (corpus: UUID, job: string, document: string) => {
    return `/corpora/${corpus}/jobs/${job}/documents/${document}/export/merge`
}

// Public methods
/**
 * Download a corpus converted to the desired format.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param format Use enum here.
 * @param posHeadOnly Whether to include only the head of the POS tags in the export.
 */
export function convertCorpus(corpus: UUID, job: string, format: Format, posHeadOnly: boolean): Promise<BlobResponse> {
    return Utils.getBlob(convertCorpusPath(corpus, job, format, posHeadOnly))
}

/**
 * Download a corpus converted to the desired format, merging any files of that format.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param format Use enum here.
 * @param posHeadOnly Whether to include only the head of the POS tags in the export.
 */
export function mergeCorpus(corpus: UUID, job: string, format: Format, posHeadOnly: boolean): Promise<BlobResponse> {
    return Utils.getBlob(mergeCorpusPath(corpus, job, format, posHeadOnly))
}

/**
 * Download a single document from a job convert to the desired format.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param document Document name.
 * @param format Use enum here.
 */
// Currently unused
export function convertDocument(corpus: UUID, job: string, document: string, format: Format): Promise<BlobResponse> {
    return Utils.getBlob(convertDocumentPath(corpus, job, document, format))
}

/**
 * Download a single document from a job convert to the desired format, 
 * merging it if the original file is of that format.
 * @param corpus UUID of the corpus.
 * @param job Tagger job name.
 * @param document Document name.
 * @param format Use enum here.
 */
// Currently unused
export function mergeDocument(corpus: UUID, job: string, document: string): Promise<BlobResponse> {
    return Utils.getBlob(mergeDocumentPath(corpus, job, document))
}
