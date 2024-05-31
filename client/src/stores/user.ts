// Libraries & stores
import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import stores, { CorporaStore } from '@/stores'
// API & Types
import { CorpusMetadata } from '@/types/corpora'
import { User } from '@/types/user'
import * as API from '@/api/application'

/**
 * User store & permissions checks.
 */
const useUser = defineStore('user', () => {
    // Stores
    const corporaStore = stores.useCorpora() as CorporaStore

    // Fields
    const user = ref({ id: 'NO USER', admin: false } as User)
    const hasWriteAccess = computed((): boolean => {
        return corporaStore.userIsCollaborator || user.value.admin || corporaStore.activeCorpus?.owner === user.value.id
    })
    const hasDeleteAccess = computed((): boolean => {
        return canDelete(corporaStore.activeCorpus)
    })

    // Methods
    /**
     * Whether the user can delete the corpus. (Has to be either owner or admin)
     * @param corpus Corpus metadata.
     */
    function canDelete(corpus: CorpusMetadata | null): boolean {
        if (!corpus) return false
        return corpus.owner === user.value.id || user.value.admin
    }

    /**
     * Poll for the user account. On error, refresh page.
     */
    function fetchUser() {
        API.getUser().then(response => {
            user.value = response.data
        }).catch(_ => {
            // On error we wait 5 seconds then reload the page to force a new login
            setTimeout(() => { console.log('Reloading page'); window.location.reload() }, 5000)
        })
    }

    // Exports
    return {
        // Fields
        user, hasWriteAccess, hasDeleteAccess,
        // Methods
        fetchUser, canDelete,
    }
})

export default useUser
