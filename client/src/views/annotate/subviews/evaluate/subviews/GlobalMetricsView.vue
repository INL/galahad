<template>
    <div>
        <MetricsTable title="Basic Global Metrics" :loading :columns :items="basicItems"
            @download="(data) => download(data)" :downloading>
            <template #help>
                <p>In Global Metrics an overview is given of the (dis)agreement between the two layers that have been
                    selected for lemma and PoS comparison. Click on the (dis)agreement value to show a data sample.</p>

            </template>
        </MetricsTable>

        <MetricsTable title="Extended Global Metrics" :loading :columns :items="complexItems" noHelp
            @download="(data) => download(data)" :downloading />

        <EvaluationInfoBox :eval="metrics" />
    </div>
</template>

<script setup lang='ts'>
// Libraries & stores
import { storeToRefs } from 'pinia'
import { computed, ref } from 'vue'
import stores, { CorporaStore, AppStore, JobSelectionStore } from "@/stores"
// API & types
import { metricsPerPosColumns } from '@/stores/evaluation/metrics'
import * as API from '@/api/evaluation'
import * as Utils from "@/api/utils"
// Components
import MetricsTable from '@/components/tables/MetricsTable.vue'
import { EvaluationInfoBox } from '@/components'

// Types
type GlobalMetricsRow = {
    id: string
    name: string
    group: string
    count: number
    truePositive: number
    falseNegative: number
    noMatch: number
    macroPrecision: number
    macroRecall: number
    macroF1: number
    microAccuracy: number
}

// Stores
const { loading, metrics } = storeToRefs(stores.useMetrics())
const corporaStore = stores.useCorpora() as CorporaStore
const jobSelection = stores.useJobSelection() as JobSelectionStore
const app = stores.useApp() as AppStore

// Fields
const downloading = ref(false)
const columns = computed(() => {
    const withoutName = metricsPerPosColumns.filter((col) => !["precision", "recall", "f1", "falsePositive", "name"].includes(col.key))
    const addColumns = [
        { key: "name", label: "annotation", sortOn: x => x.annotation },
        { key: "group", label: "grouped by", sortOn: x => x.group },
        { key: "macroPrecision", label: "macro\nprecision", sortOn: x => x.macroPrecision },
        { key: "macroRecall", label: "macro\nrecall", sortOn: x => x.macroRecall },
        { key: "macroF1", label: "macro\nf1", sortOn: x => x.macroF1 },
        { key: "microAccuracy", label: "micro\naccuracy", sortOn: x => x.microAccuracy }
    ]
    return addColumns.concat(withoutName)
})
const items = computed(() => {
    if (metrics.value?.metrics == null)
        return []
    else {
        // metrics has the form
        // { pos: { f1, recall, ... }, lemma: { f1, recall, ... }, lemmaPos: { f1, recall, ... } }
        // We want to transform this to
        // [ { name: "PoS", f1, recall, ... }, { name: "Lemma", f1, recall, ... }, { name: "Lemma & PoS", f1, recall, ... } ]
        const ret = Object.keys(metrics.value.metrics).map((key) => ({ name: key, ...metrics.value.metrics[key] })).map((i) => {
            const annoAndGroup = annotationAndGroupFromName(i.name)
            return {
                id: i.setting.id,
                name: i.setting.annotation,
                group: i.setting.group,
                count: i.classes.classCount,
                truePositive: i.classes.truePositive,
                falseNegative: i.classes.falseNegative,
                noMatch: i.classes.noMatch,
                macroPrecision: i.macro.precision,
                macroRecall: i.macro.recall,
                macroF1: i.macro.f1,
                microAccuracy: i.micro.accuracy,
            }
        })
        return ret
    }
})
const basicItems = computed(() => items.value.filter(basicMetricFilter))
const complexItems = computed(() => items.value.filter((item) => !basicMetricFilter(item)))

// Methods
function annotationAndGroupFromName(name: string) {
    const names = name.split("By")
    const annotation = splitCamelCase(names[0]).toLowerCase().split(" ")
    const group = names[1].toLowerCase()
    return { annotation: annotation, group: group }
}

function splitCamelCase(s: string) {
    return s.split(/(?=[A-Z])/).join(" ")
}

function download(data: Any) {
    const classType = data.field.key
    const setting = data.item.id

    downloading.value = true
    API.getMetricsSamples(corporaStore.activeUUID, jobSelection.hypothesisJobId, jobSelection.referenceJobId, setting, classType)
        .then((response) => {
            Utils.browserDownloadResponseFile(response)
        })
        .catch(res => Utils.handleBlobError(res, "download global metrics samples", app))
        .finally(() => downloading.value = false)
}

function basicMetricFilter(item: GlobalMetricsRow): boolean {
    return item.group == item.name || item.id.includes("lemmaPos")
}
</script>
