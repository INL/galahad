<template>
    <a :href="'/galahad/help/general#' + subject" target="_blank" rel="noopener noreferrer">
        read more on <i>{{ subject }}</i>
    </a>
</template>

<script setup lang='ts'>
const props = defineProps({
    subject: { type: String, required: true, default: "__missing_subject__" }
})
</script>

<style scoped lang="scss">
a {
    color: inherit;
    text-decoration: underline;
    text-decoration-style: double;
    text-decoration-color: var(--gold);
}
</style>