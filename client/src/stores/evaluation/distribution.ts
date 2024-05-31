// Libraries & stores
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import stores from '@/stores'
// API & types
import { UUID } from '@/types/corpora'
import { DistributionWrapper } from '@/types/evaluation'
import * as API from '@/api/evaluation'
import * as Utils from '@/stores/evaluation/utils'

// Allows for Object.keys(distribution.table), which dislikes null.
const defaultDistribution = () => ({ distribution: {} } as DistributionWrapper)

/**
 * Stores and fetches the term frequency distribution.
 */
const useDistribution = defineStore('distribution', () => {
    // Fields
    const distribution = ref(defaultDistribution())
    const loading = ref(false)
    const posses = computed(() => {
        // A bit hacky using Object.entries, but .map throws on undefined.
        return Object.entries(distribution.value?.distribution)?.map(x => x[1].pos)
            .filter((val, ind, arr) => arr.indexOf(val) === ind) // unique values
            .sort()
    })

    // Methods
    /**
     * Reset it when e.g. the hypothesis or reference is changed.
     */
    function reset() {
        distribution.value = defaultDistribution()
    }

    /**
     * Reloads the term frequency distribution for the given corpus, hypothesis and reference.
     * @param corpus The UUID of the corpus.
     * @param hypothesis The hypothesis job ID.
     */
    function reloadForUUIDHypothesis(corpus: UUID, hypothesis: string) {
        Utils.reloadEval(
            API.getDistribution,
            reset,
            "fetch distribution",
            loading,
            distribution,
            stores,
            corpus,
            hypothesis,
            ""
        )
    }

    // Exports
    return {
        // Fields
        loading, distribution, posses,
        // Methods
        reloadForUUIDHypothesis, reset,
    }
})

export default useDistribution