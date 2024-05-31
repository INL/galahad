<template>
    <div>
        <CorpusTable :type="TableCorporaType.Dataset" :corpora="corporaStore.datasetCorpora" selectable>
            <template #help>
                <BenchmarkSetsHelp />
            </template>
        </CorpusTable>
        <DocumentsTable :type="TableDocumentsType.Dataset" :corpus="corporaStore.activeCorpus">
            <template #help>
                Here you can see a small preview of the documents within the selected benchmark set.
            </template>
        </DocumentsTable>
        <GSpinner v-if="corporaStore.loading" medium class="spinner" />
    </div>
</template>

<script setup lang='ts'>
// Libraries & stores
import { onMounted } from 'vue'
import stores, { CorporaStore } from '@/stores'
// API & types
import { TableCorporaType, TableDocumentsType } from '@/types/table'
// Components
import { CorpusTable, DocumentsTable, GSpinner } from '@/components'
import BenchmarkSetsHelp from "@/components/help/BenchmarkSetsHelp.vue"

// Stores
const corporaStore = stores.useCorpora() as CorporaStore

// Watches & mounts
// Only needs to load once.
onMounted(() => {
    corporaStore.reload()
})

</script>

<style>
.spinner {
    align-self: center;
}
</style>
