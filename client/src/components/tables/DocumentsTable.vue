<template>
    <div>

        <GTable :columns :items="documentsStore.available" :loading="documentsStore.loading" :displayOnEmpty="false"
            sortedByColumn="name" :sortDesc="false" hoverRow>
            <template #title>
                <span v-if="!corpus || (type == TableDocumentsType.Dataset && !corpus.dataset)">
                    No documents
                </span>
                <span v-else>
                    {{ documentsStore.available.length }}
                    {{ documentsStore.available.length === 1 ? 'document' : 'documents' }}
                    in corpus <i>{{ corpus.name }}</i>
                </span>
            </template>

            <template #help>
                <slot name="help">
                    <component :is="help.documents"></component>
                </slot>
            </template>

            <template #table-empty-instruction>
                <span v-if="!corpus || (type == TableDocumentsType.Dataset && !corpus?.dataset)">No corpus
                    selected.</span>
                <span v-else-if="corpus?.uuid && type != TableDocumentsType.Dataset" style="margin-top:10px">
                    This corpus is empty. Upload documents to the corpus.
                </span>
            </template>

            <template #header>
                <UploadDocuments v-if="userStore.hasWriteAccess && type != TableDocumentsType.Dataset" />
            </template>

            <!-- valid cell -->
            <template #cell-valid="data">
                {{ data.value ? '👍' : '🔥' }}
            </template>

            <!-- name cell -->
            <template #cell-name="data">
                <div style="max-height: 3em; line-break: anywhere; overflow: hidden; min-width:80px">{{ data.value }}
                </div>
            </template>

            <!-- size cell -->
            <template #cell-size="data">
                {{ data.value }}
            </template>

            <template #head-layerSummary>
                source annotations<br>
                (token / PoS / lemma)
            </template>
            <!-- layerSummary cell -->
            <template #cell-layerSummary="data">
                <RightFloatCell>
                    <template #left>
                        {{ data.value.numWordForms }} /
                        {{ data.value.numPOS }} /
                        {{ data.value.numLemma }}
                    </template>
                    <template #right>
                        <InspectButton v-if="data.value.numWordForms > 0" @click="previewDocument = data.item" />
                    </template>
                </RightFloatCell>
            </template>

            <!-- plain text preview cell -->
            <template #cell-preview="data">
                <div style="min-width: 200px; max-height: 3em; overflow: hidden;">{{ data.value }}</div>
            </template>

            <!-- size in bytes cell -->
            <!-- <template #cell-sizeInBytes="data">
                <span v-if="data.value > 1023">{{Utils.formatBytes(data.value)}}</span>
                <span v-else>~ 0</span>
            </template> -->

            <!-- last modified cell -->
            <template #cell-lastModified="data">
                <span style="white-space:nowrap">{{ new Date(data.value).toLocaleString("nl", {
                    year: "2-digit",
                    month: "2-digit",
                    day: "numeric",
                    hour: "2-digit",
                    minute: "2-digit",
                }) }}</span>
            </template>

            <!-- actions cell -->
            <template #cell-actions="data">
                <div style="display: flex;">

                    <DownloadButton @click="download(data.item)" />

                    <GButton red @click="deleteDocumentData = data.item; showDeleteModal = true" title="Delete">
                        <i class="fa fa-trash"></i>
                    </GButton>
                </div>
            </template>

        </GTable>

        <!-- preview modal -->
        <GModal :show="previewDocument !== null" @hide="preview = null; previewDocument = null"
            :title="`Preview of document ${previewDocument ? previewDocument.name : ''}`" style="text-align: center">
            <template #title>Source layer preview of document {{ previewDocument ? previewDocument.name : ""
                }}</template>
            <template #help>
                Here you can inspect a small part of the source layer of the document.
            </template>
            <template v-if="loading">
                <p>Loading...</p>
                <GSpinner medium />
            </template>
            <LayerViewer v-else :layer="preview" />

        </GModal>

        <!-- delete modal -->
        <DeleteModal :show="showDeleteModal" :item="deleteDocumentData"
            :displayname="'document ' + (deleteDocumentData !== null ? deleteDocumentData.name : '[null]') + ' and associated results'"
            @hide="showDeleteModal = false" @delete="deleteDocument" />

    </div>
</template>

<script setup lang='ts'>
// Libraries & stores
import { computed, ref, PropType, watch, onMounted } from 'vue'
import stores from '@/stores'
// API & types
import * as API from '@/api/jobs'
import { TableDocumentsType, Field } from '@/types/table'
import { DocumentMetadata } from '@/types/documents'
import { CorpusMetadata } from '@/types/corpora'
import { LayerPreview, SOURCE_LAYER } from '@/types/jobs'
// Components
import { GButton, GModal, GTable, DownloadButton, DeleteModal, RightFloatCell, InspectButton } from '@/components'
import LayerViewer from '@/components/tables/LayerViewer.vue'
import UploadDocuments from '@/components/input/UploadDocuments.vue'
import help from '@/components/help'

// Stores
const app = stores.useApp()
const documentsStore = stores.useDocuments()
const userStore = stores.useUser()

// Props
const props = defineProps({
    type: String as PropType<TableDocumentsType>, // the mode of the table
    corpus: { type: Object as PropType<CorpusMetadata>, default: null },
})

// Fields
const deleteDocumentData = ref(null as null | DocumentMetadata)
const previewDocument = ref(null as null | DocumentMetadata)
const preview = ref(null as null | LayerPreview)
const showDeleteModal = ref(false)
const loading = ref(false)

const columns: Field[] = computed(() => {
    const publicFields = [
        // { key: "valid", sortOn: (x: Document) => x.valid },
        { key: "name", sortOn: (x: DocumentMetadata) => x.name, textAlign: "left" },
        { key: "format", sortOn: (x: DocumentMetadata) => x.format },
        { key: "preview", textAlign: "left" },
        // { key: "sizeInBytes", label: "size", sortOn: (x: Document) => x.sizeInBytes },
        // { key: "numChars", label: "size (chars)", sortOn: (x: DocumentMetadata) => x.numChars },
        { key: "layerSummary" },
        { key: "lastModified", label: "last modified", sortOn: (x: DocumentMetadata) => x.lastModified }
    ] as Field[];
    if (userStore.hasWriteAccess && props.type == TableDocumentsType.User) {
        return publicFields.concat(
            { key: "actions" }
        )
    } else {
        // public
        return publicFields
    }
}
)

// Methods
function deleteDocument(document: DocumentMetadata) {
    return documentsStore.deleteDocument(document.name)
}
function download(document: DocumentMetadata) {
    return documentsStore.downloadRaw(document.name)
}
function loadSourceLayer() {
    loading.value = true
    API.getJobDocumentResult(props.corpus?.uuid, SOURCE_LAYER, previewDocument.value.name)
        .then(response => {
            const encodedDocName = encodeURI(previewDocument.value.name)
            // Only load the preview if the response is for the current document
            if (response.request.responseURL.includes(encodedDocName)) {
                preview.value = response.data.preview
                loading.value = false
            }
        })
        .catch(error => {
            app.handleServerError("get job document result", error)
        })
}

// Watches & mounts
// When previewDocument is not null, the GModal already opens. So also autoload the source layer.
watch(() => previewDocument.value, () => {
    if (previewDocument.value !== null)
        loadSourceLayer()
})
// Reload docs on uuid change (and onMounted). But don't show user docs on dataset tab.
watch(() => props.corpus?.uuid, () => {
    if (props.type == TableDocumentsType.Dataset && !props.corpus?.dataset)
        return
    documentsStore.reloadDocumentsForCorpus(props.corpus?.uuid)
}, { immediate: true })
// Reset any previous selection.
// E.g. when switching between datasets and user corpora.
onMounted(() => {
    documentsStore.available = []
})
</script>
