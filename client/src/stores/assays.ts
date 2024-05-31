// Libraries & stores
import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import stores, { AppStore } from '@/stores'
// Types & API
import { Assays } from '@/types/assays'
import * as API from '@/api/assays'

/**
 * Contains dataset assays.
 */
const useAssays = defineStore('assays', () => {
    // Stores
    const app = stores.useApp() as AppStore

    // Fields
    const loading = ref(false)
    const assays = ref({} as Assays)

    // Methods
    /**
     * Reload all assays.
     */
    function reload() {
        loading.value = true
        API.getAssays()
            .then(response => assays.value = response.data)
            .catch(error => app.handleServerError("fetch assays", error))
            .finally(() => loading.value = false)
    }

    // Exports
    return {
        // Fields
        assays, loading,
        // Methods
        reload,
    }
})

export default useAssays