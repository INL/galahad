<template>
    <div>
        <h2>Step 3: Jobs</h2>
        <p>
            You can use some or all of the available taggers to tag your documents. Each tagger will create a linguistic
            annotation layer. Use the filters to find a tagger that works well for you or take a look at the
            <GNav :route="{ path: '/overview/benchmarks' }">benchmark results</GNav>.
        </p>
        <p>
            To start a tagging job, select one of the taggers and click on 'View & Tag'.
        </p>
        <GNav :route="{ path: '/annotate/jobs' }" v-if="showLink">Go to <i>Annotate > Jobs</i></GNav>
    </div>
</template>

<script setup lang="ts">
// Components
import { GNav } from '@/components'

defineProps({
    showLink: Boolean,
})
</script>
