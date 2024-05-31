<!-- Do not put a comment between <template> and <AnnotateTab>
    It breaks vue... https://github.com/vuejs/vue/issues/2253 -->

<template>
    <AnnotateTab>
        <!-- title-only card -->
        <GCard :title="`Evaluate corpus ${corporaStore.activeCorpus?.name}`" helpSubject="evaluate">
            <template #help>
                <component :is="help.evaluate"></component>
            </template>
            <!-- Explicitly empty body-->
            <template></template>
        </GCard>
        <!-- Choose hypothesis and reference -->
        <div style="display: flex; width: 100%; align-items: center; justify-content: center; flex-wrap: wrap">
            <JobSelect />
            <JobSelect :isReference="true" />
            <GCard title="Download as CSV" noHelp>
                <i v-if="!jobSelection.hypothesisJobId || !jobSelection.referenceJobId">Select both layers first.</i>
                <DownloadButton v-else wide @click="evaluation.downloadCSV()" :loading="evaluation.loading" />
            </GCard>
        </div>

        <!-- Table tabs -->
        <GTabs ref="tabs" class="level-3" :basePath="basePath" :tabs="[
            { id: 'distribution', title: 'Distribution' },
            { id: 'global_metrics', title: 'Global Metrics' },
            { id: 'grouped_metrics', title: 'Grouped Metrics' },
            { id: 'confusion', title: 'PoS confusion' }
        ]">
        </GTabs>

    </AnnotateTab>
</template>

<script setup lang="ts">
// Libraries & stores
import { onMounted, watch } from 'vue'
import stores, {
    DistributionStore, JobsStore, MetricsStore, EvaluationStore,
    JobSelectionStore, ConfusionStore, CorporaStore, DocumentsStore
} from '@/stores'
// Components
import { GCard, GTabs, AnnotateTab, JobSelect, DownloadButton } from '@/components'
import help from '@/components/help'
import { SOURCE_LAYER } from '@/types/jobs';

// Stores
const jobsStore = stores.useJobs() as JobsStore
const evaluation = stores.useEvaluation() as EvaluationStore
const confusion = stores.useConfusion() as ConfusionStore
const distribution = stores.useDistribution() as DistributionStore
const metrics = stores.useMetrics() as MetricsStore
const jobSelection = stores.useJobSelection() as JobSelectionStore
const corporaStore = stores.useCorpora() as CorporaStore
const documentsStore = stores.useDocuments() as DocumentsStore

// Fields
const props = defineProps(['basePath'])

// Methods
/**
 * Reloads the three evaluation types for the current hypothesis and reference.
 * Because distribution is affected solely by hypothesis changes, it is not reloaded by default.
 * Check whether the hypothesis has changed yourself.
 * @param reloadDistribution Whether to reload the distribution as well.
 */
function reloadEvaluationData(reloadDistribution = false) {
    if (!corporaStore.activeCorpus) return
    if (!jobSelection.selectionsValid) return
    const hypothesis = jobSelection.hypothesisJobId
    const reference = jobSelection.referenceJobId

    if (reference != null && hypothesis != null) {
        confusion.reset()
        metrics.reset()
        confusion.reloadForUUIDHypothesisReference(corporaStore.activeUUID, hypothesis, reference)
        metrics.reloadForUUIDHypothesisReference(corporaStore.activeUUID, hypothesis, reference)
    }
    // Distribution is unaffected by reference changes, so explicitly ask for it.
    if (reloadDistribution && hypothesis != null) {
        distribution.reset()
        distribution.reloadForUUIDHypothesis(corporaStore.activeUUID, hypothesis)
    }
}

// Watches & mounts
onMounted(() => {
    // Always reset, because e.g. the selected corpus might have changed
    confusion.reset()
    metrics.reset()
    distribution.reset()
})

// On corpus (=dataset) selection, reload jobs & docs for that corpus
watch(() => corporaStore.activeCorpus, () => {
    // Jobs needed for jobs <select>.
    jobsStore.reload()
    // Docs needed to determine whether the sourceLayer job has annotations.
    documentsStore.reloadDocumentsForCorpus(corporaStore.activeUUID)
}, { immediate: true })

// Reload data on job selection changes.
watch(() => jobSelection.hypothesisJobId, () => {
    reloadEvaluationData(true)
})
watch(() => jobSelection.referenceJobId, () => {
    reloadEvaluationData()
})
// OnLoad, we also have to wait on validation.
watch(() => jobSelection.selectionsValid, () => {
    if (jobSelection.selectionsValid) {
        reloadEvaluationData(true)
    }
    // At this point, invalid selections are set to null.
    // So we can override the reference to a default.
    if (jobSelection.referenceJobId == null) {
        jobSelection.referenceJobId = SOURCE_LAYER
    }
}, { immediate: true })

</script>

<style scoped lang="scss">
.level-3 {
    margin-top: 2em;

    :deep(h3) {
        margin-bottom: 0;
    }
}
</style>