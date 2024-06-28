<template>
    <AnnotateTab hideAnnotationsError>
        <GTable :title="`Jobs for corpus ${corporaStore.activeCorpus?.name}`" helpSubject="jobs" :columns
            :items="displayJobs" :loading="jobsStore.loading" fill hoverRow sortedByColumn="id" :sortDesc="false"
            class="jobsview">

            <template #help>
                <component :is="help.jobs"></component>
            </template>
            <template #table-empty-instruction>
                <p v-if="Object.keys(jobsStore.taggableJobs).length">No results for current filter settings</p>
                <div v-else>No taggers showed up? Something went wrong! Please contact support.</div>
            </template>

            <!-- id cell -->
            <template #cell-id="d">
                <ExternalLink v-if="d.item.tagger.id !== SOURCE_LAYER"
                    :href="`/galahad/overview/taggers#${d.item.tagger.id}`">
                    {{ d.item.tagger.id }}
                </ExternalLink>
                <div v-else>
                    <span style="font-weight: bold">{{ d.item.tagger.id }}</span>
                </div>
            </template>

            <!-- tagset cell -->
            <template #cell-tagset="d">
                <div v-if="!d.item.tagger.tagset"><i>Unknown</i></div>
                <div v-else>{{ d.item.tagger.tagset }}</div>
            </template>

            <!-- produces cell -->
            <template #cell-produces="d">
                {{ sort_tagger_produces(d.item.tagger.produces).join(", ") }}
                <i v-if="d.item.tagger.produces.length === 0">None</i>
            </template>

            <!-- result summary cell -->
            <template #cell-resultSummary="d">
                <!-- <span v-for="key in Object.keys(d.value)" :key="key"><span :key="key" v-if="d.value[key] > 0">{{ key }}: {{ d.value[key] }}, </span></span> -->
                {{ d.value.numWordForms }}
            </template>

            <!-- era cell -->
            <template #cell-era="d">
                <div style="white-space: nowrap"><b v-if="eraRange[0] <= d.item.tagger.eraFrom">{{ d.item.tagger.eraFrom
                        }}</b><span v-else>{{ d.item.tagger.eraFrom }}</span>
                    &ndash;
                    <b v-if="eraRange[1] >= d.item.tagger.eraTo">{{ d.item.tagger.eraTo }}</b><span v-else>{{
                        d.item.tagger.eraTo
                    }}</span>
                </div>
            </template>

            <!-- last modified cell -->
            <template #cell-lastModified="d">
                <span style="white-space:nowrap">{{ unixToString(d.item.lastModified) }}</span>
            </template>

            <!-- progress cell -->
            <template #cell-progress="d">
                <span>
                    <!-- note that percentage is calculated based on num documents, ie not very accurate -->
                    {{ d.item.progress.total === 0 ? '' : formatProgress(d.item.progress) }}
                    <span v-if="d.item.progress.hasError" style="color: var(--int-red)">error !!</span>
                </span>
                <GSpinner class="spinner" small v-show="d.item.progress.busy" />
            </template>

            <!-- actions cell -->
            <template v-slot:cell-actions="d">
                <GNav v-if="!corporaStore.hasDocs" :route="{ path: '/annotate/data/documents' }">
                    Upload documents to start job
                </GNav>
                <GButton v-else @click="jobId = d.item.tagger.id"> View &amp; Tag
                </GButton>
            </template>

            <template #prepend>

                <div class="table-controls">

                    <div class="table-control">
                        Search tagger name:
                        <GInput type="text" v-model="taggerNameFilter" placeholder="Tagger name" clearBtn></GInput>
                    </div>

                    <div class="table-control">
                        Tagset, any of:
                        <div v-for="tagset in tagsets" :key="tagset" style="white-space: nowrap;">
                            <GInput type="checkbox" v-model="includeTagset[tagset]"> {{ tagset || 'Unknown' }} </GInput>
                        </div>
                    </div>

                    <div class="table-control">
                        Require type:
                        <div v-for="type in types" :key="type" style="white-space: nowrap;">
                            <GInput type="checkbox" v-model="requireType[type]"> {{ type }}</GInput>
                        </div>
                    </div>

                    <div class="table-control slider">
                        Era range: {{ eraRange[0] }} &ndash; {{ eraRange[1] }}
                        <vue-slider ref="slider" class="vue-slider" v-model="eraRange" :interval="50" :min="500"
                            :max="2050" :min-range="1" lazy :marks="{
                                '500': '500',
                                '1000': '1000',
                                '1500': '1500',
                                '2000': '2000',
                            }" piecewise :piecewise-filter="x => x.label % 250 === 0" piecewise-label process-dragable
                            :tooltip-dir="['bottom', 'top']">
                        </vue-slider>
                    </div>

                </div>

                <p>
                    Shown <b>{{ displayJobs.length }}</b> of <b>{{ Object.keys(jobsStore.taggableJobs).length }}</b>
                    taggers.
                </p>
            </template>

        </GTable>


        <!-- job modal -->
        <JobModal :show="!!jobId" v-if="jobId" :jobId="jobId" @hide="jobId = null" />

    </AnnotateTab>
</template>

<script setup lang="ts">
// Libraries & stores
import { computed, onMounted, ref, watch } from 'vue'
import VueSlider from 'vue-slider-component'
import 'vue-slider-component/theme/default.css'
import stores, { DocumentsStore, JobsStore, UserStore, CorporaStore } from '@/stores'
// API & types
import { Job, Progress, SOURCE_LAYER } from '@/types/jobs'
import { Field } from '@/types/table'
import { sort_tagger_produces } from "@/stores/taggers"
// Components
import { GButton, GNav, GTable, GInput, GSpinner, AnnotateTab, JobModal } from '@/components'
import help from '@/components/help'

// Stores
const userStore = stores.useUser() as UserStore
const documentsStore = stores.useDocuments() as DocumentsStore
const jobsStore = stores.useJobs() as JobsStore
const corporaStore = stores.useCorpora() as CorporaStore

// Fields
const taggerNameFilter = ref('')
const includeTagset = ref({} as { [tagset: string]: boolean })
const requireType = ref({} as { [type: string]: boolean })
const eraRange = ref([500, 2050])
const jobId = ref(null as null | string)

const displayJobs = computed(() =>
    Object.values(jobsStore.taggableJobs as Job[])
        .filter((job) => {
            // Case insensitive string comparison.
            return job.tagger.id.toLowerCase().includes(taggerNameFilter.value.toLowerCase())
        })
        .filter(job => {
            return (eraRange.value[0] <= job.tagger.eraTo) &&
                (eraRange.value[1] >= job.tagger.eraFrom)
        })
        .filter(job => {
            return includeTagset.value[job.tagger.tagset]
        })
        .filter(job => {
            let pass = true
            Object.keys(requireType.value).forEach(key => {
                if (requireType.value[key] && !(job.tagger.produces.includes(key))) {
                    pass = false
                }
            })
            return pass
        })
)

const tagsets = computed(() => {
    return Object.values(jobsStore.taggableJobs)
        .map((x: Job) => x.tagger.tagset)
        .filter((val, ind, arr) => arr.indexOf(val) === ind) // unique values
        .sort()
})

const columns = computed(() => {
    const publicFields = [
        { key: "id", label: "tagger", sortOn: x => x.tagger.id, textAlign: "left" },
        { key: "tagset", sortOn: x => x.tagger.tagset },
        { key: "produces", label: "type", },
        { key: "resultSummary", label: "tokens", sortOn: x => x.resultSummary.numWordForms },
        { key: "era", label: "period", sortOn: x => x.tagger.eraFrom.toString() + x.tagger.eraTo.toString() },
        { key: "lastModified", label: "last modified", sortOn: x => x.lastModified },
        { key: "progress", sortOn: x => x.progress.finished / x.progress.total },
    ] as Field[];
    if (userStore.hasWriteAccess) {
        return publicFields.concat(
            { key: "actions" },
        )
    } else {
        return publicFields
    }
})

const types = computed(() => {
    return Object.values(jobsStore.taggableJobs)
        .flatMap((x: Job) => x.tagger.produces)
        .filter((val, ind, arr) => arr.indexOf(val) === ind) // unique values
        .sort()
})

// Watches & mounts
onMounted(() => {
    jobsStore.reload()
    // We also need to load the documents, because a time estimate is made based on documents.totalSizeInChars.
    documentsStore.reloadDocumentsForCorpus(corporaStore.activeUUID)
})
watch(tagsets, enableAllTagsets)
onMounted(enableAllTagsets)

// Methods
// Checkmarks
function enableAllTagsets() {
    tagsets.value.forEach(tagset => includeTagset.value[tagset] = true)
}
// Format progress with Math.floor, because e.g. toFixed(0) rounds up 99.9% to 100%, which is confusing.
function formatProgress(progress: Progress) {
    return `${Math.floor(100 * progress.finished / progress.total)}%`
}

function unixToString(time: number) {
    if (time <= 0) {
        return "Never"
    } else {
        return new Date(time).toLocaleString("nl", {
            year: "2-digit",
            month: "2-digit",
            day: "numeric",
            hour: "2-digit",
            minute: "2-digit",
        })
    }
}

</script>

<style scoped>
.spinner {
    position: relative;
    top: 3px;
}

/* Set a width even when there are no results after filtering.*/
:deep(#prepend) {
    width: 900px;
    max-width: 100%;
}

table button {
    background-color: rgba(0, 0, 0, 0);
    border: var(--int-grey) solid 1px;

    &:hover {
        background-color: rgba(0, 0, 0, 0.1);
    }

    &:active {
        background-color: rgba(0, 0, 0, 0.15);
    }
}

/* The slider itself */
:deep(.vue-slider) .vue-slider-rail {
    height: 15px;
    background: var(--int-very-light-grey);
    border-radius: 5px;
    cursor: pointer;
}

:deep(.vue-slider) .vue-slider-process {
    background: var(--int-theme);
}

:deep(.vue-slider) .vue-slider-rail:hover {
    background: var(--int-very-light-grey-hover);
}

:deep(.vue-slider) .vue-slider-rail:hover .vue-slider-process {
    background: var(--int-theme-hover);
}

:deep(.vue-slider) .vue-slider-dot {
    width: 25px !important;
    height: 25px !important;
    z-index: 1;
}

:deep(.vue-slider) .vue-slider-dot-handle {
    background: var(--int-theme);
    border: 2px solid #000;
    box-shadow: none;
}

:deep(.vue-slider) .vue-slider-dot-handle:hover {
    background: var(--int-theme-hover);
}

:deep(.vue-slider) .vue-slider-dot-handle:active {
    background: var(--int-theme-active);
}

:deep(.vue-slider) .vue-slider-dot-tooltip-inner {
    background: var(--int-theme);
    color: var(--black);
    border-color: var(--int-theme);
}
</style>