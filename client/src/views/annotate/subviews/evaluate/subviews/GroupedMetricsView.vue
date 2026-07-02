<template>
    <GCard>
        <MetricsTable
            v-model="tableData"
            title="Grouped Metrics"
            :loading
            :columns
            :items="filteredItems"
            @download="(data) => download(data)"
            :downloading
            sortColumn="hypothesis"
        >
            <template #help>
                <p>
                    In Grouped Metrics an overview is given of the (dis)agreement for lemma and PoS per part-of-speech.
                    For each PoS, different metrics are given by choosing the annotation and the grouping. By clicking
                    on a percentage, a data sample is shown.
                </p>
            </template>
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
                        <fieldset v-if="groupedMetrics">
                            <label for="analysis-select">Single/multiple analyses</label>
                            <GSelect id="analysis-select" :options="analysesOptions" v-model="selectedAnalysis" />
                        </fieldset>
                    </GForm>
                    <aside>
                        <p>Micro summary:</p>
                        <AnnotationSummary :annotations="groupedMetrics?.micro ?? {}" />
                    </aside>
                    <aside>
                        <p>Macro summary:</p>
                        <AnnotationSummary :annotations="groupedMetrics?.macro ?? {}" />
                    </aside>
                </template>
            </template>
        </MetricsTable>

        <ComparisonModal
            v-if="tableData"
            :evaluationEntry="tableData.value"
            :hypothesisLayer
            :referenceLayer
            :annotations="[...selectedAnnotations, selectedGroup]"
            :downloading
            @download="() => download(tableData)"
            @hide="tableData = undefined"
        >
            <template #title>
                {{ formatCamelCase(tableData.column.key) }} samples between <i>{{ hypothesisId }}</i> and
                <i>{{ referenceId }}</i> in
                <i>{{ tableData.item.group }}</i>
            </template>
        </ComparisonModal>
    </GCard>
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
import type { Metrics, ClassificationClasses, ClassificationMetrics } from "@/types/evaluation/metrics"
import MultiSelect from "primevue/multiselect"
import { formatDecimal } from "@/ts/format"

const { commonAnnotations, hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
const {
    loading,
    groupedMetrics,
    annotations: selectedAnnotations,
    group: selectedGroup,
} = storeToRefs(useGroupedMetrics())
const { corpusId } = storeToRefs(useCorpora())

const tableData = ref()

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
const selectedAnalysis = ref<string>(analysesOptions[0].value)
const selectedAnnotation = computed<string>((): string => {
    if (!selectedAnnotations.value?.length) return ""
    return selectedAnnotations.value?.join("<br>")
})

// Table data
const columns = computed((): Column<ClassificationClasses & { group: string }>[] => [
    { key: "group", label: selectedGroup.value },
    {
        key: "accuracy",
        label: `${selectedAnnotation.value}<br>accuracy`,
        align: "right",
        format: (c: ClassificationClasses) => formatDecimal(c.metrics.accuracy),
        sortOn: (c: ClassificationClasses) => c.metrics.accuracy,
    },
    {
        key: "precision",
        label: `${selectedAnnotation.value}<br>precision`,
        align: "right",
        format: (c: ClassificationClasses) => formatDecimal(c.metrics.precision),
        sortOn: (c: ClassificationClasses) => c.metrics.precision,
    },
    {
        key: "recall",
        label: `${selectedAnnotation.value}<br>recall`,
        align: "right",
        format: (c: ClassificationClasses) => formatDecimal(c.metrics.recall),
        sortOn: (c: ClassificationClasses) => c.metrics.recall,
    },
    {
        key: "f1",
        label: `${selectedAnnotation.value}<br>f1`,
        align: "right",
        format: (c: ClassificationClasses) => formatDecimal(c.metrics.f1),
        sortOn: (c: ClassificationClasses) => c.metrics.f1,
    },
    {
        key: "hypothesis",
        label: "count<br>(hypothesis)",
        align: "right",
        format: (c: ClassificationClasses) => c.hypothesis.toLocaleString(),
        sortOn: (c: ClassificationClasses) => c.hypothesis,
    },
    {
        key: "reference",
        label: "count<br>(reference)",
        align: "right",
        format: (c: ClassificationClasses) => c.reference.toLocaleString(),
        sortOn: (c: ClassificationClasses) => c.reference,
    },
    {
        key: "truePositive",
        label: `${selectedAnnotation.value}<br>true positive`,
        button: true,
        sortOn: (c: ClassificationClasses): number => c.truePositive.count,
    },
    {
        key: "falsePositive",
        label: `${selectedAnnotation.value}<br>false positive`,
        button: true,
        sortOn: (c: ClassificationClasses): number => c.falsePositive.count,
    },
    {
        key: "falseNegative",
        label: `${selectedAnnotation.value}<br>false negative`,
        button: true,
        sortOn: (c: ClassificationClasses): number => c.falseNegative.count,
    },
    { key: "noMatch", label: "no match", button: true, sortOn: (c: ClassificationClasses): number => c.noMatch.count },
])
const grouped = computed<ClassificationClasses & { group: string }>(() => {
    if (!groupedMetrics.value) return []
    return Object.entries(groupedMetrics.value.grouped).map((entry) => ({ ...entry[1], group: entry[0] }))
})
const filteredItems = computed(() => {
    if (selectedAnalysis.value === "single") {
        return grouped.value.filter((i) => !i.group.includes("+"))
    }
    if (selectedAnalysis.value === "multiple") {
        return grouped.value.filter((i) => i.group.includes("+"))
    }
    return grouped.value
})

// Methods
const downloading = ref<boolean>(false)
function download(data: TableData<any>) {
    const classification = data.column.key
    const groupFilter = data.item.group
    downloading.value = true
    API.getMetricsSamples(
        // TODO plausible
        corpusId.value,
        hypothesisId.value,
        referenceId.value,
        selectedAnnotations.value,
        selectedGroup.value,
        classification,
        groupFilter,
    )
        .then((response) => {
            Utils.browserDownloadResponseFile(response)
        })
        .finally(() => (downloading.value = false))
}

// Methods
function formatCamelCase(camelCase: string) {
    return camelCase
        .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
        .toLowerCase()
        .replace(/^./, (c) => c.toUpperCase())
}

// Default select options
watchPostEffect(() => {
    if (!annotationOptions.value?.length) return
    selectedAnnotations.value ??= [annotationOptions.value[0]?.value]
})
watchPostEffect(() => {
    selectedGroup.value ??= groupOptions.value[2]?.value
})
</script>

<style scoped lang="scss">
aside {
    text-align: center;
}
</style>
