<template>
    <GCard>
        <CorporaTable title="Datasets" :filter="(c: CorpusMetadata) => c.dataset">
            <template #help>
                <DatasetsHelp />
                <HelpLink topic="datasets" />
            </template>
            <template #empty>No dataset corpora available.</template>
        </CorporaTable>

        <DocumentsTable :layer="sourceLayer"> </DocumentsTable>
    </GCard>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import useDocuments from "@/stores/documents"
import useLayers from "@/stores/layers"
import type { CorpusMetadata } from "@/types/corpora"

const { corpus, corpusId } = storeToRefs(useCorpora())
const { documents } = storeToRefs(useDocuments())
const { sourceLayer, layers } = storeToRefs(useLayers())

const { reload: reloadCorpora } = useCorpora()
const { reload: reloadDocuments } = useDocuments()
const { reload: reloadLayers } = useLayers()

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
