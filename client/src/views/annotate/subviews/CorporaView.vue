<template>
    <AnnotateTab hideCorpusError hideDocsError>
        <!-- Owner corpus table -->
        <CorporaTable title="Corpora" :filter="(c: CorpusMetadata) => c.owner == user.name && !c.dataset">
            <template #help>
                <CorpusHelp />
            </template>
            <template #header>
                <!-- Action buttons -->
                <GForm gap="0.25rem">
                    <GButton green title="New" @click="newCorpus = true">
                        <i class="fa fa-plus"></i>
                    </GButton>
                    <GButton orange title="Edit" :disabled="!canWrite || !corpusId" @click="editCorpus = copy(corpus)">
                        <i class="fa fa-pencil"></i>
                    </GButton>
                    <GButton red title="Delete" :disabled="!canDelete || !corpusId" @click="deleteCorpus = corpus">
                        <i class="fa fa-trash"></i>
                    </GButton>
                </GForm>
            </template>
            <template #empty> <p>First, create a corpus.</p> </template>
        </CorporaTable>

        <!-- Shared corpus table -->
        <CorporaTable title="Shared with you" :filter="(c: CorpusMetadata) => c.owner != user.name && !c.dataset">
            <template #help>
                <p>
                    Here you can see the corpora that have been shared with you. If you are a collaborator, you can make
                    modifications. If you are a viewer, you can only inspect and evaluate it.
                </p>
            </template>
            <template #empty>No corpora have been shared with you.</template>
        </CorporaTable>

        <!-- Benchmark corpus table -->
        <CorporaTable title="Datasets" :filter="(c: CorpusMetadata) => c.dataset">
            <template #help>
                <BenchmarkSetsHelp />
            </template>
            <template #empty>No dataset corpora available.</template>
        </CorporaTable>

        <!-- Create modal -->
        <CorpusModal v-if="newCorpus" title="New corpus" @hide="newCorpus = false" @confirm="create">
            <template #help>
                Fill in the metadata and create a corpus.
                <CorpusModalHelp />
            </template>
        </CorpusModal>

        <!-- Update modal -->
        <CorpusModal
            v-if="editCorpus"
            title="Edit corpus"
            :initial="editCorpus"
            @hide="editCorpus = undefined"
            @confirm="update"
        >
            <template #help>
                Change the metadata of an existing corpus.
                <CorpusModalHelp />
            </template>
        </CorpusModal>

        <!-- Delete modal -->
        <DeleteModal
            v-if="deleteCorpus"
            :itemName="deleteCorpus.name"
            @delete="remove(deleteCorpus)"
            @hide="deleteCorpus = undefined"
        />
    </AnnotateTab>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import useUser from "@/stores/static/user"
import type { CorpusMetadata } from "@/types/corpora"

const { user } = storeToRefs(useUser())
const { create, remove, update } = useCorpora()
const { corpus, canWrite, canDelete, corpusId } = storeToRefs(useCorpora())

// Once not falsy, respective modal is shown.
const newCorpus = ref<boolean>()
const deleteCorpus = ref<CorpusMetadata>()
const editCorpus = ref<CorpusMetadata>()

// Deepcopy so we can modify the object freely.
function copy(corpus: CorpusMetadata): CorpusMetadata {
    return structuredClone(toRaw(corpus))
}
</script>
