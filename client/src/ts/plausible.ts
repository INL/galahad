import type { CorpusMetadata, MutableCorpusMetadata } from "@/types/corpora"
import type { DocumentMetadata, Format } from "@/types/documents"
import type { Job } from "@/types/jobs"
import type { LayerMetadata } from "@/types/layers"
import { formatPeriod } from "@/ts/format"
import useTaggers from "@/stores/static/taggers"
import type { Tagger } from "@/types/taggers"

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

/**
 * Round a number to the nearest base ten.
 * Example: 49 -> 10; 50 -> 100
 */
function roundBaseTen(n: number): number {
    const p = 10 ** Math.floor(Math.log10(n))
    return n / p < 5 ? p : p * 10
}

/** Extract corpus metadata for plausible. */
function corpusParams(corpus: CorpusMetadata | MutableCorpusMetadata): Record<string, any> {
    return {
        [`corpus-shared`]: corpus.dataset
            ? "dataset"
            : (corpus.collaborators?.length ?? 0) + (corpus.viewers?.length ?? 0),
        [`corpus-period`]: formatPeriod(corpus.period),
        [`corpus-language`]: corpus.language,
        [`corpus-source`]: Boolean(corpus.source),
        [`corpus-documents`]: corpus.documents,
        [`corpus-tokens`]: Boolean(corpus.annotations) ? roundBaseTen(corpus.annotations.token) : undefined,
        // TODO maybe also add number of layers (not just jobs)
    }
}

/** Extract document metadata for plausible. */
function docParams(doc: DocumentMetadata): Record<string, any> {
    return {
        [`document-format`]: doc.format,
        [`document-annotations`]: Object.keys(doc.annotations).join(),
        [`document-tokens`]: roundBaseTen(doc.annotations.token),
    }
}

/** Extract job metadata for plausible. */
function jobParams(job: Job): Record<string, any> {
    return {
        [`tagger-name`]: job.tagger.name,
        [`tagger-language`]: job.tagger.language,
        [`tagger-period`]: formatPeriod(job.tagger.period),
        [`tagger-annotations`]: job.tagger.annotations.map((a) => a.annotation).join(),
    }
}

/** Extract layer metadata for plausible. */
function layerParams(layer: LayerMetadata, type: LayerType): Record<string, any> {
    return { [`${type}-name`]: layer.tagger.name, [`${type}-annotations`]: Object.keys(layer.annotations).join() }
}

/** Extract metadata shared by evaluations for plausible. */
function evaluationParams(
    corpus: CorpusMetadata,
    hypothesisLayer: LayerMetadata,
    referenceLayer: LayerMetadata,
): Record<string, any> {
    return {
        ...corpusParams(corpus),
        ...layerParams(hypothesisLayer, LayerType.Hypothesis),
        ...layerParams(referenceLayer, LayerType.Reference),
    }
}

/** Extract corpus & document metadata for plausible. */
function corpusDocParams(corpus: CorpusMetadata, doc: DocumentMetadata): Record<string, any> {
    return { ...docParams(doc), ...corpusParams(corpus) }
}

export const plausible = {
    corpus: {
        created(corpus: CorpusMetadata): void {
            window.plausible("corpus-created", { props: corpusParams(corpus) })
        },
        deleted(corpus: CorpusMetadata): void {
            window.plausible("corpus-deleted", { props: corpusParams(corpus) })
        },
        updated(corpus: CorpusMetadata): void {
            window.plausible("corpus-updated", { props: corpusParams(corpus) })
        },
    },
    document: {
        downloaded(corpus: CorpusMetadata, doc: DocumentMetadata): void {
            window.plausible("document-downloaded", { props: corpusDocParams(corpus, doc) })
        },
        deleted(corpus: CorpusMetadata, doc: DocumentMetadata): void {
            window.plausible("document-deleted", { props: corpusDocParams(corpus, doc) })
        },
        uploaded(corpus: CorpusMetadata, fileExtension: string): void {
            const props = { [`document-format`]: fileExtension, ...corpusParams(corpus) }
            window.plausible("document-uploaded", { props })
        },
    },
    export: {
        exported(
            corpus: CorpusMetadata,
            layer: LayerMetadata,
            format: Format,
            merged: boolean,
            headOnly: boolean,
        ): void {
            const props = {
                [`export-format`]: format,
                [`export-merged`]: merged,
                [`export-main-pos`]: headOnly,
                ...corpusParams(corpus),
                ...layerParams(layer, LayerType.Hypothesis),
            }
            window.plausible("corpus-exported", { props })
        },
    },
    help: {
        clicked(): void {
            const props = { [`help-url`]: location.pathname }
            window.plausible("help-clicked", { props })
        },
    },
    evaluation: {
        distribution(corpus: CorpusMetadata, hypothesisLayer: LayerMetadata, annotation: string, group: string): void {
            const props = {
                ["evaluation-annotations"]: annotation,
                ["evaluation-group"]: group,
                ...layerParams(hypothesisLayer, LayerType.Hypothesis),
                ...corpusParams(corpus),
            }
            window.plausible("distribution-evaluated", { props })
        },
        confusion(
            corpus: CorpusMetadata,
            hypothesisLayer: LayerMetadata,
            referenceLayer: LayerMetadata,
            annotation: string,
        ): void {
            const props = {
                ["evaluation-annotations"]: annotation,
                ...evaluationParams(corpus, hypothesisLayer, referenceLayer),
            }
            window.plausible("confusion-evaluated", { props })
        },
        groupedMetrics(
            corpus: CorpusMetadata,
            hypothesisLayer: LayerMetadata,
            referenceLayer: LayerMetadata,
            annotations: string[],
            group: string,
            analysis: string,
        ): void {
            const props = {
                ["evaluation-annotations"]: annotations.join(),
                ["evaluation-group"]: group,
                ["evaluation-analysis"]: analysis,
                ...evaluationParams(corpus, hypothesisLayer, referenceLayer),
            }
            window.plausible("grouped-metrics-evaluated", { props })
        },
        globalMetrics(corpus: CorpusMetadata, hypothesisLayer: LayerMetadata, referenceLayer: LayerMetadata): void {
            window.plausible("global-metrics-evaluated", {
                props: evaluationParams(corpus, hypothesisLayer, referenceLayer),
            })
        },
        corpusMetrics(
            corpus: CorpusMetadata,
            referenceLayer: LayerMetadata,
            annotations: string[],
            group: string,
            analysis: string,
        ): void {
            const props = {
                ["evaluation-annotations"]: annotations.join(),
                ["evaluation-group"]: group,
                ["evaluation-analysis"]: analysis,
                ...corpusParams(corpus),
                ...layerParams(referenceLayer, LayerType.Reference),
            }
            window.plausible("corpus-metrics-evaluated", { props })
        },
        downloaded(corpus: CorpusMetadata, hypothesisLayer: LayerMetadata, referenceLayer: LayerMetadata): void {
            const props = {
                ...layerParams(hypothesisLayer, LayerType.Hypothesis),
                ...layerParams(referenceLayer, LayerType.Reference),
                ...corpusParams(corpus),
            }
            window.plausible("evaluation-downloaded", { props })
        },
    },
    job: {
        started(corpus: CorpusMetadata, taggerJob: Job): void {
            const props = { ...jobParams(taggerJob), ...corpusParams(corpus) }
            window.plausible("job-started", { props })
        },
        stopped(corpus: CorpusMetadata, taggerJob: Job): void {
            const props = { ...jobParams(taggerJob), ...corpusParams(corpus) }
            window.plausible("job-stopped", { props })
        },
        deleted(corpus: CorpusMetadata, taggerJob: Job): void {
            const props = { ...jobParams(taggerJob), ...corpusParams(corpus) }
            window.plausible("job-deleted", { props })
        },
    },
}
