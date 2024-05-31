<template>
    <div>
        <MetricsTable title="Grouped Metrics" :loading :columns :items @download="(data) => download(data)"
            :downloading>
            <template #help>
                <p>In PoS Metrics an overview is given of the (dis)agreement for lemma and PoS per part-of-speech. Click
                    on
                    the (dis)agreement value to show a data sample.</p>
            </template>
            <template #prepend v-if="metrics.metrics != null">
                <p style="text-align: center;">
                    <b>
                        Only the 100 most frequent groups are shown.
                    </b>
                </p>
                <MetricsFilter ref="metricsFilter" />
            </template>
        </MetricsTable>

        <EvaluationInfoBox :eval="metrics" />
    </div>
</template>

<script setup lang='ts'>
// Libraries & stores
import { ref, computed } from 'vue'
import { storeToRefs } from 'pinia'
import stores from "@/stores"
// API & types
import { metricsPerPosColumns } from '@/stores/evaluation/metrics'
import * as API from '@/api/evaluation'
import * as Utils from "@/api/utils"
// Components
import { EvaluationInfoBox } from '@/components'
import MetricsTable from '@/components/tables/MetricsTable.vue'
import MetricsFilter from '@/components/tables/MetricsFilter.vue'

// Stores
const { loading, metrics } = storeToRefs(stores.useMetrics())
const corporaStore = stores.useCorpora()
const jobSelection = stores.useJobSelection()

// Fields
const downloading = ref(false)

const columns = computed(() => metricsPerPosColumns)
const metricsFilter = ref(null)
const metricName = computed(() => {
    return metricsFilter.value?.metricName
})

const posMetrics = computed(() => {
    if (metrics.value?.metrics?.[metricName.value] == null) return []
    // Copy over the metrics (depending on selectedMetric.value) from: 
    // { ADJ: { ADJ: { pos : { f1, recall, ... }, lemma : { f1, recall, ... } } } } }
    // to:
    // { ADJ: { ADJ: { f1, recall, ..., } } }
    const ret = metrics.value.metrics[metricName.value].grouped.map((i) => ({
        name: i.name,
        count: i.classes.classCount,
        truePositive: i.classes.truePositive,
        falsePositive: i.classes.falsePositive,
        falseNegative: i.classes.falseNegative,
        noMatch: i.classes.noMatch,
        precision: i.metrics.precision,
        recall: i.metrics.recall,
        f1: i.metrics.f1,
    }))
    return ret
})
const singlePosMetrics = computed(() => {
    return Object.values(posMetrics.value).filter((pos) => !pos.name.includes("+"))
})
const multiPosMetrics = computed(() => {
    return Object.values(posMetrics.value).filter((pos) => pos.name.includes("+"))
})
const items = computed(() => {
    // if (selectedSingleOrMultiple.value == "single") return singlePosMetrics.value
    // if (selectedSingleOrMultiple.value == "multi") return multiPosMetrics.value
    return posMetrics.value
})

// Methods
function download(data: Any) {
    const classType = data.field.key
    const group = data.item.name

    downloading.value = true
    API.getMetricsSamples(corporaStore.activeUUID, jobSelection.hypothesisJobId, jobSelection.referenceJobId, metricName.value, classType, group)
        .then((response) => {
            Utils.browserDownloadResponseFile(response)
        })
        .catch(res => Utils.handleBlobError(res, "download grouped metrics samples", app))
        .finally(() => downloading.value = false)

}
</script>
