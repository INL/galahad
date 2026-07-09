import * as LayerAPI from "@/api/layers"
import * as API from "@/api/jobs"
import { plausible } from "@/ts/plausible"
import { type Job } from "@/types/jobs"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"

const POLL_INTERVAL = 1000

/** Starts, stops and deletes jobs. Polls for job progress. Fetches available jobs. */
const useJobs = defineStore("jobs", () => {
    // Stores
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const { reload: reloadCorpora } = useCorpora()
    const { reload: reloadLayers, resetSelection } = useLayers()

    // Fields
    const loading = ref<boolean>(false)
    const posting = ref<boolean>(false)
    const jobs = ref<Job[]>([])
    const pollers = {} as { [tagger: string]: number }

    // Methods
    /** Reload the available jobs for the current corpus. */
    function reload(silent: boolean = false): void {
        if (!corpusId.value) return
        loading.value = !silent
        API.getJobs(corpusId.value)
            .then((res) => {
                jobs.value = res.data
                jobs.value
                    .filter((j: Job) => j.progress.processing)
                    .forEach((j: Job) => {
                        startPolling(j.tagger.name)
                    })
            })
            .finally(() => (loading.value = false))
    }

    function tag(job: Job): void {
        posting.value = true
        API.postJob(corpusId.value, job.tagger.name)
            .then(() => {
                // Fake it, because at this point all files will still be 'pending'.
                // isBusy however depends on 'processing', so at this point it will still be false.
                // A future poll will probably set it to true.
                startPolling(job.tagger.name) // TODO: this is a problem, because if the state doesn't change, the polling isn't stopped.
            })
            .then(() => {
                plausible.job.started(corpus.value, job)
            })
            .finally(() => {
                posting.value = false
                reloadCorpora(true)
                reload(true)
            })
    }

    function cancel(job: Job): void {
        posting.value = true
        API.cancelJob(corpusId.value, job.tagger.name)
            .then(() => {
                plausible.job.stopped(corpus.value, job)
            })
            .finally(() => {
                posting.value = false
                reloadCorpora(true)
                reload(true)
            })
    }

    function remove(job: Job): void {
        posting.value = true
        LayerAPI.removeLayer(corpusId.value, job.tagger.name)
            .then(() => {
                plausible.job.deleted(corpus.value, job)
            })
            .finally(() => {
                posting.value = false
                reload(true)
                reloadCorpora(true)
                reloadLayers()
                resetSelection()
                stopPolling(job.tagger.name)
            })
    }

    /** Start a continuous progress poller for the given job*/
    function startPolling(job: string): void {
        if (!(job in pollers)) {
            performPoll(job) // initial poll
            pollers[job] = setInterval(() => {
                performPoll(job)
            }, POLL_INTERVAL)
        }
    }

    /** Fetch the progress for the given job. To be used within a poller. */
    function performPoll(job: string): void {
        API.getJobProgress(corpusId.value, job).then((res) => {
            jobs.value.find((j: Job) => j.tagger.name === job).progress = res.data
            if (!res.data.processing) {
                stopPolling(job)
                reloadCorpora(true)
                reloadLayers()
            }
        })
        // CorpusAPI.getCorpus(corpusId.value).then((res) => {
        //     corpus.value.processing = res.data.processing
        // })
        // LayerAPI.getLayer(corpusId.value, job).then((res) => {})
    }

    function stopPolling(job: string): void {
        clearInterval(pollers[job])
        delete pollers[job]
    }

    function stopAllPollers(): void {
        Object.keys(pollers).forEach((job) => stopPolling(job))
    }

    watch(corpusId, reload)
    watch(corpusId, stopAllPollers)

    // Exports
    return {
        // Fields
        jobs,
        loading,
        posting,
        // Methods
        tag,
        cancel,
        reload,
        remove,
    }
})

export default useJobs
