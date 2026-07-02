<template>
    <dl>
        <dl v-for="[key, value] in Object.entries(annotations ?? {})" :key="key">
            <dt>{{ key }}:</dt>
            <dd>{{ formatDecimal(value).toLocaleString() }}</dd>
        </dl>
    </dl>
</template>

<script setup lang="ts">
import { formatDecimal } from "@/ts/format"
import type { AnnotationsSummary } from "@/types/documents"

const { annotations } = defineProps<{ annotations?: AnnotationsSummary }>()
</script>

<style scoped lang="scss">
dl {
    display: flex;
    gap: 1rem;
    > dl {
        display: inline-flex;
        gap: 0.25rem;
        > dt {
            font-weight: bold;
        }
    }
}
</style>
