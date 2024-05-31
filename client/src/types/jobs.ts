import { Tagset } from "@/types/tagset"
import { Tagger } from "@/types/taggers"
import { Term, WordForm } from "@/types/evaluation"

export const SOURCE_LAYER: string = "sourceLayer"

export type Job = {
    tagger: Tagger,
    progress: Progress,
    preview: LayerPreview,
    lastModified: number | null
}

export type JobDocumentResult = {
    name: string,
    tagset: Tagset,
    preview: LayerPreview,
    summary: LayerSummary,
}

export type Progress = {
    pending: number,
    processing: number,
    failed: number,
    finished: number,
    total: number,
    untagged: number,
    busy: boolean,
    hasError: boolean
    errors: { [document: string]: string }
}

export type LayerPreview = {
    wordforms: WordForm[]
    terms: Term[]
}

export type LayerSummary = {
    tagger: string
    numDocuments: number
    preview: LayerPreview
    numWordForms: number
    numTerms: number
    numLemma: number
    numPOS: number
    lastModified: number
}
