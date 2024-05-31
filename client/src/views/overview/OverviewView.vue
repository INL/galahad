<template>
    <div>
        <GTabs basePath="/overview" class="level-2" :tabs="[
            { id: 'taggers', title: 'Taggers' },
            { id: 'tagsets', title: 'Tagsets' },
            { id: 'datasets', title: 'Datasets' },
            { id: 'benchmarks', title: 'Benchmarks' },
        ]" />
    </div>
</template>

<script setup lang="ts">
import { GTabs } from '@/components'
</script>
