import { plausible } from "@/ts/plausible"
import * as API from "@/api/documents"
import * as Utils from "@/api/utils"
import { type DocumentMetadata } from "@/types/documents"
import { addContentTypeHeader, fileExtension } from "@/ts/file"
import useCorpora from "@/stores/corpora"
import useLayers from "@/stores/layers"
import { SOURCE_LAYER } from "@/types/jobs"
import useJobs from "@/stores/jobs"

// Custom types
type FileStatus = { status: "busy" | "success" | "error"; message?: string }

/**
 * Contains the documents for the current corpus,
 * as well as functionality for uploading, deleting and downloading documents.
 */
const useDocuments = defineStore("documents", () => {
    // Stores
    const { corpusId, corpus } = storeToRefs(useCorpora())
    const { reload: reloadLayers } = useLayers()
    const { reload: reloadCorpora } = useCorpora()
    const { reload: reloadJobs } = useJobs()

    // Fields
    const loading = ref<boolean>(false)
    const documents = ref<DocumentMetadata[]>([])
    const uploading: Record<string, FileStatus> = reactive({})
    const uploadBusyCount = computed(() => Object.values(uploading).filter((i) => i.status === "busy").length)
    const uploadErrorCount = computed(() => Object.values(uploading).filter((i) => i.status === "error").length)
    const filesToUpload = ref<File[]>([])
    const illegalFiles = computed((): File[] => {
        return filesToUpload.value.filter((x: any) => {
            const ext = x.name.split(".").at(-1)
            return !["xml", "tsv", "txt", "zip", "conllu", "naf", "pdf", "docx"].includes(ext)
        })
    })

    /** Reload documents and corpora (number of docs in corpusmetadata can change). */
    function reload(layer: string | undefined = SOURCE_LAYER): void {
        if (!corpusId.value || !layer) return
        loading.value = true
        API.getDocuments(corpusId.value, layer)
            .then((res) => (documents.value = res.data))
            .finally(() => (loading.value = false))
    }

    /** Delete a document. */
    function remove(name: string): void {
        API.deleteDocument(corpusId.value, name)
            .then(() => {
                plausible.document.deleted(corpus.value, getDocument(name))
            })
            .finally(() => {
                reload()
                reloadLayers()
                reloadCorpora()
                reloadJobs()
            })
    }

    /** Download original source document. */
    function download(name: string): void {
        API.getRawDocument(corpusId.value, name)
            .then(Utils.browserDownloadResponseFile)
            .then(() => {
                plausible.document.downloaded(corpus.value, getDocument(name))
            })
    }

    function getDocument(name: string): DocumentMetadata {
        return documents.value.find((d: DocumentMetadata) => d.name === name) as DocumentMetadata
    }

    /** Upload all files in filesToUpload. Creates timeouts to spread load. */
    function uploadAll(): void {
        for (let i = 0; i < filesToUpload.value.length; i++) {
            const formData = new FormData()
            const file = filesToUpload.value[i]
            // if( file.size > MAX_FILE_SIZE ) continue // skip too large files
            formData.append("file", file)
            uploading[file.name] = { status: "busy" }
            // Spread the uploads a little
            setTimeout(() => upload(formData), (i / 10) * 100)
        }
        filesToUpload.value = []
    }

    /** Clear errors from not yet uploaded files. */
    function clearUploadErrors(): void {
        Object.keys(uploading).forEach((key) => {
            if (uploading[key].status === "error") delete uploading[key]
        })
    }

    /**
     * Upload a single file. Takes http content type header into account.
     * @param formData FormData with file to upload.
     */
    function upload(formData: FormData): void {
        const file = formData.get("file") as File

        // Some files need an explicit content type header.
        const header = addContentTypeHeader(formData)

        // Update status on upload, on success and on error.
        uploading[file?.name] = { status: "busy" }
        API.postDocument(corpusId.value, formData, header)
            .then(() => {
                uploading[file?.name] = { status: "success" }
            })
            .then(() => {
                plausible.document.uploaded(corpus.value, fileExtension(file))
            })
            .catch((error) => (uploading[file.name] = { status: "error", message: error.response.data.message }))
            .finally(() => {
                if (uploadBusyCount.value === 0) {
                    reload()
                    reloadCorpora()
                    reloadLayers()
                    reloadJobs()
                }
            })
    }

    watch(corpusId, () => reload())

    // Exports
    return {
        // Fields
        documents,
        filesToUpload,
        illegalFiles,
        loading,
        uploading,
        uploadBusyCount,
        uploadErrorCount,
        // Methods
        reload,
        remove,
        download,
        uploadAll,
        clearUploadErrors,
    }
})

export default useDocuments
