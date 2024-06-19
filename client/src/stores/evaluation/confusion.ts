// Libraries & stores
import { ref } from 'vue'
import { defineStore } from 'pinia'
import stores from '@/stores'
// API & types
import { UUID } from '@/types/corpora'
import { ConfusionWrapper } from '@/types/evaluation'
import * as API from '@/api/evaluation'
import * as Utils from '@/stores/evaluation/utils'

// Allows for Object.keys(confusion.table), which dislikes null.
const defaultConfusion = () => ({ table: {} } as ConfusionWrapper)

/**
 * Stores and fetches the confusion matrix.
 */
const confusion = defineStore('confusion', () => {
    // Fields
    const loading = ref(false)
    const confusion = ref(defaultConfusion())

    // Methods
    /**
     * Reset it when e.g. the hypothesis or reference is changed.
     */
    function reset() {
        confusion.value = defaultConfusion()
    }

    /**
     * Reloads the confusion matrix for the given corpus, hypothesis and reference.
     * @param corpus The UUID of the corpus.
     * @param hypothesis Tagger job name as hypothesis layer.
     * @param reference Tagger job name as reference layer.
     */
    function reloadForUUIDHypothesisReference(corpus: UUID, hypothesis: string, reference: string) {
        Utils.reloadEval(
            API.getConfusion,
            reset,
            "fetch confusion",
            loading,
            confusion,
            stores,
            corpus,
            hypothesis,
            reference
        ).then(() => {
            if (Object.keys(confusion?.value?.table).length == 0) {
                return
            }
        })
    }

    // Exports
    return {
        // Fields
        confusion, loading,
        // Methods
        reloadForUUIDHypothesisReference, reset,
    }
})

export default confusion