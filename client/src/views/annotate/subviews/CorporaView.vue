<template>
    <div>
        <!-- Owner corpus table -->
        <CorpusTable :type="TableCorporaType.User" :corpora="corporaStore.allCorpora"
            @delete="corpus => deleteCorpusData = corpus" @update="corpus => editMode(corpus)" selectable
            @create="showNewCorpusModal = true">
            <template #title>Your corpora</template>
        </CorpusTable>

        <!-- Shared corpus table -->
        <CorpusTable :type="TableCorporaType.User" :corpora="corporaStore.sharedCorpora"
            @delete="corpus => deleteCorpusData = corpus" @update="corpus => editMode(corpus)" selectable
            @create="showNewCorpusModal = true" sharedWithYou>
            <template #title>Shared with you</template>
            <template #help>
                Here you can see the corpora that have been shared with you. <br>
                If a corpus has been shared with you as a
                collaborator, you can make modifications. <br>
                If it has been shared with you as a viewer, you can only
                inspect and evaluate it.
            </template>
        </CorpusTable>

        <!-- Public corpus table -->
        <CorpusTable :type="TableCorporaType.Public" :corpora="corporaStore.publicCorpora"
            @delete="corpus => deleteCorpusData = corpus" @update="corpus => editMode(corpus)" selectable
            @create="showNewCorpusModal = true">
            <template #help>
                <BenchmarkSetsHelp /><br />
                You can inspect them in further detail on the
                <GNav :route="{ path: '/annotate/evaluate' }">Evaluate tab</GNav>.
            </template>
        </CorpusTable>

        <!-- Create modal -->
        <CorpusForm title="Create new corpus" :show="showNewCorpusModal" @hide="showNewCorpusModal = false"
            :action="metadata => { corporaStore.createCorpus(metadata); showNewCorpusModal = false }"
            :cancel="() => showNewCorpusModal = false">
            <template #help>Fill in the metadata and create a corpus.
                <br>
                <CorpusFormHelp />
            </template>
        </CorpusForm>

        <!-- Update modal -->
        <CorpusForm title="Update corpus metadata" :show="updateCorpusData !== null" @hide="updateCorpusData = null"
            update :item="updateCorpusData"
            :action="metadata => { corporaStore.updateCorpus(updateCorpusData.uuid, metadata); updateCorpusData = null }"
            :cancel="() => updateCorpusData = null">
            <template #help>
                Change the metadata of an existing corpus.
                <br>
                <CorpusFormHelp />
            </template>
        </CorpusForm>

        <!-- Delete modal -->
        <DeleteModal :show="deleteCorpusData !== null" :item="deleteCorpusData"
            :displayname="'Corpus ' + (deleteCorpusData !== null ? deleteCorpusData.name : '[null]')"
            @delete="corporaStore.deleteCorpus" @hide="deleteCorpusData = null" />
    </div>
</template>

<script setup lang='ts'>
// Libraries & stores
import { ref, onMounted } from 'vue'
import stores, { CorporaStore } from '@/stores'
// Types & API
import { CorpusMetadata } from '@/types/corpora'
import { TableCorporaType } from '@/types/table'
// Components
import { CorpusTable, GNav } from '@/components'
import CorpusForm from '@/components/modals/corpus/CorpusForm.vue'
import DeleteModal from "@/components/modals/DeleteModal.vue"
import CorpusFormHelp from "@/components/help/CorpusFormHelp.vue"
import BenchmarkSetsHelp from "@/components/help/BenchmarkSetsHelp.vue"

// Stores
const corporaStore = stores.useCorpora() as CorporaStore

// Fields
const showNewCorpusModal = ref(false)
// Once not null, respective modal is shown.
const deleteCorpusData = ref(null as null | CorpusMetadata)
const updateCorpusData = ref(null as null | CorpusMetadata)

const editMode = (corpus: CorpusMetadata) => {
    // Deepcopy so we can modify the object freely.
    updateCorpusData.value = JSON.parse(JSON.stringify(corpus))
}

// Mounts & watches
/** 
 * Although CorporaView lives in AnnotateView, which reloads corpora, 
 * the number of jobs could change when navigating between jobs and corpora, requiring a reload.
 */
onMounted(() => {
    corporaStore.reload()
})
</script>
