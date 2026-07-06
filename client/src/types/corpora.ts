import type { Period } from "@/types/taggers"

export type UUID = string

export type CorpusMetadata = MutableCorpusMetadata & {
    uuid: UUID
    documents: number
    annotations: Record<string, number>
    jobs: number
    processing: number
    size: number
    modified: number
}

export type Source = { name: string; url: string }

export type MutableCorpusMetadata = {
    name: string
    owner: string
    dataset: boolean
    period: Period
    language: string
    tagset: string
    source: Source
    collaborators: string[]
    viewers: string[]
}
