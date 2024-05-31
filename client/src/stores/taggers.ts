// Libraries & stores
import { ref } from 'vue'
import { defineStore } from 'pinia'
import stores, { AppStore } from '@/stores'
// API & types
import { Tagger } from '@/types/taggers'
import * as API from '@/api/taggers'

/**
 * Sort the 'produces' field of the taggers. The order is stochastic when retrieved from the API.
 * For the interface, we want the order to be fixed.
 */
export function sort_tagger_produces(types: string[]): string[] {
    // By pure coincidence, reverse sorting makes the order TOK, POS, LEM, which is acceptable.
    // But we might want a different order at some point.
    return types.sort((a, b) => b.localeCompare(a))
}

/**
 * Stores all available taggers. Mainly informational.
 */
const useTaggers = defineStore('taggers', () => {
    // Stores
    const app = stores.useApp() as AppStore

    // Fields
    const loading = ref(false)
    const taggers = ref([] as Tagger[])

    // Methods
    function reload() {
        loading.value = true
        API.getTaggers()
            .then(response => taggers.value = response.data)
            .catch(error => { app.handleServerError("fetch taggers", error) })
            .finally(() => loading.value = false)
    }

    reload() // load once

    // Exports
    return {
        loading, taggers
    }
})

export default useTaggers
