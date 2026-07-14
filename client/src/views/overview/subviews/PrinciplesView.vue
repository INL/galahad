<template>
    <GCard title="Principles">
        <template #help>
            <PrinciplesHelp />
            <HelpLink topic="principles" />
        </template>

        <GTable :columns :items :loading sortColumn="annotation">
            <template #empty>
                No principles appeared? That is not right! Please contact the INT at
                <MailAddress />
            </template>

            <template #cell-description="d: TableData<Principle>">
                <ExternalLink v-if="d.item.principle.url" :href="d.item.principle.url">
                    {{ d.item.principle.description ?? d.item.principle.url }}
                </ExternalLink>
                <template v-else>
                    {{ d.item.principle.description }}
                </template>
            </template>

            <template #cell-taggers="d: TableData<Principle>">
                <GButton @click="modalData = d.item" plain>
                    <u>{{ d.item.taggers.length }} {{ d.item.taggers.length == 1 ? "tagger" : "taggers" }}</u>
                </GButton>
            </template>
        </GTable>

        <GModal v-if="modalData" @hide="modalData = undefined">
            <template #title> {{ modalData.principle.name }} ({{ modalData.annotation }}) </template>
            <p>Used by:</p>
            <ul>
                <li v-for="tagger in modalData.taggers" :key="tagger">
                    <router-link :to="`/overview/taggers#${tagger}`">{{ tagger }}</router-link>
                </li>
            </ul>
        </GModal>
    </GCard>
</template>

<script setup lang="ts">
import usePrinciples from "@/stores/static/principles"
import type { Principle } from "@/types/principles"
import type { Column, TableData } from "@/types/ui/table"

const { principles: items, loading } = storeToRefs(usePrinciples())
const columns: Column<Principle>[] = [
    { key: "name", format: (p: Principle): string => p.principle.name },
    { key: "annotation", format: (p: Principle): string => p.annotation },
    { key: "description" },
    { key: "taggers", button: true, sortOn: (p: Principle): number => p.taggers.length },
]
const modalData = ref()
</script>
