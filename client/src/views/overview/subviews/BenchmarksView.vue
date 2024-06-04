<template>
    <div class="center">
        <GCard title="Benchmarks">
            <template #help>
                Benchmarks show the performance of taggers on the default datasets.
                The accuracy scores are given for lemma, PoS, and both.
                <br />
                For more details on the datasets, see the
                <GNav :route="{ path: '/overview/datasets' }">
                    datasets overview
                </GNav>

                <b>*</b>: when taggers use a different tagset than the reference tagset, the score can be very low.
            </template>

            <GTable headless :columns :items :loading="assaysStore.loading" noHelp>

                <template #table-empty-instruction>
                    Select a dataset to view benchmarks.
                </template>

                <!-- tagger name -->

                <template #cell-tagger="d">
                    <ExternalLink v-if="d.item.tagger !== SOURCE_LAYER"
                        :href="`/galahad/overview/taggers#${d.item.tagger}`">
                        {{ d.item.tagger }}
                    </ExternalLink>
                    <div v-else>
                        <span style="font-weight: bold">{{ d.item.tagger }}</span>
                    </div>
                </template>

                <template #cell="d">
                    {{ d.value ? d.value.toFixed(2) : "0.00" }}<span v-if="showAsterisk(d)">*</span>
                </template>

                <template #cell-details="d">
                    <ExternalLink
                        :href="`/galahad/annotate/evaluate?corpus=${selectedDatasetUuid}&hypothesis=${d.item.tagger}`">
                        Details
                    </ExternalLink>
                </template>

                <template #prepend>
                    <div class="table-controls">
                        <div class="table-control">
                            Dataset:
                            <GInput type="select" :options="datasetOptions" v-model="selectedDatasetUuid" />
                        </div>
                    </div>
                    <MetricsFilter ref="metricsFilter" v-if="selectedDatasetUuid" />
                </template>

            </GTable>
        </GCard>
    </div>
</template>

<script setup lang='ts'>
// Libraries & stores
import { computed, onMounted, ref } from 'vue'
import stores, { AssaysStore } from '@/stores'
// API & Types
import { MetricTypeAssay } from '@/types/assays'
import { SOURCE_LAYER } from '@/types/jobs'
import { TableData } from '@/types/table'
// Components
import { MailAddress, GTable, GInfo, GCard, GNav } from '@/components'
import MetricsFilter from '@/components/tables/MetricsFilter.vue'

// Types
type AssayRow = { tagger: string, accuracy: number, precision: number, recall: number, f1: number }

// Stores
const assaysStore = stores.useAssays() as AssaysStore
const corporaStore = stores.useCorpora()

// Fields
const datasetOptions = computed(() => corporaStore.datasetCorpora.map((d) => ({ value: d.uuid, text: d.name })).sort((a, b) => a.text.localeCompare(b.text)))
const selectedDatasetUuid = ref(null)
const selectedDatasetName = computed(() => corporaStore.datasetCorpora.find((d) => d.uuid == selectedDatasetUuid.value)?.name)
const metricsFilter = ref(null)
const columns = [
    { key: "tagger", label: "tagger" },
    { key: "precision", label: "macro\nprecision", sortOn: i => i.precision },
    { key: "recall", label: "macro\nrecall", sortOn: i => i.recall },
    { key: "f1", label: "macro\nf1", sortOn: i => i.f1 },
    { key: "accuracy", label: "micro\naccuracy", sortOn: i => i.accuracy },
    { key: "details", label: "detailed\nevaluation" },
]
/**
 * Our input data is in the form:
 * {
 *     "dataset-1": {
 *         "posByPos": {
 *             "tagger-1": {
 *                 "micro": { ... }, "macro": { ... }
 *             },
 *             "tagger-2": { ... },
 *         },
 *         "lemmaByLemma": { ... },
 *     },
 *     "dataset-2": { ... },
 * }
 * We want to transform this to:
 * [
 *    { tagger: "tagger-1", microAccuracy: 0, macroPrecision: 0, ... },
 *    { ... },
 * ]
 * Filtered by the selected dataset and metric type.
 */
const items = computed(() => {
    const metricName = metricsFilter.value?.metricName
    return Object.entries(assaysStore.assays[selectedDatasetName.value]?.[metricName] ?? {}).map((taggerAndMetric) => {
        const tagger: string = taggerAndMetric[0]
        const mta: MetricTypeAssay = taggerAndMetric[1]
        const result = {
            tagger: tagger,
            accuracy: mta.micro.accuracy,
            precision: mta.macro.precision,
            recall: mta.macro.recall,
            f1: mta.macro.f1,
        }
        return result
    })
})

// Watches & mounts
// Only needs to load once
onMounted(() => {
    corporaStore.reload()
    assaysStore.reload()
})

// Methods
/** Calculate the score to 2 decimals */
function score(assay: Assay, desc: AssayDescription): string {
    // We access the object value with a string,
    // so typescript needs some explicit typing.
    return (assay[desc.id as keyof Assay] as number / assay.count).toFixed(2)
}

/**
 * Show an asterisk for extremely low PoS scores
 */
function showAsterisk(d: TableData<AssayRow>): boolean {
    return !d.value || parseFloat(d.value) <= 0.02
}
</script>

<style scoped lang="scss">
.center {
    display: flex;
    flex-direction: column;
    align-items: center;
}
</style>
