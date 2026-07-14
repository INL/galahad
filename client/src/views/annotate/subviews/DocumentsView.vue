<template>
    <AnnotateTab hideDocsError>
        <DocumentsTable :layer actionable>
            <template #help>
                <slot name="help">
                    <DocumentsHelp />
                    <HelpLink topic="formats" />
                </slot>
            </template>
            <template #header>
                <UploadDocuments v-if="canWrite" />
            </template>
        </DocumentsTable>
    </AnnotateTab>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"

const { canWrite } = storeToRefs(useCorpora())
const { sourceLayer: layer } = storeToRefs(useLayers())
</script>
