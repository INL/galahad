import type { LayerPreview } from "@/types/layers"

export const Format = {
    TEI_P4_LEGACY: "tei-p4-legacy",
    TEI_P5_LEGACY: "tei-p5-legacy",
    TEI_P5: "tei-p5",
    NAF: "naf",
    FOLIA: "folia",
    TSV: "tsv",
    TXT: "txt",
    CONLLU: "conllu",
    JSON: "json",
} as const
export type Format = (typeof Format)[keyof typeof Format]

export type AnnotationsSummary = Record<string, number>
export type StructureSummary = Record<string, number>

export type DocumentMetadata = {
    name: string
    format: Format
    text: string
    preview: LayerPreview
    annotations: AnnotationsSummary
    structure: StructureSummary
    modified: number
}
