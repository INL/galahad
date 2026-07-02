<template>
    <GCard title="Benchmarks" helpLink="benchmarks">
        <template #help>
            <p>
                Benchmarks show the performance of taggers on the default datasets. The accuracy scores are given for
                lemma, PoS, and both. For more details on the datasets, see the
                <router-link to="/overview/datasets">datasets overview</router-link>.
            </p>
            <p>
                <i>
                    <strong>Note:</strong>
                    When taggers use a different tagset than the reference tagset, the score can be very low.
                </i>
            </p>
        </template>

        <GTable :columns :items :loading sortColumn="macroF1">
            <template #header>
                <GForm>
                    <fieldset>
                        <label for="dataset-select">Dataset</label>
                        <GSelect id="dataset-select" :options="datasetOptions" v-model="corpusId" />
                    </fieldset>
                    <fieldset v-if="corpusId && corpus?.dataset">
                        <label for="annotation-select">Annotation</label>
                        <MultiSelect
                            id="annotation-select"
                            v-model="selectedAnnotations"
                            :options="annotationOptions"
                            optionLabel="text"
                            optionValue="value"
                            placeholder="Annotation"
                            :maxSelectedLabels="5"
                        />
                    </fieldset>
                    <fieldset v-if="corpusId && corpus?.dataset">
                        <label for="group-select">Group by</label>
                        <GSelect id="group-select" :options="groupOptions" v-model="selectedGroup" />
                    </fieldset>
                </GForm>
            </template>

            <template #cell-layer="d">
                <ExternalLink :href="`/galahad/overview/taggers#${d.item.layer}`">
                    {{ d.item.layer }}
                </ExternalLink>
            </template>

            <template #cell-details="d">
                <ExternalLink :href="`/galahad/annotate/evaluate?corpus=${corpusId}&hypothesis=${d.item.layer}`">
                    Details
                </ExternalLink>
            </template>

            <template
                v-for="cell in ['cell-truePositive', 'cell-falseNegative']"
                #[cell]="d: TableData<GlobalMetrics>"
                :key="cell"
            >
                <GButton :disabled="d.value?.count === 0" @click="tableData = d" style="justify-content: right" plain>
                    {{ `${((d.value.count / d.item.classes.hypothesis) * 100).toFixed(1)}%` }}
                    <i>({{ d.value.count.toLocaleString() }})</i>
                </GButton>
            </template>
        </GTable>

        <ComparisonModal
            v-if="tableData"
            :evaluationEntry="tableData.value"
            :hypothesisLayer="layers.find((l: LayerMetadata) => l.tagger.name == tableData.item.layer)"
            :referenceLayer="sourceLayer"
            :annotations="[...selectedAnnotations, selectedGroup]"
            :downloading
            @download="() => download(tableData)"
            @hide="tableData = undefined"
        >
            <template #title>
                {{ formatClassification(tableData.column.key) }} samples between <i>{{ tableData.item.layer }}</i> and
                <i>source annotations</i>
            </template>
        </ComparisonModal>
    </GCard>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import type { GlobalMetrics } from "@/types/evaluation/metrics"
import type { CorpusMetadata } from "@/types/corpora"
import type { SelectOption } from "@/types/ui/select"
import type { Column, TableData } from "@/types/ui/table"
import useLayers from "@/stores/layers"
import useBenchmarks from "@/stores/benchmarks"
import MultiSelect from "primevue/multiselect"
import { formatClassification, formatDecimal } from "@/ts/format"
import type { LayerMetadata } from "@/types/layers"

const { sourceAnnotations, layers, sourceLayer } = storeToRefs(useLayers())
const { corpora, corpusId, corpus } = storeToRefs(useCorpora())
const { reload: reloadCorpora } = useCorpora()
const { reload: reloadLayers } = useLayers()
const { annotations: selectedAnnotations, group: selectedGroup, benchmarks, loading } = storeToRefs(useBenchmarks())

const tableData = ref()
const datasetOptions = computed<SelectOption[]>((): SelectOption[] =>
    corpora.value
        .filter((c: CorpusMetadata) => c.dataset)
        .map((c: CorpusMetadata) => ({ text: c.name, value: c.uuid })),
)
const annotationOptions = computed(() =>
    sourceAnnotations.value.filter((option: SelectOption) => !["token"].includes(option.text)),
)
const groupOptions = computed<SelectOption[]>((): SelectOption[] =>
    sourceAnnotations.value.filter((option: SelectOption) => !["head"].includes(option.text)),
)
const selectedAnnotation = computed<string>((): string => {
    if (!selectedAnnotations.value?.length) return ""
    return selectedAnnotations.value?.join("<br>")
})

const columns: Column<GlobalMetrics>[] = computed(() => [
    { key: "layer", label: "tagger" },
    {
        key: "microAccuracy",
        label: `${selectedAnnotation.value}<br>micro<br>accuracy`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.micro.accuracy),
        sortOn: (g: GlobalMetrics) => g.micro.accuracy,
    },
    {
        key: "microF1",
        label: `${selectedAnnotation.value}<br>micro<br>f1`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.micro.f1),
        sortOn: (g: GlobalMetrics) => g.micro.f1,
    },
    {
        key: "macroAccuracy",
        label: `${selectedAnnotation.value}<br>macro<br>accuracy`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.macro.accuracy),
        sortOn: (g: GlobalMetrics) => g.macro.accuracy,
    },
    {
        key: "macroPrecision",
        label: `${selectedAnnotation.value}<br>macro<br>precision`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.macro.precision),
        sortOn: (g: GlobalMetrics) => g.macro.precision,
    },
    {
        key: "macroRecall",
        label: `${selectedAnnotation.value}<br>macro<br>recall`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.macro.recall),
        sortOn: (g: GlobalMetrics) => g.macro.recall,
    },
    {
        key: "macroF1",
        label: `${selectedAnnotation.value}<br>macro<br>f1`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.macro.f1),
        sortOn: (g: GlobalMetrics) => g.macro.f1,
    },
    {
        key: "truePositive",
        label: `${selectedAnnotation.value}<br>true<br>positive`,
        button: true,
        sortOn: (g: GlobalMetrics) => g.classes.truePositive.count,
    },
    {
        key: "falseNegative",
        label: `${selectedAnnotation.value}<br>false<br>negative`,
        button: true,
        sortOn: (g: GlobalMetrics) => g.classes.falseNegative.count,
    },
    { key: "details", label: "detailed<br>evaluation", align: "center", noSort: true },
])
const items = computed((): GlobalMetrics[] => {
    if (!benchmarks.value) return []
    return benchmarks.value.map((g: GlobalMetrics) => ({
        ...g,
        truePositive: g.classes.truePositive,
        falseNegative: g.classes.falseNegative,
        hypothesis: g.classes.hypothesis,
    }))
})

onMounted(reloadCorpora)
onMounted(reloadLayers)

// Deselect non-dataset
watchPostEffect(() => {
    if (corpusId.value && corpus.value && !corpus.value.dataset) {
        corpusId.value = undefined
        layers.value = []
    }
})

// Default select options
watchPostEffect(() => {
    if (!annotationOptions.value?.length) return
    selectedAnnotations.value ??= [annotationOptions.value[0]?.value]
})
watchPostEffect(() => {
    selectedGroup.value ??= groupOptions.value[2]?.value
})
</script>
