/** API for downloading evaluation zip. */

import { getBlob, type BlobResponse } from "@/api/utils"
import type { UUID } from "@/types/corpora"
import { endpoints } from ".."

/**
 * Download evaluation zip.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference  Tagger job name as reference layer.
 */
export function getDownloadEvaluation(corpus: UUID, hypothesis: string, reference: string): Promise<BlobResponse> {
    return getBlob(endpoints.evaluation.download({ corpus, layer: hypothesis }, { reference }))
}
