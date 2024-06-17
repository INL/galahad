<template>
    <div>

        <GTable title="Part-of-speech confusion" helpSubject="evaluation" :columns :items="rows" id="confusionTable"
            :loading="loading" sortedByColumn="referenceJob" :sortDesc="false" hoverRow>
            <template #help>
                <p>
                    In part-of-speech confusion, an overview is given of the matches (in green) and mismatches per PoS
                    when comparing
                    the tagging of the hypothesis layer with the reference layer. Click on any frequency below to show a
                    data sample.
                </p>
                <p>
                    The category "MULTIPLE" contains combined tags like "ADP+NOU" or "VRB+PD+PD". These are shown in one
                    cell, but this does not mean that the taggers agree on the exact tags. Click on the cell or look at
                    the Global Metrics for more details.
                </p>
                <DifferentTagsetsHelp />
            </template>

            <template #table-empty-instruction>Select a reference layer and a hypothesis layer to generate a confusion
                table.</template>

            <!-- top left header -->
            <template #head-referenceJob>
                part-of-speech <br>
                ({{ jobSelection.hypothesisJobId }}→)<br>
                ({{ jobSelection.referenceJobId }}↓)
            </template>

            <!-- custom cell rendering -->
            <template #cell="data: Cell">

                <!-- header column -->
                <div v-if="data.field.key == 'referenceJob'">
                    {{ data.value }}
                </div>

                <!-- cell -->
                <GButton v-else :disabled="!data.value.count" :class=cssClass(data) @click="openModal(data)">
                    {{ `${(data.value ? data.value.count : 0).toString().padStart(3, '&nbsp;')}` }}
                </GButton>

            </template>

        </GTable>

        <ComparisonModal :show="showModal" @hide="showModal = false" :samples="samples" :downloading
            @download="(data) => download(data)" :referenceJob="jobSelection.referenceJobId"
            :hypothesisJob="jobSelection.hypothesisJobId" />

        <EvaluationInfoBox :eval="confusion" />

    </div>
</template>

<script setup lang='ts'>
// Libraries & stores
import { computed, ref } from 'vue'
import stores, { JobSelectionStore, CorporaStore, AppStore } from '@/stores'
import { storeToRefs } from 'pinia'
// API & types
import { Field } from '@/types/table'
import { TermComparison, EvaluationEntry } from "@/types/evaluation"
import { MISC } from '@/stores/evaluation/confusion'
import * as API from '@/api/evaluation'
import * as Utils from "@/api/utils"
// Components
import { GButton, GInfo, GTable, EvaluationInfoBox, ComparisonModal } from '@/components'
import DifferentTagsetsHelp from '@/components/help/DifferentTagsetsHelp.vue'

// Stores
const { loading, confusion } = storeToRefs(stores.useConfusion())
const corporaStore = stores.useCorpora() as CorporaStore
const jobSelection = stores.useJobSelection() as JobSelectionStore
const app = stores.useApp() as AppStore

// Custom types
type Item = { [key: string]: EvaluationEntry } & { referenceJob: string }
type Cell = { field: Field; item: Item; value: EvaluationEntry }

// Fields
const downloading = ref(false)
const modalData = ref({})
const samples = ref({ title: "", samples: [] } as { title: string, samples: TermComparison[] })
const showModal = ref(false)

const columns = computed((): Field[] => {
    // add the entries
    const entries = {} as { [key: string]: boolean }
    Object.keys(confusion?.value?.table)?.map((k1) => {
        Object.keys(confusion?.value?.table[k1])?.forEach((k2) => entries[k2] = true)
    })

    // add referenceJob, sort, map and return
    const refJobField = {
        key: 'referenceJob',
        sortOn: (value: Item) => {
            const pos = value.referenceJob
            return (posToBottom(pos) ? Infinity : pos)
        }
    }

    const allFields = Object.keys(entries)
        // GTable sort also uses localeCompare. Just using sort() as is messes up the order 
        // between e.g. 'NOU' & 'NO_POS'. I don't know why, though.
        .sort((a, b) => {
            if (posToBottom(a)) return 1
            if (posToBottom(b)) return -1
            return a.localeCompare(b)
        })

    const returnVal = allFields.map(field => {
        return {
            key: field,
            sortOn: value => (field !== 'referenceJob' ? value[field]?.count : value?.referenceJob)
        }
    })
    returnVal.unshift(refJobField)
    return returnVal
})

const rows = computed((): Item[] => {
    return Object.keys(confusion.value.table).map((k1) => {
        const ret = { referenceJob: k1 } as { [key: string]: EvaluationEntry } & {
            referenceJob: string
        }
        Object.keys(confusion.value.table[k1]).forEach(
            (k2) => (ret[k2] = confusion.value.table[k1][k2])
        )
        return ret
    })
})

// Methods
function download() {
    const data = modalData.value
    const hypothesisPos = data.field.key
    const referencePos = data.item.referenceJob
    downloading.value = true
    API.getDownloadPosConfusion(corporaStore.activeUUID, jobSelection.hypothesisJobId, jobSelection.referenceJobId, hypothesisPos, referencePos)
        .then((response) => {
            Utils.browserDownloadResponseFile(response)
        })
        .catch(res => Utils.handleBlobError(res, "download confusion samples", app))
        .finally(() => downloading.value = false)
}
/**
 * Case insensitive string compare.
 */
function strEqual(a: string, b: string) {
    return a.toUpperCase() === b.toUpperCase()
}

/**
 * returns whether this pos should be sorted to the bottom.
 */
function posToBottom(pos: string) {
    const posses = ["NO_POS", "Missing match", MISC, "LET", "PUNCT", "PC", "MULTIPLE"]
    return posses.includes(pos)
}

function cssClass(data) {
    const match: boolean = strEqual(data.field.key, data.item.referenceJob)
    const warnings = ["NO_POS", "MULTIPLE", "Missing match"]
    if (warnings.includes(data.field.key)) {
        return {
            orange: match,
            plain: !match
        }
    }
    else {
        return {
            green: match,
            plain: !match
        }
    }
}

function openModal(data) {
    modalData.value = data
    samples.value = {
        agreement: strEqual(data.field.key, data.item.referenceJob),
        samples: data.value.samples,
        hypothesisPos: data.field.key,
        referencePos: data.item.referenceJob
    }
    showModal.value = true
}
</script>

<style scoped lang="scss">
#confusionTable :deep(td) {
    padding: 0 !important;
    margin: 0;
}

#confusionTable td {
    button {
        display: block;
        text-align: center;
        width: 100%;
        height: 100%;
        margin: 0;

        &.plain {
            background-color: transparent;

            &:hover {
                background-color: var(--int-light-grey) !important;
            }

            &:focus {
                background-color: var(--int-light-grey-hover) !important;
            }
        }
    }
}
</style>
