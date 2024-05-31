<template>
    <GCard :title="title" :disabled="disabled" noHelp>
        <template #help>
            Select the result of a tagger job as a hypothesis or reference layer.
            It is also possible to select the source annotations.
        </template>
        <GSpinner medium v-if="jobsStore.loading" />
        <GInput v-else type="select" :options="jobSelectionStore.selectableJobs" v-model="private_value"
            :disabled="disabled">
        </GInput>
        <GInfo v-if="untaggedDocsExist">
            Not all documents have been tagged yet. It is still possible to select this layer, but it will be
            incomplete. <br />
            Alternatively, <GNav :route="{ path: '/annotate/jobs' }">start a new tagger job</GNav> or wait for the
            current job to finish.
        </GInfo>
        <GInfo v-if="sourceLayerHasMissingAnnotations">
            Some documents in this corpus have no source annotations. It is still possible to select this layer, but it
            will be
            incomplete. <br />
            Alternatively, <GNav :route="{ path: '/annotate/documents' }">go to documents</GNav> and remove or add
            documents.
        </GInfo>
    </GCard>
</template>

<script setup lang='ts'>
// Libraries & stores
import { computed, ref, watch } from 'vue'
import stores, { JobsStore, JobSelectionStore } from '@/stores'
// API & Types
import { SOURCE_LAYER } from '@/types/jobs'
// Components
import { GCard, GInput, GSpinner } from '@/components'

// Stores
const jobsStore = stores.useJobs() as JobsStore
const jobSelectionStore = stores.useJobSelection() as JobSelectionStore
const documentsStore = stores.useDocuments()

// Fields
const props = defineProps({
    disabled: { default: false },
    isReference: { default: false },
    customTitle: { default: null as string | null },
})
const title = computed(() => props.customTitle ?? (props.isReference ? 'Reference' : 'Hypothesis'))
const private_value = ref(null)
// Whether there are documents that have not been tagged yet.
const untaggedDocsExist = computed(() => {
    if (!private_value.value) return false
    if (!jobsStore.jobs) return false
    const job = jobsStore.jobs[private_value.value]
    if (!job) return false
    return job.progress.finished < job.progress.total
})
// Whether the selected layer is sourceLayer and has missing annotations.
const sourceLayerHasMissingAnnotations = computed(() => {
    if (!private_value.value) return false
    if (!jobsStore.jobs) return false
    const job = jobsStore.jobs[private_value.value]
    if (!job) return false
    if (job.tagger.id != SOURCE_LAYER) return false
    return job.progress.finished != documentsStore.numSourceAnnotations
})

// Watches & mounts
// watch both referenceJobId and hypothesisJobId
watch(() => [jobSelectionStore.referenceJobId, jobSelectionStore.hypothesisJobId], () => {
    if (props.isReference)
        private_value.value = jobSelectionStore.referenceJobId
    else
        private_value.value = jobSelectionStore.hypothesisJobId
}, { immediate: true })
// reverse
watch(private_value, () => {
    if (!private_value.value) return
    if (props.isReference)
        jobSelectionStore.referenceJobId = private_value.value
    else
        jobSelectionStore.hypothesisJobId = private_value.value
}, { immediate: true })

</script>

<style scoped lang="scss">
:deep(.infocontainer) {
    max-width: 500px;
    text-align: left;
}

.g-card {
    text-align: center;
}
</style>
