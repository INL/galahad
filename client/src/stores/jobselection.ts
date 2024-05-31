// Libraries & stores
import { ref, watch, computed } from 'vue'
import { defineStore } from 'pinia'
import stores, { CorporaStore, JobsStore, DocumentsStore } from '@/stores'
import { Job, SOURCE_LAYER } from '@/types/jobs'

/**
 * Stores the current job selection from the <select>. Used in Evaluation & Export.
 */
const useJobSelection = defineStore('jobSelection', () => {
    // Stores
    const jobsStore = stores.useJobs() as JobsStore
    const corporaStore = stores.useCorpora() as CorporaStore
    const documentsStore = stores.useDocuments() as DocumentsStore

    // Fields
    const hypothesisJobId = ref(null as unknown as string)
    const referenceJobId = ref(null as unknown as string)
    // Set to true once we know the jobs exist in selectableJobs.
    // (which requires waiting on jobs & docs to load)
    const selectionsValid = ref(false)
    const tagsetsMismatch = computed(() => {
        return hypothesisJob.value?.tagger.tagset !== referenceJob.value?.tagger.tagset
    })
    // Selectable jobs are jobs that have at least one finished document, 
    // or have source annotations (i.e. sourceLayer).
    const selectableJobs = computed((): { key: string, value: string, text: string }[] => {
        const jobs = jobsStore.jobs
        if (!jobs) return []
        return Object.keys(jobs)?.filter(job => jobs[job].progress.finished > 0)
            // Filter out sourceLayer if no documents have source annotations.
            .filter((job: string) => !(job == SOURCE_LAYER && !documentsStore.numSourceAnnotations))
            ?.map(job => {
                return {
                    key: job,
                    value: job,
                    text: formatJobString(jobs[job]),
                }
            })
    })

    // Private Fields
    const referenceJob = computed((): Job | null => {
        return referenceJobId.value ? jobsStore.jobs[referenceJobId.value] : null
    })
    const hypothesisJob = computed((): Job | null => {
        return hypothesisJobId.value ? jobsStore.jobs[hypothesisJobId.value] : null
    })

    // Watches
    watch(() => corporaStore.activeUUID, (newValue, oldValue) => {
        if (oldValue !== newValue && oldValue) {
            hypothesisJobId.value = null as any
            referenceJobId.value = null as any
        }
    })
    /** Remove invalid job selections on loading jobs & loading docs (the latter for sourceLayer annotations).*/
    watch([() => jobsStore.loading, () => documentsStore.loading], () => {
        validateJobSelections()
    })

    // Methods
    /** Remove any invalid job selections, either non-existing names or jobs that have no layer  */
    function validateJobSelections() {
        const jobsExist: boolean = !jobsStore.loading && Object.keys(jobsStore.jobs).length > 0
        const docsExist: boolean = !documentsStore.loading && documentsStore.available.length > 0
        if (jobsExist && docsExist) {
            if (!selectableJobs.value.map(job => job.key).includes(hypothesisJobId.value)) {
                hypothesisJobId.value = null as any
            }
            if (!selectableJobs.value.map(job => job.key).includes(referenceJobId.value)) {
                referenceJobId.value = null as any
            }
            selectionsValid.value = true
        }
    }
    /** Format as displayed in the <select> */
    function formatJobString(job: Job) {
        let finished = job.progress.finished
        if (job.tagger.id == SOURCE_LAYER) {
            finished = documentsStore.numSourceAnnotations
        }
        return `${job.tagger.id} (${job.tagger.description}) [${finished}/${job.progress.total} documents]`
    }
    function setHypothesisJobId(id: string) {
        if (Object.keys(jobsStore.jobs)?.includes(id)) hypothesisJobId.value = id
    }
    function setReferenceJobId(id: string) {
        if (Object.keys(jobsStore.jobs)?.includes(id)) referenceJobId.value = id
    }

    // Exports
    return {
        // Fields
        tagsetsMismatch, hypothesisJobId, referenceJobId, selectableJobs, selectionsValid,
        // Methods
        setHypothesisJobId, setReferenceJobId,
    }
})

export default useJobSelection

