<template>
    <GTable :columns :items :loading sortColumn="name" selectable v-model="selectedCorpus">
        <template #help>
            <slot name="help"></slot>
        </template>

        <template #header>
            <slot name="header"></slot>
        </template>

        <template #empty>
            <slot name="empty"></slot>
        </template>

        <!-- source cell -->
        <template #cell-source="d: TableData<CorpusMetadata>">
            <ExternalLink v-if="d.item.source?.url" :href="d.item.source?.url">
                {{ d.item.source?.name ?? d.item.source?.url }}
            </ExternalLink>
            <template v-else>{{ d.item.source?.name }}</template>
        </template>

        <!-- jobs cell -->
        <template #cell-jobs="d: TableData<CorpusMetadata>">
            <GSpinner small inline v-if="d.item.processing > 0" />
            {{ d.item.jobs }}
        </template>
    </GTable>
</template>

<script setup lang="ts">
import type { CorpusMetadata } from "@/types/corpora"
import { type Column, type TableData } from "@/types/ui/table"
import { formatBytes, formatDate, formatPeriod, formatShared } from "@/ts/format"
import useCorpora from "@/stores/corpora"

// --- props ---
const { filter } = defineProps<{ filter: (c: CorpusMetadata) => boolean }>()

// --- data ---
const { loading, corpusId, corpus, corpora } = storeToRefs(useCorpora())
const columns: Column<CorpusMetadata>[] = [
    { key: "name" },
    { key: "source" },
    { key: "tagset" },
    { key: "language" },
    { key: "period", align: "center", format: (c: CorpusMetadata): string | undefined => formatPeriod(c.period) },
    { key: "documents", label: "files", align: "right" },
    {
        key: "annotations",
        label: "tokens",
        align: "right",
        format: (c: CorpusMetadata): number => c.annotations?.token ?? 0,
    },
    { key: "jobs", label: "jobs", align: "right" },
    { key: "shared", sortOn: sortShared, format: formatShared },
    {
        key: "size",
        align: "right",
        sortOn: (c: CorpusMetadata): number => c.size,
        format: (c: CorpusMetadata): string => formatBytes(c.size),
    },
    { key: "modified", format: (c: CorpusMetadata): string => formatDate(c.modified) },
]

// --- computed ---
const items = ref<CorpusMetadata[]>([])
// For some reason, this needs to be a watch.
// You would expect a computed based on corpora.value.filter to work,
// but is keeps triggering uncessarily.
watch(
    corpora,
    () => {
        items.value = corpora.value.filter((c: CorpusMetadata) => filter(c))
    },
    { immediate: true },
)

const selectedCorpus = computed<CorpusMetadata>({
    get: () => corpus.value,
    set: (value: CorpusMetadata) => {
        corpusId.value = value.uuid
    },
})

// --- methods ---
function sortShared(c: CorpusMetadata): number {
    if (c.dataset) return -1
    return (c.collaborators?.length || 0) + (c.viewers?.length || 0)
}
</script>
