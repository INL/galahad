<template>
    <GTabs class="level-2" basePath="/annotate" :tabs="[
        { id: 'corpora', title: 'Corpora' },
        { id: 'documents', title: 'Documents', disabled: !corporaStore.activeCorpus },
        { id: 'jobs', title: 'Jobs', disabled: !corporaStore.hasDocs },
        { id: 'evaluate', title: 'Evaluate', disabled: !corporaStore.hasDocs },
        { id: 'export', title: 'Export', disabled: !userStore.hasWriteAccess || !corporaStore.hasDocs }
    ]" />
</template>

<script setup lang="ts">
// Libraries & stores
import { onMounted } from 'vue'
import router from '@/router'
import stores, { UserStore, CorporaStore } from '@/stores'
// Components
import { GTabs } from '@/components'

// Stores
const userStore = stores.useUser() as UserStore
const corporaStore = stores.useCorpora() as CorporaStore

// When reloading the page in any of the subtabs, the corpus UUID will be set, 
// but computing activeCorpus also needs the corpora to be retrieved.
onMounted(() => {
    // The corpora tab is an exception, it already reloads corporaStore by itself.
    if (!router.currentRoute.value.path.includes('corpora'))
        corporaStore.reload()
})

</script>
