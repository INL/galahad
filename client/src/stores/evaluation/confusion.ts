import type { Confusion } from "@/types/evaluation/confusion"
import * as API from "@/api/evaluation/confusion"
import { plausible } from "@/ts/plausible"
import useLayers from "@/stores/layers"
import useCorpora from "@/stores/corpora"

/** Stores and fetches the confusion matrix. */
const useConfusion = defineStore("confusion", () => {
    const { hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const loading = ref<boolean>(false)
    const confusion = ref<Confusion>()
    const annotation = ref<string>()

    function reload(): void {
        if ([corpusId.value, hypothesisId.value, referenceId.value, annotation.value].includes(undefined)) return
        plausible.confusionEvaluated(corpus.value, hypothesisLayer.value, referenceLayer.value)
        loading.value = true
        API.getConfusion(corpusId.value, hypothesisId.value, referenceId.value, annotation.value)
            .then((res) => (confusion.value = res.data))
            .finally(() => (loading.value = false))
    }

    watch([corpusId, hypothesisId, referenceId], () => {
        confusion.value = undefined
        annotation.value = undefined
        reload()
    })
    watch(annotation, reload)

    return { reload, confusion, loading, annotation }
})

export default useConfusion
