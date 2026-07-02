import type { CorpusMetadata, MutableCorpusMetadata } from "@/types/corpora"
import type { DocumentMetadata, Format } from "@/types/documents"
import type { Job } from "@/types/jobs"
import type { LayerMetadata } from "@/types/layers"
import { formatPeriod } from "@/ts/format"

declare global {
    interface Window {
        plausible: (eventName: string, props?: Record<string, any>) => void
    }
}

enum LayerType {
    Hypothesis = "hypothesis",
    Reference = "reference",
}

// debug printing for plausible
if (location.hostname.includes("localhost")) {
    window.plausible = (eventName: string, props?: Record<string, any>): void =>
        console.log(`localhost plausible event: ${eventName}\nparams: ${JSON.stringify(props)}`)
}

function corpusParams(corpus: CorpusMetadata): Record<string, number | string | boolean> {
    return {
        shared: corpus.dataset ? "dataset" : (corpus.collaborators?.length ?? 0) + (corpus.viewers?.length ?? 0),
        period: formatPeriod(corpus.period),
        language: corpus.language,
        source: Boolean(corpus.source),
        numDocs: corpus.numDocs,
    }
}

function docParams(doc: DocumentMetadata): Record<string, string> {
    return { format: doc.format, annotations: Object.keys(doc.annotations).join() }
}

function jobParams(job: Job): Record<string, any> {
    return {
        [`tagger-name`]: job.tagger.name,
        [`tagger-annotations`]: job.tagger.annotations.map((a) => a.annotation).join(),
    }
}

function layerParams(layer: LayerMetadata, type: LayerType): Record<string, any> {
    return {
        [`${type}-name`]: layer.tagger.name,
        // TODO should be keys of layer.annotations?
        [`${type}-annotations`]: layer.tagger.annotations.map((a) => a.annotation).join(),
    }
}

function corpusDocParams(corpus: CorpusMetadata, doc: DocumentMetadata): Record<string, string | number | boolean> {
    return { ...docParams(doc), ...corpusParams(corpus) }
}

export const plausible = {
    corpusCreated(corpus: CorpusMetadata): void {
        window.plausible("corpus-created", { props: corpusParams(corpus) })
    },
    corpusDeleted(corpus: CorpusMetadata): void {
        window.plausible("corpus-deleted", { props: corpusParams(corpus) })
    },
    corpusUpdated(corpus: CorpusMetadata): void {
        window.plausible("corpus-updated", { props: corpusParams(corpus) })
    },
    documentDownloaded(corpus: CorpusMetadata, doc: DocumentMetadata): void {
        window.plausible("document-downloaded", { props: corpusDocParams(corpus, doc) })
    },
    documentDeleted(corpus: CorpusMetadata, doc: DocumentMetadata): void {
        window.plausible("document-deleted", { props: corpusDocParams(corpus, doc) })
    },
    documentUploaded(corpus: CorpusMetadata, fileExtension: string): void {
        const props = { format: fileExtension, ...corpusParams(corpus) }
        window.plausible("document-uploaded", { props })
    },
    corpusExported(
        corpus: CorpusMetadata,
        layer: LayerMetadata,
        format: Format,
        merged: boolean,
        headOnly: boolean,
    ): void {
        const props = { format, merged, headOnly, ...corpusParams(corpus), ...layerParams(layer, LayerType.Hypothesis) }
        window.plausible("corpus-exported", { props })
    },
    helpClicked(): void {
        const props = { url: location.pathname }
        window.plausible("help-clicked", { props })
    },
    distributionEvaluated(corpus: CorpusMetadata, hypothesisLayer: LayerMetadata): void {
        const props = { ...layerParams(hypothesisLayer, LayerType.Hypothesis), ...corpusParams(corpus) }
        window.plausible("distribution-evaluated", { props })
    },
    confusionEvaluated(corpus: CorpusMetadata, hypothesisLayer: LayerMetadata, referenceLayer: LayerMetadata): void {
        const props = {
            ...layerParams(hypothesisLayer, LayerType.Hypothesis),
            ...layerParams(referenceLayer, LayerType.Reference),
            ...corpusParams(corpus),
        }
        window.plausible("confusion-evaluated", { props })
    },
    metricsEvaluated(corpus: CorpusMetadata, hypothesisLayer: LayerMetadata, referenceLayer: LayerMetadata): void {
        const props = {
            ...layerParams(hypothesisLayer, LayerType.Hypothesis),
            ...layerParams(referenceLayer, LayerType.Reference),
            ...corpusParams(corpus),
        }
        window.plausible("metrics-evaluated", { props })
    },
    evaluationDownloaded(corpus: CorpusMetadata, hypothesisLayer: LayerMetadata, referenceLayer: LayerMetadata): void {
        const props = {
            ...layerParams(hypothesisLayer, LayerType.Hypothesis),
            ...layerParams(referenceLayer, LayerType.Reference),
            ...corpusParams(corpus),
        }
        window.plausible("evaluation-downloaded", { props })
    },
    jobStarted(corpus: CorpusMetadata, taggerJob: Job): void {
        const props = { ...jobParams(taggerJob), ...corpusParams(corpus) }
        window.plausible("job-started", { props })
    },
    jobDeleted(corpus: CorpusMetadata, taggerJob: Job): void {
        const props = { ...jobParams(taggerJob), ...corpusParams(corpus) }
        window.plausible("job-deleted", { props })
    },
    jobStopped(corpus: CorpusMetadata, taggerJob: Job): void {
        const props = { ...jobParams(taggerJob), ...corpusParams(corpus) }
        window.plausible("job-stopped", { props })
    },
}
