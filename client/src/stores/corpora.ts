// Libraries & stores
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import stores, { AppStore, UserStore } from '@/stores'
// Types & API
import { CorpusMetadata, MutableCorpusMetadata, UUID } from '@/types/corpora'
import * as API from '@/api/corpora'

/**
 * Contains all corpora for which the user has read access.
 */
const useCorpora = defineStore('corpora', () => {
    // Stores
    const userStore = stores.useUser() as UserStore
    const app = stores.useApp() as AppStore

    // Fields
    const loading = ref(false)
    const activeUUID = ref(null as unknown as UUID) // has to be null for <select> to show its default value
    const allCorpora = ref([] as CorpusMetadata[])
    const datasetCorpora = computed((): CorpusMetadata[] => allCorpora.value.filter(i => i.dataset))
    const publicCorpora = computed((): CorpusMetadata[] => allCorpora.value.filter(i => i.public))
    const sharedCorpora = computed((): CorpusMetadata[] => allCorpora.value.filter(i => !i.public && i.owner != userStore.user.id))
    const activeCorpus = computed((): CorpusMetadata | null => {
        const candidates = allCorpora.value.filter(x => x.uuid == activeUUID.value)
        return (candidates.length === 1 ? candidates[0] : null)
    })
    const hasDocs = computed((): boolean => {
        return (activeCorpus.value?.numDocs ?? 0) > 0
    })
    const userIsCollaborator = computed((): boolean => {
        return activeCorpus.value?.collaborators.includes(userStore.user.id) ?? false
    })

    // Methods
    /** 
     * Only one corpus operation is allowed to run at a time
     * Is a corpus operation running? If, not request to start a new operation
     * It is the responsibility of the operation to call reload which will release loading.value after finishing
     */
    function corpusOperationLock(): boolean {
        if (loading.value) { return true } else { loading.value = true; return false }
    }

    /**
     * Fetch all corpora for which the user has read access.
     */
    function reload() {
        loading.value = true // this will block any other operations
        API.getCorpora()
            .then(response => allCorpora.value = response.data || [])
            .catch(error => { allCorpora.value = []; app.handleServerError("get corpora", error) })
            .finally(() => loading.value = false)
    }

    /**
     * Create a new corpus with the given metadata and set it as active.
     * @param metadata Metadata of the new corpus.
     */
    function createCorpus(metadata: MutableCorpusMetadata) {
        if (corpusOperationLock()) return
        API.postCorpus(metadata)
            // Automatically set the new corpus as active.
            .then(response => activeUUID.value = response.data)
            .catch(error => app.handleServerError("create corpus", error))
            .finally(reload)
    }

    /**
     * Delete and unselect corpus.
     * @param metadata Corpus to delete.
     */
    function deleteCorpus(metadata: CorpusMetadata) {
        if (corpusOperationLock()) return
        API.deleteCorpus(metadata.uuid)
            .then(() => {
                // Deselect now deleted corpus
                if (metadata.uuid === activeUUID.value) {
                    activeUUID.value = null as unknown as UUID
                }
            })
            .catch(error => app.handleServerError("delete corpus", error))
            .finally(reload)
    }

    /**
     * Update metadata of existing corpus. Keeps it selected.
     * @param uuid UUID of corpus to update.
     * @param metadata Updated metadata.
     */
    function updateCorpus(uuid: UUID, metadata: MutableCorpusMetadata) {
        if (corpusOperationLock()) return
        API.patchCorpus(uuid, metadata)
            .catch(error => app.handleServerError("update corpus", error))
            .finally(reload)
    }

    // Exports
    return {
        // Fields
        allCorpora, loading, datasetCorpora, publicCorpora, sharedCorpora, activeCorpus, hasDocs, activeUUID, userIsCollaborator,
        // Methods
        createCorpus, deleteCorpus, updateCorpus, reload,
    }
})

export default useCorpora
