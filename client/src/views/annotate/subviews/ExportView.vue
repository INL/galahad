<template>
    <AnnotateTab :hidePermissionsError="false">
        <GCard :title="`Export corpus ${corporaStore.activeCorpus?.name}`" helpSubject="export">
            <template #help>
                <component :is="help.export"></component>
            </template>
            <JobSelect customTitle="Annotation layer" />
            <GCard noHelp>
                <template #title>Download as format</template>
                <template #header> </template>
                <div id="center">
                    <FileFormatInput v-model="exportStore.format" />

                    <div>
                        <GInput type="checkbox" v-model="posHeadOnly">Export part of speech<br>without features</GInput>
                        <GInput type="checkbox" v-if="showMergeOption" v-model="shouldMerge">Merge</GInput>
                    </div>

                    <template v-if="showMergeOption">
                        <GInfo>
                            <p>
                                You have uploaded <b>{{ formatToHumanReadable(exportStore.format) }}</b> files to this
                                corpus
                                and
                                you are now exporting <b>{{ formatToHumanReadable(exportStore.format) }}</b>. <br />
                                It is possible to insert the annotation layer into the original file. This will retain
                                the original encoding.
                            </p>
                            <p>
                                If you do not choose the merge option, your export will ignore the original encoding of
                                your uploaded document.
                            </p>
                        </GInfo>

                        <GInfo v-if="hasTeiP5Legacy && shouldMerge" style="max-width: 850px;">
                            <h4 style="margin-top: 0;">Special notice for <b>TEI P5 legacy</b></h4>
                            <TeiP5LegacyWarning />
                        </GInfo>
                    </template>

                    <DownloadButton wide @click="exportStore.convert(shouldMerge, posHeadOnly)"
                        :disabled="exportStore.loading || !exportStore.linksAreValid" />

                    <GInfo spinner v-if="exportStore.loading">
                        <span><i>Please wait while your export is being processed.</i></span>
                    </GInfo>
                </div>
            </GCard>
        </GCard>
    </AnnotateTab>
</template>

<script setup lang="ts">
// Libraries & stores
import { onMounted, ref, computed, watch } from "vue"
import stores, { CorporaStore, JobsStore, ExportStore, DocumentsStore } from "@/stores"
// Api & types
import { Format } from '@/types/documents'
// Components
import { GCard, JobSelect, AnnotateTab, DownloadButton, GInfo } from "@/components"
import help from "@/components/help"
import TeiP5LegacyWarning from '@/views/help/subviews/formats/TeiP5LegacyWarning.vue'

// Stores
const corporaStore = stores.useCorpora() as CorporaStore
const jobsStore = stores.useJobs() as JobsStore
const exportStore = stores.useExportStore() as ExportStore
const documentsStore = stores.useDocuments() as DocumentsStore

// Fields
const posHeadOnly = ref(false)
const shouldMerge = ref(true)
const showMergeOption = computed(() => {
    const format = exportStore.format
    const formatIsMergeable = format == Format.Tei_p5 || format == Format.Tsv || format == Format.Folia || format == Format.Conllu
    const formatInCorpus = documentsStore.containsFormat(format)
    return formatIsMergeable && formatInCorpus
})
const hasTeiP5Legacy = computed(() => documentsStore.available.some(i => i.format == Format.Tei_p5_legacy))

// Methods
function formatToHumanReadable(format: Format): string {
    switch (format) {
        case Format.Tei_p5:
        case Format.Tei_p5_legacy:
            return "TEI P5"
        default:
            return format
    }
}

// Watchers
// Load jobs list once. jobSelectionStore takes care of the selected job.
onMounted(() => {
    jobsStore.reload()
    // We also need to load the documents, in order to determine the presence of TEI files.
    documentsStore.reloadDocumentsForCorpus(corporaStore.activeUUID)
})
</script>

<style scoped>
.content-wrapper {
    text-align: center;
}

:deep(#center) {
    display: flex;
    flex-direction: column;
    gap: 10px;
    align-content: center;
    align-items: center;
}
</style>
