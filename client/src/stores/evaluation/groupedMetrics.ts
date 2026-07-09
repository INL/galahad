import * as API from "@/api/evaluation/metrics"
import { plausible } from "@/ts/plausible"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"
import type { Metrics } from "@/types/evaluation/metrics"

/** Stores and fetches the type token distribution. */
const useGroupedMetrics = defineStore("groupedMetrics", () => {
    const { hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const loading = ref<boolean>(false)
    const groupedMetrics = ref<Metrics>()
    const annotations = ref<string[]>()
    const group = ref<string>()
    const analysis = ref<string>()

    function reload(): void {
        if ([corpusId.value, hypothesisId.value, referenceId.value, group.value].includes(undefined)) return
        if (!annotations.value?.length) return
        loading.value = true
        API.getGroupedMetrics(
            corpusId.value,
            hypothesisId.value,
            referenceId.value,
            annotations.value,
            group.value,
            analysis.value,
        )
            .then((res) => (groupedMetrics.value = res.data))
            .then(() => {
                plausible.evaluation.groupedMetrics(
                    corpus.value,
                    hypothesisLayer.value,
                    referenceLayer.value,
                    annotations.value,
                    group.value,
                    analysis.value,
                )
            })
            .finally(() => (loading.value = false))
    }

    watch([corpusId, hypothesisId, referenceId], () => {
        groupedMetrics.value = undefined
        annotations.value = undefined
        group.value = undefined
        analysis.value = undefined
    })
    watch([annotations, group, analysis], reload)

    return { reload, loading, groupedMetrics, annotations, group, analysis }
})

export default useGroupedMetrics
