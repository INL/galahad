<template>
    <GCard title="Principles">
        <template #help>
            <p>Here you can see an overview of possible tagsets to annotate Dutch.</p>
            <p>
                <i>Currently only TDN-Core is used in the platform.</i>
            </p>
        </template>

        <GTable :columns :items :loading sortColumn="annotation">
            <template #empty>
                No tagsets appeared? That is not right! Please contact the INT at
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
        </GTable>
    </GCard>
</template>

<script setup lang="ts">
import usePrinciples from "@/stores/static/principles"
import type { Principle } from "@/types/principles"
import type { Column } from "@/types/ui/table"

const { principles: items, loading } = storeToRefs(usePrinciples())
const columns: Column<Principle>[] = [
    { key: "name", format: (p: Principle): string => p.principle.name },
    { key: "annotation", format: (p: Principle): string => p.annotation },
    { key: "description" },
]
</script>
