<template>
    <GCard title="Taggers">
        <template #help>
            <TaggersHelp />
            <HelpLink topic="taggers" />
        </template>

        <GTable :columns :items :loading sortColumn="period">
            <template #empty>
                No taggers appeared? That is not right! Please contact the INT at
                <MailAddress />.
            </template>

            <template #cell-name="d: TableData<Tagger>">
                <span :id="d.item.name" :class="markActive(d.item.name)">{{ d.item.name }}</span>
            </template>

            <template #cell-annotations="d: TableData<Tagger>">
                <AnnotationItemsViewer :tagger="d.item" />
            </template>

            <template #cell-attributions="d: TableData<Tagger>">
                <AttributionsViewer :tagger="d.item" />
            </template>
        </GTable>
    </GCard>
</template>

<script setup lang="ts">
import useTaggers from "@/stores/static/taggers"
import { formatPeriod } from "@/ts/format"
import type { Tagger } from "@/types/taggers"
import type { Column, TableData } from "@/types/ui/table"

const { taggers: items, loading } = storeToRefs(useTaggers())

const columns: Column<Tagger>[] = [
    { key: "name" },
    { key: "description" },
    { key: "language" },
    { key: "period", format: (t: Tagger): string | undefined => formatPeriod(t.period) },
    { key: "annotations", sortOn: (t: Tagger): string => t.annotations.map((a) => a.annotation).join() },
    { key: "attributions", noSort: true },
]

/** Mark the active row, retrieved from the url anchor. */
function markActive(name: string): string {
    const hash = window.location.hash.substring(1)
    return name === hash ? "active" : ""
}
</script>

<style scoped lang="scss">
:deep(tr):has(> td > span.active) {
    background-color: var(--int-theme-lighter);
}
</style>
