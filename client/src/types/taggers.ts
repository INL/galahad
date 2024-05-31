export type Tagger = {
    id: string
    description: string
    tagset: null | string
    eraFrom: number
    eraTo: number
    produces: string[]
    attributions: null | {
        [key: string]: string
    }
    links: null | {
        name: string
        href: string
    }[]
}

export enum TaggerStatus {
    HEALTHY = 'HEALTHY',
    ERROR = 'ERROR',
    NOT_HEALTHY = 'NOT_HEALTHY',
    UNKOWN = 'UNKOWN'
}

export type TaggerHealth = {
    status: TaggerStatus
    queueSizeAtTagger: number
    processingSpeed: number
    message: string
}
