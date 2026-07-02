import * as API from "@/api/layers"
import type { LayerMetadata } from "@/types/layers"
import useCorpora from "@/stores/corpora"
import { useRouteQuery } from "@vueuse/router"
import type { SelectOption } from "@/types/ui/select"
import { SOURCE_LAYER, type Job } from "@/types/jobs"
import { formatLayer } from "@/ts/format"

/** Contains the layers for the current corpus. */
const useLayers = defineStore("layers", () => {
    // Stores
    const { corpusId } = storeToRefs(useCorpora())

    // Fields
    const loading = ref<boolean>()
    const layers = ref<LayerMetadata[]>([])
    const hypothesisId = useRouteQuery<string | undefined>("hypothesis")
    const referenceId = useRouteQuery<string | undefined>("reference")

    // Computed
    const sourceLayer = computed<LayerMetadata | undefined>(() =>
        layers.value.find((l: LayerMetadata) => l.tagger.name === SOURCE_LAYER),
    )
    const hypothesisLayer = computed<LayerMetadata | undefined>(() =>
        layers.value.find((l: LayerMetadata) => l.tagger.name === hypothesisId.value),
    )
    const referenceLayer = computed<LayerMetadata | undefined>(() =>
        layers.value.find((l: LayerMetadata) => l.tagger.name === referenceId.value),
    )
    const options = computed<SelectOption[]>((): SelectOption[] =>
        layers.value.map((l: LayerMetadata) => ({ value: l.tagger.name, text: formatLayer(l) })),
    )
    const sourceAnnotations = computed<SelectOption[]>((): SelectOption[] =>
        Object.keys(sourceLayer.value?.annotations ?? {}).map((s) => ({ value: s, text: s })),
    )
    const hypothesisAnnotations = computed<SelectOption[]>((): SelectOption[] =>
        Object.keys(hypothesisLayer.value?.annotations ?? {}).map((s) => ({ value: s, text: s })),
    )
    const referenceAnnotations = computed<SelectOption[]>((): SelectOption[] =>
        Object.keys(referenceLayer.value?.annotations ?? {}).map((s) => ({ value: s, text: s })),
    )
    const commonAnnotations = computed<SelectOption[]>((): SelectOption[] => {
        const h = new Set(hypothesisAnnotations.value.map((o) => o.value))
        const r = new Set(referenceAnnotations.value.map((o) => o.value))
        return [...h.intersection(r)].map((s) => ({ value: s, text: s }))
    })

    /** Reload layers */
    function reload(): void {
        if (!corpusId.value) return
        loading.value = true
        API.getLayers(corpusId.value)
            .then((res) => (layers.value = res.data))
            .finally(() => {
                loading.value = false
                referenceId.value ??= "source"
            })
    }

    function resetSelection(): void {
        hypothesisId.value = undefined
        referenceId.value = undefined
    }

    // Reset on corpus selection
    watch(corpusId, resetSelection)

    watch(corpusId, reload)

    // Exports
    return {
        // Fields
        layers,
        loading,
        sourceLayer,
        hypothesisId,
        referenceId,
        hypothesisLayer,
        referenceLayer,
        sourceAnnotations,
        hypothesisAnnotations,
        referenceAnnotations,
        commonAnnotations,
        options,
        // Methods
        reload,
        resetSelection,
    }
})

export default useLayers
