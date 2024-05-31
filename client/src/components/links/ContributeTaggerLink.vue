<template>
    <GCard title="Add your own tagger">
      <hr>
      Feel free to add your own tagger
      <GNav :route="{ path: '/taggers/contribute'}">Contribute</GNav>
    </GCard>
</template>

<script setup lang='ts'>
import { GCard, GNav } from '@/components'
</script>

<style scoped lang="scss">
</style>