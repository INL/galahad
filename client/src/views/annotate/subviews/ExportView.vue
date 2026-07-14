<template>
    <AnnotateTab>
        <template #title>Export {{ corpus.name }}</template>

        <template #help>
            <ExportHelp />
        </template>

        <GForm vertical>
            <LayerSelect displayName="Annotation layer" />

            <FormatSelect />

            <GCheckBox v-model="posHeadOnly">Export part of speech without features</GCheckBox>

            <GCheckBox v-if="showMergeOption" v-model="shouldMerge">Merge encoding</GCheckBox>

            <DownloadButton wide :disabled @click="convert(shouldMerge, posHeadOnly)" />
        </GForm>

        <GInfo v-if="loading" spinner>Please wait while your export is being processed.</GInfo>

        <template v-if="showMergeOption">
            <GInfo>
                <p>
                    You have uploaded
                    <b>{{ format }}</b> files to this corpus and you are now exporting <b>{{ format }}</b
                    >. It is possible to insert the annotation layer into the original file. This will retain the
                    original encoding.
                </p>
                <p>
                    If you do not choose the merge option, your export will ignore the original encoding of your
                    uploaded document.
                </p>
            </GInfo>

            <GInfo v-if="hasTeiP5Legacy && shouldMerge">
                <h4>Special notice for <b>TEI P5 legacy</b></h4>
                <TeiP5LegacyWarning />
            </GInfo>
        </template>
    </AnnotateTab>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import useDocuments from "@/stores/documents"
import useExport from "@/stores/export"
import useLayers from "@/stores/layers"
import { Format } from "@/types/documents"
import TeiP5LegacyWarning from "@/views/help/subviews/formats/TeiP5LegacyWarning.vue"

const { corpus } = storeToRefs(useCorpora())
const { hypothesisId } = storeToRefs(useLayers())
const { reload } = useLayers()
const { convert } = useExport()
const { loading, format } = storeToRefs(useExport())
const { documents } = storeToRefs(useDocuments())

const posHeadOnly = ref<boolean>(false)
const shouldMerge = ref<boolean>(false)

const showMergeOption = computed(() => {
    const f = format.value
    const formatIsMergeable = f === Format.TEI_P5 || f === Format.TSV || f === Format.FOLIA || f === Format.CONLLU
    const formatInCorpus = documents.value.some((doc) => doc.format === f)
    return formatIsMergeable && formatInCorpus
})
const hasTeiP5Legacy = computed(() => documents.value.some((i) => i.format === Format.TEI_P5_LEGACY))
const disabled = computed(() => format.value === undefined || hypothesisId.value === undefined || loading.value)
</script>
