<template>
    <div>
        <GTabs basePath="/contribute" class="level-2" :tabs="[
            { id: 'taggers', title: 'Taggers' },
            { id: 'datasets', title: 'Datasets' },
        ]" />
    </div>
</template>

<script setup lang="ts">
import { GTabs } from '@/components'
</script>
<style scoped lang="scss">
:deep(.content-wrapper)>.content {
    flex: 0 1 800px !important;
}
</style>
