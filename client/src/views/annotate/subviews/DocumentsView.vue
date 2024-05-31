<template>
    <AnnotateTab hideDocsError hideAnnotationsError> <!-- Afterall, this is where we will add docs -->
        <DocumentsTable :type="TableDocumentsType.User" :corpus="corporaStore.activeCorpus" />
    </AnnotateTab>
</template>

<script setup lang='ts'>
// Libraries & stores
import { onMounted } from 'vue'
import stores, { CorporaStore } from '@/stores'
// API & types
import { TableDocumentsType } from '@/types/table'
// Components
import { DocumentsTable, AnnotateTab } from '@/components'

// Stores
const corporaStore = stores.useCorpora() as CorporaStore
const documentsStore = stores.useDocuments()

// Watches & mounts
onMounted(() => {
    // Clear errors when this tab is opened.
    // Note that we can't put this inside DocumentsTable,
    // because on upload, AnnotateTab will mount it again with v-if.
    documentsStore.clearUploadErrors()
})
</script>
