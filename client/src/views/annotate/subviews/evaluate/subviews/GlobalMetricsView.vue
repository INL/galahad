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

const columns: Column<GlobalMetrics>[] = computed(() => [
    { key: "annotation", format: (g: GlobalMetrics) => g.settings.annotations.join(", ") },
    { key: "group", format: (g: GlobalMetrics) => g.settings.group },
    {
        key: "microAccuracy",
        label: `micro<br>accuracy`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.micro.accuracy),
        sortOn: (g: GlobalMetrics) => g.micro.accuracy,
    },
    {
        key: "microF1",
        label: `micro<br>f1`,
        align: "right",
        format: (g: GlobalMetrics) => formatDecimal(g.micro.f1),
        sortOn: (g: GlobalMetrics) => g.micro.f1,
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
    }))
})
</script>
