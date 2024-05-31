<template>
  <a :href="`mailto:${address}`">
    {{ address }}
  </a>
</template>

<script setup lang='ts'>
defineProps({
  address: { type: String, default: "servicedesk@ivdnt.org" }
})
</script>