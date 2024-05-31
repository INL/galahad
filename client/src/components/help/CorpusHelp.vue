<template>
    <div>
        <h2>Step 1: Corpora</h2>
        <ol>
            <li>Press 'new' to create one or more corpora.</li>
            <li>Select a corpus and press 'edit' to modify the corpus metadata</li>
            <li>Select a corpus and press ‘delete’ to remove the selected corpus</li>
        </ol>
        <GNav v-if="showLink" :route="{ path: '/annotate/corpora' }">Go to <i>Annotate &gt; Corpora</i></GNav>
    </div>
</template>

<script setup lang="ts">
// Components
import { GNav } from "@/components"

defineProps({
    showLink: Boolean,
})
</script>
