<!-- A modal used by PoS confusion & metrics. -->
<template>
    <GModal @hide="$emit('hide')">
        <GTable :columns="filteredColumns" :items>
            <template #title>
                <slot name="title"></slot>
            </template>

            <template #help>
                <p>
                    Here you can see a sample of how a token was tagged by <i>{{ hypothesisLayer.tagger.name }}</i> and
                    <i>{{ referenceLayer.tagger.name }}</i
                    >. The samples are a random selection of all tokens in this category.
                </p>
            </template>

            <template #header>
                <p>Columns to display:</p>
                <GForm>
                    <GCheckBox v-for="option in annotationOptions" :key="option" v-model="visibleColumns[option]">
                        {{ option }}
                    </GCheckBox>
                </GForm>
            </template>
        </GTable>
        <!--Download-->
        <p>Download all samples for this category.</p>
        <DownloadButton wide :loading="downloading" @click="$emit('download')" />
    </GModal>
</template>

<script setup lang="ts">
// Libraries & stores

// Types & API.
import { literalsForTermComparison } from "@/ts/termcomparison"
import type { TermComparison, EvaluationEntry } from "@/types/evaluation"
import type { LayerMetadata } from "@/types/layers"

// Props
const { evaluationEntry, hypothesisLayer, referenceLayer, annotations, downloading } = defineProps<{
    evaluationEntry: EvaluationEntry
    hypothesisLayer: LayerMetadata
    referenceLayer: LayerMetadata
    annotations: string[]
    downloading: boolean
}>()

const annotationOptions = computed(() => [
    ...new Set(
        [...Object.keys(hypothesisLayer.annotations), ...Object.keys(referenceLayer.annotations)].filter(
            (i) => i != "token",
        ),
    ),
])

// // Emits
const emit = defineEmits<{ download: []; hide: [] }>()

const columns = computed(() => {
    const referenceColumns = annotationOptions.value.map((i) => ({
        key: `${referenceLayer.tagger.name}-${i}`,
        label: `${referenceLayer.tagger.name}<br>${i}`,
    }))
    const hypothesisColumns = annotationOptions.value.map((i) => ({
        key: `${hypothesisLayer.tagger.name}-${i}`,
        label: `${hypothesisLayer.tagger.name}<br>${i}`,
    }))

    return [{ key: "token" }, ...hypothesisColumns, ...referenceColumns]
})
const visibleColumns = ref<Record<string, boolean>>({})
// /**
//  * columns filtered based on the visible columns selection.
//  */
const filteredColumns = computed(() => {
    const columnNames = ["token"]
    for (const [annotation, visible] of Object.entries(visibleColumns.value)) {
        if (visible) {
            columnNames.push(`${referenceLayer.tagger.name}-${annotation}`)
            columnNames.push(`${hypothesisLayer.tagger.name}-${annotation}`)
        }
    }
    return columns.value.filter((i) => columnNames.includes(i.key))
})

const items = computed(() => {
    return evaluationEntry.samples.map((sample: TermComparison) => {
        const hypoAnnotations = Object.entries(sample.hyp.annotations).map((i) => ({
            [`${hypothesisLayer.tagger.name}-${i[0]}`]: i[1],
        }))
        const refAnnotations = Object.entries(sample.ref.annotations).map((i) => ({
            [`${referenceLayer.tagger.name}-${i[0]}`]: i[1],
        }))

        return { token: literalsForTermComparison(sample), ...Object.assign({}, ...hypoAnnotations, ...refAnnotations) }
    })
})

watch(
    () => annotations,
    () => {
        visibleColumns.value = {} // reset
        // annotationType is e.g. "[pos]" or "[pos, lemma]"
        for (const annotation of annotations) {
            visibleColumns.value[annotation] = true
        }
    },
    { immediate: true },
)
</script>
