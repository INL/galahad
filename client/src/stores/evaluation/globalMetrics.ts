import * as API from "@/api/evaluation/metrics"
import { plausible } from "@/ts/plausible"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"
import type { Metrics } from "@/types/evaluation/metrics"

const useGlobalMetrics = defineStore("globalMetrics", () => {
    const { hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const loading = ref<boolean>(false)
    const globalMetrics = ref<Metrics[]>()
    const annotations = ref<string[]>()
    const group = ref<string>()

    function reload(): void {
        if ([corpusId.value, hypothesisId.value, referenceId.value, annotations.value, group.value].includes(undefined))
            return
        plausible.metricsEvaluated(corpus.value, hypothesisLayer.value, referenceLayer.value)
        loading.value = true
        API.getGroupedMetrics(corpusId.value, hypothesisId.value, referenceId.value, annotations.value, group.value)
            .then((res) => (groupedMetrics.value = res.data))
            .finally(() => (loading.value = false))
    }

    watch([corpusId, hypothesisId, referenceId], () => {
        groupedMetrics.value = undefined
        annotations.value = []
        group.value = undefined
    })
    watch([annotations, group], reload)

    return { reload, loading, groupedMetrics, annotations, group }
})

export default useGlobalMetrics
