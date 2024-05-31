<template>
    <GTabs class="level-2" basePath="/application" :tabs="[
        { id: 'about', title: 'About' },
        { id: 'api', title: 'API', stub: true },
    ]">

        <template #api-title>
            <ExternalLink class="blockLink" :href=apiURL>API</ExternalLink>
        </template>

    </GTabs>
</template>

<script setup lang='ts'>
// Components
import { GTabs, ExternalLink } from '@/components'

const apiURL = window.location.protocol + '//' + window.location.hostname + "/galahad/api/swagger-ui/index.html"

</script>

<style scoped>
:deep(.blockLink) {
    display: block;
    /* Makes block links full size in tab header */
    margin: 0 -20px;
}
</style>
