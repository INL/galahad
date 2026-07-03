import * as API from "@/api/evaluation/metrics"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"
import type { UUID } from "@/types/corpora"
import type { GlobalMetrics } from "@/types/evaluation/metrics"

const useBenchmarks = defineStore("benchmarks", () => {
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const loading = ref<boolean>(false)
    const benchmarks = ref<GlobalMetrics[]>()
    const annotations = ref<string[]>()
    const group = ref<string>()
    const analysis = ref<string>()

    function reload(): void {
        if ([corpusId.value, group.value, analysis.value].includes(undefined)) return
        if (!annotations.value?.length) return
        if (!corpus.value?.dataset) return
        loading.value = true
        API.getCorpusMetrics(corpusId.value, annotations.value, group.value, analysis.value)
            .then((res) => (benchmarks.value = res.data))
            .finally(() => (loading.value = false))
    }

    watch(corpusId, () => {
        benchmarks.value = undefined
        annotations.value = undefined
        group.value = undefined
        analysis.value = undefined
    })
    watch([corpusId, corpus, annotations, group, analysis], reload)

    return { loading, benchmarks, annotations, group, analysis }
})

export default useBenchmarks
