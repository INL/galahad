<template>
    <GCard>
        <GTable title="Confusion table" :columns :items :loading sortColumn="referenceAnnotation" class="confusion">
            <template #help>
                <p>
                    In part-of-speech confusion, an overview is given of the matches (in green) and mismatches per PoS
                    when comparing the tagging of the hypothesis layer with the reference layer. Click on any frequency
                    below to show a data sample.
                </p>
                <p>
                    The category "MULTIPLE" contains combined tags like "ADP+NOU" or "VRB+PD+PD". These are shown in one
                    cell, but this does not mean that the taggers agree on the exact tags. Click on the cell or look at
                    the Global Metrics for more details.
                </p>
                <DifferentTagsetsHelp />
            </template>

            <template #header>
                <GForm v-if="commonAnnotations.length">
                    <fieldset>
                        <label for="annotation-select">Annotation</label>
                        <GSelect id="annotation-select" :options="annotationOptions" v-model="selectedAnnotation" />
                    </fieldset>
                </GForm>
                <p v-else>Select a hypothesis layer and a reference layer</p>
            </template>

            <!-- top left header -->
            <template #head-referenceAnnotation>
                {{ hypothesisId }}→<br />
                {{ referenceId }}↓
            </template>

            <!-- custom cell rendering -->
            <template #cell="d: TableData<ConfusionRow>">
                <!-- header column -->
                <span v-if="d.column.key == 'referenceAnnotation'">
                    {{ d.item.referenceAnnotation }}
                    <!-- {{ d.value }} -->
                </span>
                <GButton v-else :disabled="!d.value" :class="cssClass(d)" @click="tableData = d">
                    {{ d.value?.count.toLocaleString() ?? 0 }}
                </GButton>
            </template>
        </GTable>

        <ComparisonModal
            v-if="tableData"
            :evaluationEntry="tableData.value"
            :hypothesisLayer
            :referenceLayer
            :annotations="[selectedAnnotation]"
            :downloading
            @download="() => download(tableData)"
            @hide="tableData = undefined"
        >
            <template #title>
                Samples of <i>{{ hypothesisId }} {{ tableData.column.key }}</i> and
                <i>{{ referenceId }} {{ tableData.item.referenceAnnotation }}</i>
            </template>
        </ComparisonModal>
    </GCard>
</template>

<script setup lang="ts">
import * as API from "@/api/evaluation/confusion"
import * as Utils from "@/api/utils"
import useConfusion from "@/stores/evaluation/confusion"
import useLayers from "@/stores/layers"
import type { Confusion } from "@/types/evaluation/confusion"
import type { EvaluationEntry } from "@/types/evaluation"
import type { Column, TableData } from "@/types/ui/table"
import type { SelectOption } from "@/types/ui/select"
import useCorpora from "@/stores/corpora"

const { commonAnnotations, hypothesisId, referenceId, hypothesisLayer, referenceLayer } = storeToRefs(useLayers())
const { confusion, loading, annotation: selectedAnnotation } = storeToRefs(useConfusion())
const { corpusId } = storeToRefs(useCorpora())

type ConfusionRow = { [key: string]: EvaluationEntry } & { referenceAnnotation: string }

// Form
const annotationOptions = computed(() =>
    // only logical annotations
    commonAnnotations.value.filter(
        (option: SelectOption) => !["lemma", "head", "group", "token"].includes(option.text),
    ),
)

// Gmodal
const tableData = ref<TableData<any>>()
const downloading = ref<boolean>()

// Table data
const columns = computed((): Column<ConfusionRow>[] => {
    if (!confusion.value) return []
    const cols = [...new Set(Object.values(confusion.value).flatMap((c: Confusion) => Object.keys(c)))]
        // GTable sort also uses localeCompare. Just using sort() as is messes up the order
        // between e.g. 'NOU' & 'NO_POS'. I don't know why, though.
        .sort((a, b) => {
            if (posToBottom(a)) return 1
            if (posToBottom(b)) return -1
            return a.localeCompare(b)
        })
        .map((key) => ({ key, button: true }))
    cols.unshift({
        key: "referenceAnnotation",
        sortOn: (value: any) => {
            const pos = value.referenceAnnotation
            return posToBottom(pos) ? undefined : pos
        },
    })
    return cols
})
const items = computed((): ConfusionRow[] => {
    if (!confusion.value) return []
    return Object.entries(confusion.value).map((entry) => ({ ...entry[1], referenceAnnotation: entry[0] }))
})

// Methods
// TODO might move to confusion store
function download(data: TableData<any>) {
    const hypothesisPos = data.column.key
    const referencePos = data.item.referenceAnnotation
    downloading.value = true
    API.getConfusionSamples(
        // TODO plausible
        corpusId.value,
        hypothesisId.value,
        referenceId.value,
        selectedAnnotation.value,
        hypothesisPos,
        referencePos,
    )
        .then((response) => {
            Utils.browserDownloadResponseFile(response)
        })
        .finally(() => (downloading.value = false))
}
/** Case insensitive string compare. */
function strEqual(a: string, b: string) {
    return a.toUpperCase() === b.toUpperCase()
}

/** returns whether this pos should be sorted to the bottom. */
function posToBottom(pos: string) {
    const posses = ["NO_POS", "MISSING_MATCH", "OTHER", "LET", "PUNCT", "PC", "MULTIPLE"]
    return posses.includes(pos)
}

function cssClass(d: TableData<any>) {
    const match: boolean = strEqual(d.column.key, d.item.referenceAnnotation)
    const colorless = ["MISSING_MATCH"]
    if (colorless.includes(d.column.key) || colorless.includes(d.item.referenceAnnotation)) {
        return { plain: true }
    }
    const warnings = ["NO_POS", "MULTIPLE"]
    if (warnings.includes(d.column.key) || warnings.includes(d.item.referenceAnnotation)) {
        return { orange: match, plain: !match }
    }
    return { green: match, plain: !match }
}

// Default select options
watchPostEffect(() => {
    selectedAnnotation.value ??= annotationOptions.value[0]?.value
})
</script>
