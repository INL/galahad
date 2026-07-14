<template>
    <ExternalLink :href class="external-link">
        Go to <i>{{ displayName ?? topic }}</i> in the manual
    </ExternalLink>
</template>

<script setup lang="ts">
const { topic, displayName } = defineProps<{ topic: string; displayName?: string }>()
const href = `/galahad/help/general#${topic}`
</script>

<style scoped lang="scss">
.external-link {
    display: block;
    text-align: center;
}
</style>
