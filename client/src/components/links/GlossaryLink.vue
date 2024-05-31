<template>
    <a :href="'/galahad/help/glossary#' + gloss" target="_blank" rel="noopener noreferrer">
        <slot>{{ gloss }}</slot>
    </a>
</template>

<script lang='ts'>
import { defineComponent } from 'vue'

export default defineComponent({
    name: "GlossaryLink",
    props: {
        "gloss": { type: String },
    },
});
</script>

<style scoped lang="scss">
a {
    color: inherit;
    text-decoration: underline dotted black !important;

    &:hover {
        text-decoration-style: solid !important;
    }
}
</style>