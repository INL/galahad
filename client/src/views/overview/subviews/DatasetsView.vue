<template>
    <GCard>
        <CorporaTable title="Datasets" :filter="(c: CorpusMetadata) => c.dataset" :selectable="false">
            <template #help>
                <DatasetsHelp />
                <HelpLink topic="datasets" />
            </template>
            <template #empty>No dataset corpora available.</template>
            <template #files="d: TableData<CorpusMetadata>">
                <RightFloatCell
                    ><template #left>{{ d.item.documents }}</template>
                    <template #right
                        ><InspectButton
                            @click="
                                () => {
                                    corpusId = d.item.uuid
                                    showModal = true
                                }
                            "
                    /></template>
                </RightFloatCell>
            </template>
        </CorporaTable>

        <GModal v-if="showModal" @hide="showModal = false">
            <DocumentsTable :layer="sourceLayer" />
        </GModal>
    </GCard>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import useDocuments from "@/stores/documents"
import useLayers from "@/stores/layers"
import type { CorpusMetadata } from "@/types/corpora"
import type { TableData } from "@/types/ui/table"

const { corpus, corpusId } = storeToRefs(useCorpora())
const { documents } = storeToRefs(useDocuments())
const { sourceLayer, layers } = storeToRefs(useLayers())

const { reload: reloadCorpora } = useCorpora()
const { reload: reloadDocuments } = useDocuments()
const { reload: reloadLayers } = useLayers()

const showModal = ref(false)

onMounted(reloadCorpora)
onMounted(reloadDocuments)
onMounted(reloadLayers)

// Deselect non-dataset
watchPostEffect(() => {
    if (corpusId.value && corpus.value && !corpus.value.dataset) {
        corpusId.value = undefined
        documents.value = []
        layers.value = []
    }
})
</script>
