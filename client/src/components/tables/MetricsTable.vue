<template>
    <GTable :title :columns :items :loading helpSubject="evaluate" class="metricsTable" :sortedByField :noHelp>

        <template #help>
            <slot name="help">
                <p><i>PoS comparison is base on the full PoS, including features.</i></p>
                <p>Click on a entry to view samples.</p>
            </slot>
            <DifferentTagsetsHelp />
        </template>

        <template #table-empty-instruction>
            Select a reference layer and a hypothesis layer to generate metrics.
        </template>

        <template #header v-if="loading">
            <p>Generating metrics for large corpora may take a while...</p>
        </template>

        <template #prepend>
            <slot name="prepend"></slot>
        </template>

        <template
            v-for="cell in ['cell-accuracy', 'cell-precision', 'cell-recall', 'cell-f1', 'cell-macroPrecision', 'cell-microPrecision', 'cell-macroRecall', 'cell-microRecall', 'cell-macroF1', 'cell-microF1', 'cell-microAccuracy']"
            #[cell]="data">
            <div :key="cell">
                {{ `${data.value ? parseFloat(data.value).toFixed(2) : 0}` }}
            </div>
        </template>

        <template v-for="cell in ['cell-falsePositive', 'cell-falseNegative', 'cell-truePositive', 'cell-noMatch']"
            #[cell]="data">
            <div :key="cell">
                <GButton :disabled="data.value.count === 0" @click="openModal(data)">
                    {{ (`${(data.value.count / data.item.count * 100).toFixed(1)}%`) }}
                    <i>({{ data.value.count.toString() }})</i>
                </GButton>
            </div>
        </template>
    </GTable>

    <ComparisonModal :show=showModal @hide="showModal = false" :samples="samples"
        @download="$emit('download', modalData)" :referenceJob="jobSelection.referenceJobId"
        :hypothesisJob="jobSelection.hypothesisJobId" :downloading />
</template>

<script setup lang="ts">
// Libraries & stores
import { ref } from 'vue'
import stores, { JobSelectionStore } from "@/stores"
// Components
import { GTable, GButton, ComparisonModal } from '@/components'
import DifferentTagsetsHelp from '@/components/help/DifferentTagsetsHelp.vue'

// Stores
const jobSelection = stores.useJobSelection() as JobSelectionStore

// Props
const props = defineProps({
    title: { type: String, default: "Metrics" },
    columns: { type: Array, default: [] },
    items: { type: Array, default: [] },
    loading: { type: Boolean, default: false },
    sortedByField: { type: String, default: "count" },
    downloading: { type: Boolean, default: false },
    noHelp: { type: Boolean, default: false }
})

// Emits
defineEmits(['download'])

// Fields
const showModal = ref(false)
const samples = ref({ title: "", samples: [] } as { title: string, samples: TermComparison[] })
const modalData = ref({})

// Methods
/**
 * Open a set of samples in a modal.
 */
function openModal(data) {
    modalData.value = data
    samples.value = { title: `${data.field.label} ${data.item.name} samples`, samples: data.value.samples }
    showModal.value = true
}
</script>

<style scoped lang="scss">
.metricsTable td {
    button {
        display: block;
        text-align: center;
        width: 100%;
        height: 100%;
        margin: 0;
        background-color: transparent;

        &:hover {
            background-color: var(--int-light-grey) !important;
        }

        &:focus {
            background-color: var(--int-light-grey-hover) !important;
        }
    }
}

table button {
    display: block;
    width: initial;
    white-space: initial;
    margin: auto;
}

.metricsTable :deep(td) {
    padding: 0 10px !important;
    margin: 0;
}

.metricsTable :deep(.table-control) {
    min-height: auto;
}
</style>
