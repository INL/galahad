import type { Tagger } from "@/types/taggers"
import type { Term } from "@/types/annotation"
import type { AnnotationsSummary, StructureSummary } from "@/types/documents"

export type LayerPreview = { terms: Term[] }

export type LayerMetadata = {
    tagger: Tagger
    documents: number
    preview: LayerPreview
    annotations: AnnotationsSummary
    structure: StructureSummary
    modified: number
}
