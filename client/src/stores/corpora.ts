import * as API from "@/api/corpora"
import { plausible } from "@/ts/plausible"
import type { CorpusMetadata, MutableCorpusMetadata, UUID } from "@/types/corpora"
import { useRouteQuery } from "@vueuse/router"
import useUser from "@/stores/static/user"

/** Contains all corpora for which the user has read access. */
const useCorpora = defineStore("corpora", () => {
    // Stores
    const { user } = storeToRefs(useUser())

    const canWrite = computed<boolean>((): boolean => user.value?.admin || isOwner.value || isCollaborator.value)
    const canDelete = computed<boolean>((): boolean => user.value?.admin || isOwner.value)

    // Fields
    const corpusId = useRouteQuery<UUID>("corpus")
    const loading = ref<boolean>(false)
    const corpora = ref<CorpusMetadata[]>([])
    const corpus = computed<CorpusMetadata | undefined>((): CorpusMetadata | undefined =>
        corpora.value?.find((i) => i.uuid === corpusId.value),
    )
    const isCollaborator = computed((): boolean => corpus.value?.collaborators?.includes(user.value?.name) ?? false)
    const isOwner = computed<boolean>((): boolean => corpus.value?.owner === user.value?.name)

    /** Reload all corpora. */
    function reload(silent: boolean = false): void {
        loading.value = !silent
        API.getCorpora()
            .then((res) => (corpora.value = res.data))
            .finally(() => (loading.value = false))
    }

    /** Create a new corpus with the given metadata and set it as active. */
    function create(metadata: MutableCorpusMetadata): void {
        loading.value = true
        API.postCorpus(metadata)
            .then((res) => (corpusId.value = res.data))
            .then(() => {
                plausible.corpus.created(metadata)
            })
            .finally(reload)
    }

    /** Delete and unselect corpus. */
    function remove(metadata: CorpusMetadata): void {
        loading.value = true
        API.deleteCorpus(metadata.uuid)
            .then(() => (corpusId.value = undefined))
            .then(() => {
                plausible.corpus.deleted(metadata)
            })
            .finally(reload)
    }

    /** Update metadata of existing corpus. */
    function update(metadata: CorpusMetadata): void {
        loading.value = true
        API.updateCorpus(metadata.uuid, metadata)
            .then(() => {
                plausible.corpus.updated(metadata)
            })
            .finally(reload)
    }

    return {
        corpora,
        loading,
        corpus,
        corpusId,
        isCollaborator,
        isOwner,
        canWrite,
        canDelete,
        reload,
        create,
        remove,
        update,
    }
})

export default useCorpora
