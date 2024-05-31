export enum Format {
    Tei_p4_legacy = "tei-p4-legacy",
    Tei_p5_legacy = "tei-p5-legacy",
    Tei_p5 = "tei-p5",
    Naf = "naf",
    Folia = "folia",
    Tsv = "tsv",
    Txt = "txt",
    Conllu = "conllu",
}

export type LayerSummary = {
    numLemma: number
    numPOS: number
    numTerms: number
    numWordForms: number
}

export type DocumentMetadata = {
    name: string
    format: Format
    valid: boolean
    sizeInBytes: number
    numChars: number
    numAlphabeticChars: number
    preview: string
    layerSummary: LayerSummary
    lastModified: number
}
