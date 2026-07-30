<template>
    <AnnotateTab>
        <template #title>Evaluate {{ corpus.name }}</template>

        <template #help>
            <EvaluateHelp />
        </template>

        <GForm>
            <LayerSelect />
            <LayerSelect isReference />
            <fieldset>
                <label for="csv-download">Download as CSV</label>
                <i v-if="!hypothesisId || !referenceId"> Select both layers first. </i>
                <DownloadButton v-else id="csv-download" wide :loading @click="downloadCSV" />
            </fieldset>
        </GForm>

        <GTabs
            class="level-3"
            basePath="/annotate/evaluate"
            :tabs="[
                { id: 'distribution', title: 'Distribution' },
                { id: 'global_metrics', title: 'Global Metrics' },
                { id: 'grouped_metrics', title: 'Grouped Metrics' },
                { id: 'confusion', title: 'Confusion' },
            ]"
        >
        </GTabs>
    </AnnotateTab>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import useEvaluation from "@/stores/evaluation"
import useLayers from "@/stores/layers"

const { corpus } = storeToRefs(useCorpora())
const { downloadCSV } = useEvaluation()
const { loading } = storeToRefs(useEvaluation())
const { hypothesisId, referenceId } = storeToRefs(useLayers())
</script>
