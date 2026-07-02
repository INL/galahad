<template>
    <AnnotateTab>
        <GTable :columns :items :loading title="Jobs" sortColumn="name">
            <template #help>
                <JobsHelp />
            </template>

            <template #header>
                <GForm>
                    <fieldset>
                        <label for="tagger-name">Search tagger name</label>
                        <GInput id="tagger-name" type="text" v-model="taggerFilter" placeholder="Tagger name"></GInput>
                    </fieldset>

                    <fieldset>
                        <label for="annotation-select">Select annotation</label>
                        <MultiSelect
                            id="annotation-select"
                            v-model="annotationFilter"
                            :options="annotations"
                            placeholder="Annotation"
                            :maxSelectedLabels="5"
                        />
                    </fieldset>

                    <fieldset>
                        <label for="period-select">Select period</label>
                        <Slider
                            style="width: 309px"
                            range
                            id="period-select"
                            v-model="periodFilter"
                            :min="periodStart"
                            :max="2100"
                            :step="100"
                        />
                        <output>{{ periodCorrected[0] }} – {{ periodCorrected[1] }}</output>
                    </fieldset>
                </GForm>
            </template>

            <template #empty>
                <p v-if="jobs.length">No results for current filter settings</p>
                <div v-else>No taggers showed up? Something went wrong! Please contact support.</div>
            </template>

            <template #cell-name="d: TableData<Job>">
                <ExternalLink :href="`/galahad/overview/taggers#${d.item.tagger.name}`">
                    {{ d.item.tagger.name }}
                </ExternalLink>
            </template>

            <template #cell-annotations="d: TableData<Job>">
                <AnnotationItemsViewer :tagger="d.item.tagger" />
            </template>

            <template #cell-documents="d: TableData<Job>">
                <RightFloatCell>
                    <template #left> {{ d.item.progress.finished }}</template>
                    <template #right>
                        <InspectButton v-if="d.item.progress.finished" @click="layerId = d.item.tagger.name" />
                    </template>
                </RightFloatCell>
            </template>

            <template #cell-progress="d: TableData<Job>">
                <GSpinner v-if="d.item.progress.processing" small inline />
                <span v-if="d.item.progress.errors.length" class="error">error</span>
                {{ d.item.progress.total === 0 ? "0%" : formatProgress(d.item.progress) }}
            </template>

            <template v-slot:cell-actions="d: TableData<Job>">
                <GButton @click="jobId = d.item.tagger.name"> <i class="fa fa-cogs"></i> </GButton>
            </template>
        </GTable>

        <GModal v-if="layerId" @hide="layerId = undefined">
            <DocumentsTable :documents :loading="documentsLoading" :layer />
        </GModal>

        <JobModal v-if="jobId" :jobId @hide="jobId = undefined" />
    </AnnotateTab>
</template>

<script setup lang="ts">
import type { Job, Progress } from "@/types/jobs"
import type { Column, TableData } from "@/types/ui/table"
import { formatDate, formatPeriod, formatProgress } from "@/ts/format"
import MultiSelect from "primevue/multiselect"
import Slider from "primevue/slider"
import useJobs from "@/stores/jobs"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"
import type { LayerMetadata } from "@/types/layers"
import useDocuments from "@/stores/documents"

const { loading, jobs } = storeToRefs(useJobs())
const { corpus, canWrite } = storeToRefs(useCorpora())
const { documents, loading: documentsLoading } = storeToRefs(useDocuments())
const { reload: reloadDocuments } = useDocuments()
const { layers } = storeToRefs(useLayers())
const { reload: reloadLayers } = useLayers()

const layer = computed<LayerMetadata>(() => layers.value.find((l: LayerMetadata) => l.tagger.name == layerId.value))

const jobId = ref<Job>()
const layerId = ref<string>()
// Load layer onClick
watch(layerId, () => {
    reloadDocuments(layerId.value)
    reloadLayers()
})

// filters
const taggerFilter = ref<string>("")
const annotationFilter = ref<string[]>([])

// select options
const annotations = computed<string[]>(() => [
    ...new Set(jobs.value.flatMap((j: Job) => j.tagger.annotations.flatMap((a) => a.annotation))),
])
const periodCorrected = computed<number[]>(() => [Math.min(...periodFilter.value), Math.max(...periodFilter.value)])
const periodStart = computed<number>(() => Math.min(...jobs.value.map((j: Job) => j.tagger.period.from)))
// TODO also determine period end from taggers, and round to 100
const periodFilter = ref<number[]>([corpus.value?.period?.from ?? 0, corpus.value?.period?.to ?? 2100])
// table data
const items = computed(() =>
    jobs.value
        // filter tagger name
        .filter((j: Job) =>
            // Case insensitive string comparison.
            j.tagger.name.toLowerCase().includes(taggerFilter.value.toLowerCase()),
        )
        // filter period
        .filter(
            (j: Job) =>
                j.tagger.period.to >= periodCorrected.value[0] && j.tagger.period.from <= periodCorrected.value[1],
        )
        // filter annotations
        .filter((j: Job) =>
            annotationFilter.value?.length > 0
                ? annotationFilter.value.every((a: string) => j.tagger.annotations.map((i) => i.annotation).includes(a))
                : true,
        ),
)
const columns = computed<Column<Job>[]>((): Column<Job>[] => [
    { key: "name", sortOn: (j: Job): string => j.tagger.name, align: "left" },
    { key: "language", format: (j: Job): string => j.tagger.language },
    { key: "period", format: (j: Job): string | undefined => formatPeriod(j.tagger.period) },
    { key: "annotations", sortOn: (j: Job): string => j.tagger.annotations.map((a) => a.annotation).join() },
    { key: "documents", align: "right", format: (j: Job): number => j.progress.finished },
    { key: "modified", align: "center", format: (j: Job): string => formatDate(j.modified) },
    { key: "progress", align: "right", sortOn: (j: Job): number => j.progress.finished / j.progress.total },
    { key: "actions", hidden: !canWrite.value, noSort: true, align: "center" },
])
</script>

<style scoped lang="scss">
.error {
    color: var(--int-red);
}
</style>
