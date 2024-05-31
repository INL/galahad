// Libraries & stores
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import stores, { AppStore, CorporaStore } from '@/stores'
// API & Types
import { Job, SOURCE_LAYER } from '@/types/jobs'
import * as API from '@/api/jobs'
import { getDocsAtTaggers } from '@/api/taggers'
import { ProgressResponse } from '@/api/jobs'

const POLL_INTERVAL = 5000

/**
 * Starts, stops and deletes jobs. Polls for job progress. Fetches available jobs.
 */
const jobs = defineStore('jobs', () => {
    // Stores
    const app = stores.useApp() as AppStore
    const corporaStore = stores.useCorpora() as CorporaStore

    // Fields
    // Job statuses for the taggers.
    const jobs = ref({} as Record<string, Job>)
    const taggableJobs = computed((): Job[] => {
        return Object.keys(jobs.value).map(x => jobs.value[x]).filter(x => x.tagger.id !== SOURCE_LAYER)
    })
    const loading = ref(false)
    const posting = ref(false)
    const pollers = {} as { [tagger: string]: number }
    const numActiveDocs = ref(null as number | null)

    // Methods
    /** 
     * Fetch the progress for the given job. To be used within a poller.
     * @param job Tagger job name.
     */
    function getProgress(job: string) {
        API.getJobProgress(corporaStore.activeUUID, job)
            .then(response => setProgress(job, response))
            .catch(error => app.handleServerError("fetch job progress", error))
    }

    /**
     * On poll promise resolve, set the progress for the given job.
     */
    const setProgress = (job: string, response: ProgressResponse) => {
        if (response.request.responseURL.includes(corporaStore.activeUUID)) {
            // Only commit the response if it corresponds to the correct corpus
            // This prevents late responses overwriting responses to newer requests
            jobs.value[job].progress = response.data
            // Stop polling if the job is done.
            if (!response.data.busy) {
                stopPolling(job)
                // Displaying the layer preview requires a reload.
                reload()
            }
        } else {
            // fizzle
        }
    }

    /**
     * Start a continuous progress poller for the given job
     * @param job Tagger job name.
     */
    function startPolling(job: string) {
        if (!(job in pollers)) {
            pollers[job] = setInterval((job: string) => { getProgress(job) }, POLL_INTERVAL, job)
        }
    }

    /**
     * Stop polling progress for the given job.
     * @param job Tagger job name.
     */
    function stopPolling(job: string) {
        clearInterval(pollers[job])
        delete pollers[job]
    }

    /**
     * Empty the list of tagger job statuses.
     */
    function reset() {
        jobs.value = {}
    }

    /**
     * Fetch the list of tagger job statuses for the current corpus.
     */
    function reload() {
        if (!corporaStore.activeUUID) {
            reset()
            return
        }
        Object.keys(pollers).forEach(x => stopPolling(x))
        loading.value = true
        // Reload jobs
        API.getJobs(corporaStore.activeUUID, true)
            .then(response => {
                jobs.value = {} // reset the jobs value
                response.data.forEach(job => {
                    jobs.value[job.tagger.id] = job
                    if (job.progress.busy) {
                        // Restart polling any running job
                        startPolling(job.tagger.id)
                    }
                })
            })
            .catch(error => { jobs.value = {}; app.handleServerError("fetch jobs", error) })
            .finally(() => loading.value = false)
    }

    function tag(job: string) {
        posting.value = true
        API.postJob(corporaStore.activeUUID, job)
            .then(response => {
                posting.value = false
                // Fake it, because at this point all files will still be 'pending'. 
                // isBusy however depends on 'processing', so at this point it will still be false.
                // A future poll will probably set it to true.
                response.data.busy = true
                setProgress(job, response)
                startPolling(job) // TODO: this is a problem, because if the state doesn't change, the polling isn't stopped.
                getDocsAtTagger()
            })
            .catch(error => app.handleServerError("post job", error))
    }

    function cancel(job: string) {
        posting.value = true
        API.cancelOrDeleteJob(corporaStore.activeUUID, job, false)
            .then(response => {
                posting.value = false
                setProgress(job, response)
                getDocsAtTagger()
            })
            .catch(error => app.handleServerError("cancel job", error))
    }

    // 'delete' is a reserved keyword
    function deleteJob(job: string) {
        posting.value = true
        API.cancelOrDeleteJob(corporaStore.activeUUID, job, true)
            .then(response => {
                posting.value = false
                setProgress(job, response)
                getDocsAtTagger()
            })
            .catch(error => app.handleServerError("delete job", error))
    }

    /**
     * Get the number of documents processing at all taggers.
     */
    function getDocsAtTagger() {
        numActiveDocs.value = null
        getDocsAtTaggers()
        .then((response) => {
            numActiveDocs.value = response.data
        }).catch((error) => app.handleServerError("get number of active jobs", error))
    }

    // Exports
    return {
        // Fields
        jobs, taggableJobs, loading, posting, numActiveDocs,
        // Methods
        tag, cancel, deleteJob, reload, reset, getDocsAtTagger
    }
})

export default jobs