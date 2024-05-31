// Libraries & stores
import { ref } from 'vue'
import { defineStore } from 'pinia'
import stores from '@/stores'
// API & types
import { UUID } from '@/types/corpora'
import { Metrics, MetricsRow } from '@/types/evaluation'
import * as API from '@/api/evaluation'
import * as Utils from '@/stores/evaluation/utils'

export const metricsPerPosColumns = [
    { key: 'name', label: 'group', sortOn: (x: MetricsRow) => x.name },
    // { key: 'accuracy', sortOn: (x: MetricsRow) => x.accuracy },
    { key: 'precision', sortOn: (x: MetricsRow) => x.precision },
    { key: 'recall', sortOn: (x: MetricsRow) => x.recall },
    { key: 'f1', sortOn: (x: MetricsRow) => x.f1 },
    { key: 'count', label: 'count', sortOn: (x: MetricsRow) => x.count },
    { key: 'truePositive', label: 'true positive', sortOn: (x: MetricsRow) => x.truePositive.count / x.count },
    { key: 'falsePositive', label: 'false positive', sortOn: (x: MetricsRow) => x.falsePositive.count / x.count },
    { key: 'falseNegative', label: 'false negative', sortOn: (x: MetricsRow) => x.falseNegative.count / x.count },
    { key: 'noMatch', label: "no match", sortOn: (x: MetricsRow) => x.noMatch.count / x.count },
]

/**
 * Stores and fetches the Lemma & PoS accuracy metrics.
 */
const useMetrics = defineStore('metrics', () => {
    // Fields
    const loading = ref(false)
    const metrics = ref({} as Metrics)

    // Methods
    /**
     * Reset it when e.g. the hypothesis or reference is changed.
     */
    function reset() {
        metrics.value = {} as Metrics
    }

    /**
     * Reloads the metrics for the given corpus, hypothesis and reference.
     * @param corpus The UUID of the corpus.
     * @param hypothesis Tagger job name as hypothesis layer.
     * @param reference Tagger job name as reference layer.
     */
    function reloadForUUIDHypothesisReference(corpus: UUID, hypothesis: string, reference: string) {
        Utils.reloadEval(
            API.getMetrics,
            reset,
            "fetch metrics",
            loading,
            metrics,
            stores,
            corpus,
            hypothesis,
            reference
        )
    }

    // Exports
    return {
        // Fields
        loading, metrics,
        // Methods
        reloadForUUIDHypothesisReference, reset,
    }
})

export default useMetrics