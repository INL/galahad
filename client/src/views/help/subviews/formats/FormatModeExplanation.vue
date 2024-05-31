<template>
    <h4 v-if="slots.import">Import</h4>
    <p>
        <slot name="import"></slot>
    </p>

    <h4 v-if="slots.export">Export</h4>
    <p>
        <slot name="export"></slot>
    </p>

    <h4 v-if="slots.merge">Merge</h4>
    <p>
        <slot name="merge"></slot>
    </p>
</template>

<script setup lang="ts">
import { useSlots } from 'vue'
const slots = useSlots()
</script>

<style scoped lang="scss">
h4 {
    font-size: 17px;
}
</style>