// Libraries & stores
import { AxiosResponse } from "axios"
import { Ref } from "vue"
import { CorporaStore, JobSelectionStore } from "@/stores"
// API & types
import { UUID } from '@/types/corpora'

/**
 * Reloads the data for an evaluation store.
 * @param ApiCall API to call for data.
 * @param ResetCall Reset data call.
 * @param intent Human readable intent, on error.
 * @param loading Loading ref.
 * @param data Data ref.
 * @param stores Global stores object.
 * @param corpus UUID of the corpus.
 * @param hypothesis Tagger job name as hypothesis layer.
 * @param reference Tagger job name as reference layer.
 */
export function reloadEval(
    ApiCall: (corpus: UUID, hypothesis: string, reference: string) => Promise<AxiosResponse>,
    ResetCall: () => void,
    intent: string,
    loading: Ref<boolean>,
    data: Ref<any>,
    stores: any,
    corpus: UUID,
    hypothesis: string,
    reference?: string,
): any {
    // Specifically check reference for null. We'll allow empty strings.
    if (!corpus || !hypothesis || reference == null) {
        ResetCall()
        return
    }
    // Is already fetching
    if (loading.value) return
    // Else, start fetching
    loading.value = true

    return new Promise<void>((resolve, reject) => {
        ApiCall(corpus, hypothesis, reference)
            .then(response => {
                const corporaStore = stores.useCorpora() as CorporaStore
                const jobSelection = stores.useJobSelection() as JobSelectionStore
                // Retrieve latest selections
                const currentCorpus = corporaStore.activeUUID
                const currentHypothesis = jobSelection.hypothesisJobId
                const currentReference = jobSelection.referenceJobId
                // Only commit the response if it corresponds to the current corpus and layers
                // This prevents late responses overwriting responses to newer requests
                const url: string = response.request.responseURL
                // Distribution does not need a reference and instead passes an empty string.
                const includesRef = reference == "" ? true : url.includes(currentReference)

                if (url.includes(currentCorpus) && url.includes(currentHypothesis)
                    && includesRef) {
                    // commit
                    data.value = response.data
                    resolve()
                }
            })
            .catch(error => { 
                stores.useApp().handleServerError(intent, error)
                reject()
            })
            .finally(() => loading.value = false)
    })
}