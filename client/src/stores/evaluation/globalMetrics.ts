import * as API from "@/api/evaluation/metrics"
import { plausible } from "@/ts/plausible"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"
import type { GlobalMetrics, Metrics } from "@/types/evaluation/metrics"

const useGlobalMetrics = defineStore("globalMetrics", () => {
    const { hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const loading = ref<boolean>(false)
    const globalMetrics = ref<GlobalMetrics[]>()

    function reload(): void {
        if ([corpusId.value, hypothesisId.value, referenceId.value].includes(undefined)) return
        loading.value = true
        API.getGlobalMetrics(corpusId.value, hypothesisId.value, referenceId.value)
            .then((res) => (globalMetrics.value = res.data))
            .then(() => {
                plausible.evaluation.globalMetrics(corpus.value, hypothesisLayer.value, referenceLayer.value)
            })
            .finally(() => (loading.value = false))
    }
    watch([corpusId, hypothesisId, referenceId], () => {
        globalMetrics.value = undefined
    })
    watch([hypothesisId, referenceId], reload, { immediate: true })

    return { reload, loading, globalMetrics }
})

export default useGlobalMetrics
