export type UUID = string

export type CorpusMetadata = MutableCorpusMetadata & {
    "activeJobs": number
    "dataset": boolean
    "lastModified": number
    "numDocs": number
    "public": boolean
    "sizeInBytes": number
    "uuid": UUID
}

export type MutableCorpusMetadata = {
    "name": string
    "owner": string
    "eraFrom": number
    "eraTo": number
    "tagset": string
    "dataset": boolean
    "public": boolean
    "collaborators": string[]
    "viewers": string[]
    "sourceName": string
    "sourceURL": string
}
