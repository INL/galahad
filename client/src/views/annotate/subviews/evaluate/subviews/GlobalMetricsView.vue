<template>
    <div>
        <MetricsTable
            title="Basic Global Metrics"
            :loading
            :columns
            :items
            @download="(data) => download(data)"
            :downloading
            sortColumn="macroF1"
        >
            <template #help>
                <p>
                    In Global Metrics an overview is given of the (dis)agreement between the two layers that have been
                    selected for lemma and PoS comparison. By clicking on the percentage, a data sample is shown.
                </p>
            </template>
        </MetricsTable>

        <MetricsTable
            v-model="tableData"
            title="Extended Global Metrics"
            :loading="loadingExtended"
            :columns
            :items="extendedItems"
            @download="(data) => download(data)"
            :downloading
            sortColumn="macroF1"
        >
            <template #header>
                <template v-if="commonAnnotations.length">
                    <GForm>
                        <fieldset>
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
                        <fieldset>
                            <label for="group-select">Group by</label>
                            <GSelect id="group-select" :options="groupOptions" v-model="selectedGroup" />
                        </fieldset>
                        <fieldset>
                            <label for="analysis-select">Single/multiple analyses</label>
                            <GSelect id="analysis-select" :options="analysesOptions" v-model="selectedAnalysis" />
                        </fieldset>
                    </GForm>
                </template>
            </template>
        </MetricsTable>
    </div>
</template>

<script setup lang="ts">
// Libraries & stores
import * as API from "@/api/evaluation/metrics"
import * as Utils from "@/api/utils"
// API & types
import useLayers from "@/stores/layers"
import useCorpora from "@/stores/corpora"
import type { SelectOption } from "@/types/ui/select"
import useGroupedMetrics from "@/stores/evaluation/groupedMetrics"
import type { Column, TableData } from "@/types/ui/table"
import type { Metrics, ClassificationClasses, ClassificationMetrics, GlobalMetrics } from "@/types/evaluation/metrics"
import MultiSelect from "primevue/multiselect"
import { formatDecimal, formatClassification } from "@/ts/format"
import useGlobalMetrics from "@/stores/evaluation/globalMetrics"

const { loading, globalMetrics } = storeToRefs(useGlobalMetrics())
const { commonAnnotations, hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
const {
    loading: loadingExtended,
    groupedMetrics,
    annotations: selectedAnnotations,
    group: selectedGroup,
    analysis: selectedAnalysis,
} = storeToRefs(useGroupedMetrics())

// Form
const annotationOptions = computed(() =>
    // only logical annotations TODO might filter, might not
    commonAnnotations.value.filter((option: SelectOption) => !["token"].includes(option.text)),
)
const groupOptions = computed(() =>
    // only logical groups
    commonAnnotations.value.filter((option: SelectOption) => !["head"].includes(option.text)),
)
const analysesOptions: SelectOption[] = [
    { value: "both", text: "Both" },
    { value: "single", text: "Single" },
    { value: "multiple", text: "Multiple" },
]

const extendedItems = computed<ClassificationClasses & { group: string }>(() => {
    if (!groupedMetrics.value) return []
    return [
        {
            ...groupedMetrics.value,
            truePositive: groupedMetrics.value.classes.truePositive,
            falseNegative: groupedMetrics.value.classes.falseNegative,
            hypothesis: groupedMetrics.value.classes.hypothesis,
            reference: groupedMetrics.value.classes.reference,
        },
    ]
})

const columns: Column<GlobalMetrics>[] = computed(() => [
    { key: "annotation", format: (g: GlobalMetrics) => g.settings.annotations.join(", ") },
    { key: "group", format: (g: GlobalMetrics) => g.settings.group },
    {
        key: "microAccuracy",
        label: `micro<br>accuracy`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.accuracy),
        sortOn: (g: GlobalMetrics) => g.accuracy,
    },
    {
        key: "macroAccuracy",
        label: `macro<br>accuracy`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.macro.accuracy),
        sortOn: (g: GlobalMetrics) => g.macro.accuracy,
    },
    {
        key: "macroPrecision",
        label: `macro<br>precision`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.macro.precision),
        sortOn: (g: GlobalMetrics) => g.macro.precision,
    },
    {
        key: "macroRecall",
        label: `macro<br>recall`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.macro.recall),
        sortOn: (g: GlobalMetrics) => g.macro.recall,
    },
    {
        key: "macroF1",
        label: `macro<br>f1`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.macro.f1),
        sortOn: (g: GlobalMetrics) => g.macro.f1,
    },
    {
        key: "truePositive",
        label: `true<br>positive`,
        button: true,
        sortOn: (g: GlobalMetrics) => g.classes.truePositive.count,
    },
    {
        key: "falseNegative",
        label: `false<br>negative`,
        button: true,
        sortOn: (g: GlobalMetrics) => g.classes.falseNegative.count,
    },
])
const items = computed((): GlobalMetrics[] => {
    if (!globalMetrics.value) return []
    return globalMetrics.value.map((g: GlobalMetrics) => ({
        ...g,
        truePositive: g.classes.truePositive,
        falseNegative: g.classes.falseNegative,
        hypothesis: g.classes.hypothesis,
        reference: g.classes.reference,
    }))
})
</script>
