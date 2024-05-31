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

export const MISC = "OTHER"
const MISC_REGEX = /^[^A-Z]/

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
            combineMisc()
        })
    }

    /**
     * Combine any pos that start with [^A-Z] into a misc category.
     */
    function combineMisc() {
        // The confusion matrix has the structure:
        // { AA: { AA: {...}, ADP: {...} } } 
        // So we need to merge on two levels, starting with the inner level

        // Inner level
        for (const pos in confusion.value.table) {
            for (const pos2 in confusion.value.table[pos]) {
                if (MISC_REGEX.test(pos2)) {
                    // Create MISC if not yet present: e.g. ADP: { MISC: {...} }
                    if (!confusion.value.table[pos][MISC]) {
                        confusion.value.table[pos][MISC] = { "count": 0, "samples": [] }
                    }
                    // Merge
                    const pos_to_pos = confusion.value.table[pos][pos2]
                    confusion.value.table[pos][MISC].count += pos_to_pos.count
                    confusion.value.table[pos][MISC].samples.push(...pos_to_pos.samples)
                    delete confusion.value.table[pos][pos2]
                }
            }
        }


        // Outer level
        const misc: Record<string, any> = { }

        for (const pos in confusion.value.table) {
            if (MISC_REGEX.test(pos)) {
                for (const pos2 in confusion.value.table[pos]) {
                    // create misc[pos2] if not yet present
                    if (!misc[pos2]) {
                        misc[pos2] = { "count": 0, "samples": [] }
                    }
                    // Merge
                    const pos_to_pos = confusion.value.table[pos][pos2]
                    misc[pos2].count += pos_to_pos.count
                    misc[pos2].samples.push(...pos_to_pos.samples)
                }
                delete confusion.value.table[pos]
            }
        }

        // Commit the matches if there were any
        if (Object.keys(misc).length > 0) {
            confusion.value.table[MISC] = misc
        }
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