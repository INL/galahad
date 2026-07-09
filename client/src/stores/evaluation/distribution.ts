import type { TypeToken } from "@/types/evaluation/distribution"
import * as API from "@/api/evaluation/distribution"
import { plausible } from "@/ts/plausible"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"

/** Stores and fetches the type token distribution. */
const useDistribution = defineStore("distribution", () => {
    const { hypothesisId, hypothesisLayer } = storeToRefs(useLayers())
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const loading = ref<boolean>(false)
    const distribution = ref<TypeToken[]>()
    const annotation = ref<string>()
    const group = ref<string>()

    function reload(): void {
        if ([corpusId.value, hypothesisId.value, annotation.value, group.value].includes(undefined)) return
        loading.value = true
        API.getDistribution(corpusId.value, hypothesisId.value, annotation.value, group.value)
            .then((res) => (distribution.value = res.data))
            .then(() => {
                plausible.evaluation.distribution(corpus.value, hypothesisLayer.value, annotation.value, group.value)
            })
            .finally(() => (loading.value = false))
    }

    watch([corpusId, hypothesisId], () => {
        distribution.value = undefined
        annotation.value = undefined
        group.value = undefined
    })
    watch([annotation, group], reload)

    return { reload, loading, distribution, annotation, group }
})

export default useDistribution
