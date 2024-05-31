<template>
  <a :href="href" target="_blank" rel="noopener noreferrer">
    <slot></slot>
  </a>
</template>

<script setup lang='ts'>
defineProps<{
  href: string
}>()
</script>

<style scoped lang="scss">
a {
  color: inherit;
  cursor: pointer;
  text-decoration: underline;
  text-decoration-style: double;
}
</style>