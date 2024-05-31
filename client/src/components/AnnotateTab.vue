<!-- AnnotateTab takes care of error messages for unselected and empty corpora for the subtabs under annotate/* -->
<template>
    <div> <!-- This div fills the background color. -->
        <!-- loading corpora-->
        <GCard v-if="corporaStore.loading && !hideCorpusError" title="Loading corpora" noHelp>
            <GSpinner medium />
        </GCard>
        <!-- No corpus selected -->
        <GCard v-else-if="!corporaStore.activeCorpus && !hideCorpusError" title="No corpus selected" noHelp>
            <GInfo error>
                <p>No corpus has been selected.</p>
                <GNav :route="{ path: '/annotate/corpora' }">Select a corpus</GNav>
            </GInfo>
        </GCard>
        <!-- No write permissions on selected corpus -->
        <GCard v-else-if="!userStore.hasWriteAccess && !hidePermissionsError" title="Insufficient permissions" noHelp>
            <GInfo error>
                <p>You have insufficient permissions to perform this action.</p>
                <GNav :route="{ path: '/annotate/corpora' }">Select a different corpus</GNav>
            </GInfo>
        </GCard>
        <!-- Loading documents -->
        <GCard v-else-if="documentsStore.loading && !hideDocsError" title="Loading documents" noHelp>
            <GSpinner medium />
        </GCard>
        <!-- No documents in corpus-->
        <GCard v-else-if="!corporaStore.hasDocs && !hideDocsError" title="Empty corpus" noHelp>
            <GInfo error>
                <p>This corpus has no documents.</p>
                <GNav :route="{ path: '/annotate/documents' }">Upload documents to this corpus</GNav>
            </GInfo>
        </GCard>
        <!-- Loading jobs -->
        <GCard v-else-if="jobsStore.loading && !hideAnnotationsError" title="Loading documents" noHelp>
            <GSpinner medium />
        </GCard>
        <!-- No non-empty jobs-->
        <GCard v-else-if="jobSelectionStore.selectableJobs.length == 0 && !hideAnnotationsError" title="No annotations"
            noHelp>
            <GInfo error>
                <p>None of the documents have annotations. Either:</p>
                <ul>
                    <li>
                        <GNav :route="{ path: '/annotate/documents' }">Upload documents</GNav> to this corpus that contain
                        source annotations
                    </li>
                    <li>
                        <GNav :route="{ path: '/annotate/jobs' }">Start a tagger job</GNav> to create annotations
                    </li>
                    <li>Or wait for an existing job to finish</li>
                </ul>
            </GInfo>
        </GCard>

        <!-- content -->
        <slot v-else>Oops!</slot>
    </div>
</template>

<script setup lang='ts'>
// Libraries & stores
import stores, { CorporaStore, DocumentsStore, JobSelectionStore, JobsStore, UserStore } from '@/stores'
// Components
import { GCard, GInfo, GNav, GSpinner } from '@/components'

// Stores
const corporaStore = stores.useCorpora() as CorporaStore
const documentsStore = stores.useDocuments() as DocumentsStore
const jobsStore = stores.useJobs() as JobsStore
const jobSelectionStore = stores.useJobSelection() as JobSelectionStore
const userStore = stores.useUser() as UserStore

// Props
const props = defineProps({
    hideDocsError: {
        type: Boolean,
        default: false
    },
    hideCorpusError: {
        type: Boolean,
        default: false
    },
    hideAnnotationsError: {
        type: Boolean,
        default: false
    },
    hidePermissionsError: {
        type: Boolean,
        default: true
    },
})

</script>
