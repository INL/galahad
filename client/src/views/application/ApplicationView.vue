<template>
    <GTabs
        class="level-2"
        basePath="/application"
        :tabs="[
            { id: 'about', title: 'About' },
            { id: 'contribute', title: 'Contribute' },
            { id: 'api', title: 'API' },
        ]"
    >
        <template #api-title>
            <ExternalLink class="nav-link" :href>API</ExternalLink>
        </template>
    </GTabs>
</template>

<script setup lang="ts">
const href = `${window.location.protocol}//${window.location.hostname}/galahad/api/swagger-ui/index.html`
</script>
