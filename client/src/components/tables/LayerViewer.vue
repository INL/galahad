<template>
    <GTable :columns :items>
        <template #title>Annotations of {{ name }}</template>
        <template #header>
            <aside>
                <AnnotationSummary :annotations />
                <AnnotationSummary :annotations="structure" />
            </aside>
        </template>
    </GTable>
</template>

<script setup lang="ts">
import type { DocumentMetadata } from "@/types/documents"
import type { LayerMetadata } from "@/types/layers"

// # props
const { document, layer } = defineProps<{ document?: DocumentMetadata; layer?: LayerMetadata }>()

// #computed
const name = computed(() => (document ? document.name : layer?.tagger.name))
const annotations = computed(() => (document ? document.annotations : layer.annotations))
const structure = computed(() => document?.structure ?? layer?.structure)
const columns = computed(() => Object.keys(annotations.value).map((i) => ({ key: i, label: i, noSort: true })))
const terms = computed(() => (document ? document.preview.terms : layer.preview.terms))
const items = computed(() => terms.value.map((t) => t.annotations))
</script>

<style scoped lang="scss">
:deep(td) {
    padding: 0.2rem 0.4rem !important;
}
</style>
