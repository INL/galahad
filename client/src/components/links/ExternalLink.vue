<template>
    <a :href target="_blank" rel="noopener noreferrer"> <i class="fa fa-link"></i> <slot></slot> </a>
</template>

<script setup lang="ts">
const { href } = defineProps<{ href: string }>()
</script>

<style scoped lang="scss">
a {
    white-space: nowrap;
}
i.fa {
    display: inline;
    font-size: 0.8rem;
    vertical-align: center;
    letter-spacing: 0.1rem;
    padding: 0;
}
</style>
