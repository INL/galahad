<template>
    <GCard title="Error 404" noHelp>
        <GInfo error>
            <p>We could not find the page you were looking for.</p>
            <GNav :route="{ path: '/' }">Go back to the homepage</GNav>
        </GInfo>
    </GCard>
</template>

<script setup lang="ts">
import { GCard, GInfo, GNav } from '@/components'
</script>

<style scoped="true" lang="scss">
.g-card {
  overflow-y: auto
}
</style>