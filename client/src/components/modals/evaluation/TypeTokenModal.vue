<template>
    <GModal @hide="$emit('hide')">
        <GTable :columns :items sortColumn="count">
            <template #title>
                Types of {{ annotation }} <i>{{ typeToken?.annotation }}</i> and {{ group }}
                <i>{{ typeToken?.group }}</i>
            </template>

            <template #help>
                This is an overview of all types belonging to the chosen {{ annotation }}&mdash;{{ group }} pair.
            </template>
        </GTable>
    </GModal>
</template>

<script setup lang="ts">
import type { TypeToken } from "@/types/evaluation/distribution"
import type { Column } from "@/types/ui/table"

const { typeToken, annotation, group } = defineProps<{ typeToken: TypeToken; annotation: string; group: string }>()

const columns: Column<{ type: string; count: number }>[] = [
    { key: "type" },
    { key: "count", format: (i) => i.count.toLocaleString(), sortOn: (i) => i.count },
]
const items = computed(() => Object.entries(typeToken.tokens).map(([type, count]) => ({ type, count })))
</script>
