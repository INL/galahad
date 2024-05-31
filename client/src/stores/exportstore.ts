// Libraries & stores
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import stores, { AppStore, CorporaStore, JobSelectionStore } from '@/stores'
// Types & API
import * as API from '@/api/export'
import { Format } from '@/types/documents'
import * as Utils from '@/api/utils'

/**
 * Used to download exported corpora.
 */
const useExportStore = defineStore('exportStore', () => {
    // Stores
    const corporaStore = stores.useCorpora() as CorporaStore
    const app = stores.useApp() as AppStore
    const jobSelection = stores.useJobSelection() as JobSelectionStore

    // Fields
    const loading = ref(false)
    const format = ref(null as any as Format) // can we use this both as the export format as the 'import-to-blacklab' format?
    const linksAreValid = computed(() => {
        return corporaStore.activeUUID !== null && jobSelection.hypothesisJobId !== null && format.value !== null
    })

    // Methods
    function convert(shouldMerge: boolean, posHeadOnly: boolean) {
        if (shouldMerge) {
            merge(posHeadOnly)
            return
        }
        loading.value = true
        API.convertCorpus(corporaStore.activeUUID, jobSelection.hypothesisJobId, format.value, posHeadOnly)
            .then(Utils.browserDownloadResponseFile)
            .catch(res => Utils.handleBlobError(res, "convert corpus", app))
            .finally(() => loading.value = false)
    }

    function merge(posHeadOnly: boolean) {
        loading.value = true
        API.mergeCorpus(corporaStore.activeUUID, jobSelection.hypothesisJobId, format.value, posHeadOnly)
            .then(Utils.browserDownloadResponseFile)
            .catch(res => Utils.handleBlobError(res, "merge corpus", app))
            .finally(() => loading.value = false)
    }
    // Exports
    return {
        // Fields
        // note: exporting the format seems necessary for reactivity
        format, linksAreValid, loading,
        // Methods
        convert,
    }
})

export default useExportStore
