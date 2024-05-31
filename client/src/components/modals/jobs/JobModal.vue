<template>
    <GModal :show="show" :title="`Tag job ${job.tagger.id}`" @hide="$emit('hide')" :showHelp="false">
        <template #help>
            Here you can start a job to tag the documents in your corpus. This may take a while, depending on the corpus
            size.
            You can also stop and delete existing jobs. A preview of the resulting annotation layer is shown as
            well.
            <br><br>
            The tagger status (pending, busy, error, finished) will be displayed in the status bar.
            Tagging is carried out in the background. You do not need to keep the application open.
            The total number of documents that are being processed is given so as to give an indication of how busy the
            server is.

        </template>

        <!-- Loading screen -->
        <div v-if="taggerIsAvailable == null" class="centerText">
            <h3>Connecting to tagger...</h3>
            <GSpinner medium />
        </div>

        <!-- Content -->

        <template v-else>
            <GInfo error v-if="taggerIsAvailable == false">
                The tagger is currently unavailable. Please try again later.
            </GInfo>

            <!-- Job duration -->
            <template v-else>
                <p class="centerText" v-if="job.progress.untagged > 0">
                    <template v-if="jobIndication != null">
                        GaLAHaD is currently processing <b>{{ jobIndication }}</b> {{ jobIndication == 1 ? 'document' :
                            'documents' }}
                    </template>
                    <template v-else>
                        Calculating current server load...
                    </template>

                </p>
            </template>

            <!-- Actions -->
            <div v-if="jobsStore.posting" class="centerText">
                <!-- Show load icon while posting an action-->
                <GSpinner medium />
            </div>
            <div class="buttons" v-else-if="taggerIsAvailable">
                <GButton green :disabled="job.progress.pending === 0 || job.progress.busy"
                    @click="jobsStore.tag(job.tagger.id); healthLoading = true">
                    Start
                </GButton>
                <GButton orange :disabled="!job.progress.busy"
                    @click="jobsStore.cancel(job.tagger.id); healthLoading = true">
                    Stop
                </GButton>
                <GButton red :disabled="job.progress.untagged === job.progress.total && !job.progress.hasError"
                    @click="deleteJobId = job.tagger.id">
                    Delete
                </GButton>
            </div>

            <!-- progress -->
            <div class="progress">
                <ProgressSegment label="failed" color="var(--int-red)" :total="job.progress.total"
                    :value="job.progress.failed" />
                <ProgressSegment label="finished" color="var(--int-green)" :total="job.progress.total"
                    :value="job.progress.finished" />
                <ProgressSegment label="processing" color="var(--int-light-grey)" :total="job.progress.total"
                    :value="job.progress.processing" />
                <!-- When busy, consider untagged documents pending. -->
                <!-- Confusingly, the API already calls them pending, though. -->
                <ProgressSegment label="pending" color="var(--int-very-light-grey)" :total="job.progress.total"
                    :value="job.progress.busy ? job.progress.pending : 0" />
                <!-- Otherwise, just untagged. -->
                <ProgressSegment label="untagged" color="var(--int-very-light-grey)" :total="job.progress.total"
                    :value="job.progress.busy ? 0 : job.progress.pending" />
            </div>

            <!-- Layer preview -->
            <GCard noHelp title="Preview" style="text-align: center">
                <LayerViewer :layer="job.preview" :uid="job.tagger.id" />
            </GCard>

            <!-- errors -->
            <GInfo :error="job.progress.failed > 0" v-if="job.progress.failed > 0">
                The following
                {{ job.progress.failed == 1 ? "document" : "documents" }} encountered errors:<br /><br />
                <ol>
                    <li v-for="(message, doc) in firstFive(job.progress.errors)" :key="doc">
                        <b>{{ doc }}</b>:<br />
                        {{ message }}
                    </li>
                </ol>
                <div v-if="job.progress.failed > 5">
                    ... and {{ job.progress.failed - 5 }} more errors are omitted.
                </div>
                <div v-if="job.progress.failed === 0">None</div>
            </GInfo>
        </template>

        <!-- delete job modal -->
        <DeleteModal :show="!!deleteJobId" :item="deleteJobId" :displayname="`the results of job ${deleteJobId}`"
            @delete="jobsStore.deleteJob(deleteJobId); healthLoading = true" @hide="deleteJobId = null as any" />
    </GModal>
</template>

<script setup lang='ts'>
// Libraries & stores
import { computed, ref, onMounted, onUnmounted } from "vue"
import stores, { JobsStore, AppStore } from "@/stores"
// API & types
import { TaggerHealth } from "@/types/taggers"
import { Job } from "@/types/jobs"
import * as API from "@/api/taggers"
// Components
import { GButton, GCard, GInfo, GModal, GSpinner, DeleteModal } from "@/components"
import LayerViewer from "@/components/tables/LayerViewer.vue"
import ProgressSegment from "@/components/modals/jobs/ProgressSegment.vue"

// Stores
const app = stores.useApp() as AppStore
const jobsStore = stores.useJobs() as JobsStore

// Fields
const props = defineProps({
    show: { type: Boolean, default: ref(false) },
    jobId: { type: String, default: "" },
})
/** The job of this modal */
const job = computed<Job>(() => {
    return jobsStore.jobs[props.jobId]
})
/** Returns null while we are waiting on the first getHealth request. */
const taggerIsAvailable = computed<boolean | null>(() => {
    if (!health.value) return null
    return health.value?.status === "HEALTHY"
})
/** Opens DeleteModal when not null. */
const deleteJobId = ref((null as any) as string)
/** Expected job duration based on queue size at tagger and % of documents tagged in the corpus. */
const jobIndication = computed(() => {
    if (jobsStore.posting || jobsStore.numActiveDocs == null) {
        return null
    } else {
        return jobsStore.numActiveDocs
    }
})
/** Updated on an interval to keep track of the queue size. */
const health = ref(null as TaggerHealth | null)
/** When true, display job duration as still calculating. */
const healthLoading = ref(true)
/** Keep track of the interval id, we stop polling on modal close. */
let healthIntervalId = 0

// Watches & mounts
/** 
 * Every time this GModal opens: One health ping now, the rest on an interval.
 */
onMounted(() => {
    getHealth()
    healthIntervalId = setInterval(getHealth, 5000)
    // Set to null to induce 'calculating' every time the modal opens.
    jobsStore.getDocsAtTagger()
})
/**
 * Stop pinging health on modal close.
 */
onUnmounted(() => {
    clearInterval(healthIntervalId)
})

// Methods
/**
 * Get the health of the tagger. Called every 5 seconds while the modal is open.
 * Starting and stopping sets health loading to true. getHealth sets it back to false.
 */
function getHealth() {
    API.getTaggerHealth(props.jobId)
        .then((response) => {
            health.value = response.data
            healthLoading.value = false
        }).catch((error) => app.handleServerError("get tagger health", error))
}

/**
 * Return an object with only the first five keys of obj.
 */
function firstFive(obj: Record<string, unknown>) {
    if (!obj) {
        return {}
    }
    return Object.keys(obj)
        .slice(0, 5)
        .reduce(function (r: Record<string, unknown>, e) {
            r[e] = obj[e]
            return r
        }, {})
}
</script>

<style scoped>
.progress {
    max-width: 100%;
    margin: auto;
    margin-top: 10px;
    line-height: 2em;
    width: 700px;
}

.error {
    width: fit-content;
    margin: auto;
}

.buttons {
    height: 100%;
    display: flex;
    justify-content: center;
    align-items: center;
    margin-top: 10px;
}

.centerText {
    text-align: center;
}

.healthy {
    background-color: var(--gold);
    color: black;
}

.unhealthy {
    background-color: var(--int-red);
    color: white;
}
</style>
