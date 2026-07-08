<template>
    <GInput v-model="model" list="tagsets" placeholder="Tagset name" />
    <datalist id="tagsets">
        <option v-for="p in principles" :key="p.principle.name" :value="p.principle.name"></option>
    </datalist>
</template>

<script setup lang="ts">
import usePrinciples from "@/stores/static/principles"

const model = defineModel<string>()
const { principles } = storeToRefs(usePrinciples())
</script>
