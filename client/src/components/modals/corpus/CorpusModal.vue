<!-- create or update a corpus -->
<template>
    <GModal :title @hide="$emit('hide')">
        <template #help>
            <slot name="help"></slot>
        </template>
        <form @submit.prevent @keyup.enter="confirm">
            <table>
                <template v-if="canWrite || !initial">
                    <tr>
                        <td>
                            <label>Name</label>
                        </td>
                        <td>
                            <GInput
                                v-model="name"
                                focus
                                placeholder="Corpus name"
                                :validator="validateCorpusName"
                                validityDescriptor="Name required"
                            />
                        </td>
                    </tr>
                    <tr>
                        <td><label>Year from</label></td>
                        <td>
                            <GNumber
                                v-model="periodFrom"
                                validityDescriptor="Before end year"
                                placeholder="YYYY"
                                :validator="
                                    (v) => {
                                        return (v ?? 0) <= (periodTo ?? 0)
                                    }
                                "
                            />
                        </td>
                    </tr>

                    <tr>
                        <td><label>Year to</label></td>
                        <td>
                            <GNumber
                                v-model="periodTo"
                                validityDescriptor="After start year"
                                placeholder="YYYY"
                                :validator="
                                    (v) => {
                                        return (v ?? 0) >= (periodFrom ?? 0)
                                    }
                                "
                            />
                        </td>
                    </tr>

                    <tr>
                        <td>
                            <label><ExternalLink href="/galahad/overview/principles">Tagset</ExternalLink></label>
                        </td>
                        <td>
                            <TagsetSelect v-model="tagset" />
                        </td>
                    </tr>

                    <tr>
                        <td><label>Language</label></td>
                        <td>
                            <GInput v-model="language" placeholder="Language" />
                        </td>
                    </tr>

                    <template v-if="user.admin">
                        <tr>
                            <td colspan="2">
                                <hr />
                            </td>
                        </tr>

                        <tr>
                            <td>Benchmark set</td>
                            <td>
                                <GCheckBox v-model="dataset">Benchmark</GCheckBox>
                            </td>
                        </tr>
                    </template>

                    <tr>
                        <td colspan="2">
                            <hr />
                        </td>
                    </tr>

                    <tr>
                        <td><label>Source name</label></td>
                        <td>
                            <GInput v-model="sourceName" placeholder="Source name" />
                        </td>
                    </tr>

                    <tr>
                        <td><label>Source url</label></td>
                        <td>
                            <GInput v-model="sourceUrl" type="url" placeholder="Source url" />
                        </td>
                    </tr>

                    <CorpusModalUserList :users="collaborators" listName="Collaborators" :showAddDialog />
                </template>
                <CorpusModalUserList :users="viewers" listName="Viewers" :showAddDialog />
            </table>
        </form>

        <template #buttons>
            <GButton green @click="confirm" :disabled title="Accept"><i class="fa fa-check"></i></GButton>
        </template>
    </GModal>
</template>

<script setup lang="ts">
import useCorpora from "@/stores/corpora"
import useUser from "@/stores/static/user"
import type { CorpusMetadata, MutableCorpusMetadata } from "@/types/corpora"

const { user } = storeToRefs(useUser())
const { canDelete, canWrite } = storeToRefs(useCorpora())

// --- props ---
const { initial, title } = defineProps<{ initial?: CorpusMetadata; title: string }>()

const emit = defineEmits<{ hide: []; confirm: [metadata: CorpusMetadata] }>()

// --- data ---
const dataset = ref<boolean>()
const name = ref<string>()
const periodFrom = ref<number>()
const periodTo = ref<number>()
const tagset = ref<string>()
const language = ref<string>()
const sourceName = ref<string>()
const sourceUrl = ref<string>()
const collaborators = ref<string[]>()
const viewers = ref<string[]>()

// --- computed ---
const showAddDialog = computed(() => {
    // Only show it when you're not editing (i.e. you pressed 'new')
    // Or if you did press edit, only show it when you have access
    return !initial || canDelete.value
})
const disabled = computed(() => {
    const i = initial as MutableCorpusMetadata
    return (
        !isValid.value ||
        (initial &&
            name.value === i.name &&
            periodFrom.value === i.period?.from &&
            periodTo.value === i.period?.to &&
            tagset.value === i.tagset &&
            language.value === i.language &&
            collaborators.value.join("\n") === (i.collaborators ?? []).join("\n") &&
            viewers.value.join("\n") === (i.viewers ?? []).join("\n") &&
            sourceName.value === i.source?.name &&
            sourceUrl.value === i.source?.url &&
            dataset.value === i.dataset)
    )
})
const isValid = computed(() => {
    if (!validateCorpusName(name.value)) return false
    // check if eras are integer values
    if (periodFrom.value && !Number.isInteger(periodFrom.value)) return false
    if (periodTo.value && !Number.isInteger(periodTo.value)) return false
    if (periodFrom.value > periodTo.value) return false
    return true
})

// --- watch ---
watch(
    () => initial,
    (newValue: CorpusMetadata): void => {
        if (!newValue) return
        name.value = newValue.name
        periodFrom.value = newValue.period?.from
        periodTo.value = newValue.period?.to
        tagset.value = newValue.tagset
        language.value = newValue.language
        sourceName.value = newValue.source?.name
        sourceUrl.value = newValue.source?.url
        dataset.value = newValue.dataset
        collaborators.value = [...(newValue.collaborators ?? [])]
        viewers.value = [...(newValue.viewers ?? [])]
    },
    { immediate: true, deep: true },
)

// --- methods ---
function confirm(): void {
    const value: CorpusMetadata = {
        name: name.value,
        period: [periodFrom.value, periodTo.value].some((i) => i != undefined)
            ? { from: periodFrom.value, to: periodTo.value }
            : undefined,
        tagset: tagset.value,
        language: language.value,
        dataset: dataset.value,
        collaborators: collaborators.value,
        viewers: viewers.value,
        source: [sourceName.value, sourceUrl.value].some((i) => i != undefined)
            ? { name: sourceName.value, url: validatesourceUrl(sourceUrl.value) }
            : undefined,
        uuid: initial?.uuid,
    }
    emit("confirm", value)
    emit("hide")
}
function validateCorpusName(name: string) {
    // simply has to be non blank
    return name?.trim()?.length > 0
}
function validatesourceUrl(url: string): string {
    if (!url) return url
    try {
        new URL(url)
    } catch (error) {
        // try to fix it by adding a protocol
        url = `http://${url}`
    }
    return url
}
</script>

<style scoped lang="scss">
.warning {
    color: var(--int-red);
}

:deep(table) {
    border-collapse: collapse;

    td {
        padding: 3px 10px;
    }
}

:deep(hr) {
    margin: 10px 0;
    border: 1px dotted var(--int-grey);
}

:deep(.checkbox-container) {
    margin-bottom: 0;
}
</style>
