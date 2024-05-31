<!-- A modal used by PoS confusion & metrics. -->
<template>
    <GModal :show="show" @hide="$emit('hide')" :title="title">
        <template #help>
            Here you can see a sample of how a token was tagged by <i>{{ hypothesisJob }}</i> and <i>{{ referenceJob
                }}</i>.
            The samples are a random selection of all tokens in this category.
        </template>
        <GTable :columns :items="items" headless>

            <template #head="data">{{ data.field.label || data.field.key }}</template>
            <template #cell="data">{{ data.value }}</template>

        </GTable>
        <!--Download-->
        <p>Download all samples for this category.</p>
        <DownloadButton wide @click="$emit('download')" :loading="downloading" />
    </GModal>
</template>

<script setup lang='ts'>
// Libraries & stores
import { computed, ref } from 'vue'
import stores, { CorporaStore } from '@/stores'
// Types & API.
import * as API from '@/api/evaluation'
import * as Utils from "@/api/utils"
import { TermComparison } from '@/types/evaluation'
import { literalsForTermComparison } from '@/stores/evaluation'
import { GModal, GTable, DownloadButton } from '@/components'

// Stores
const corporaStore = stores.useCorpora() as CorporaStore

// Props
const props = defineProps({
    show: { type: Boolean },
    samples: { type: Object },
    referenceJob: { type: String },
    hypothesisJob: { type: String },
    downloading: { type: Boolean, default: false }
})

// Emits
defineEmits(['hide', 'download'])

// Fields
const title = computed(() => {
    if (props.samples.title) return props.samples.title
    return props.samples.agreement ? 'PoS agree samples' : 'PoS confusion samples'
})
const columns = computed(() => {
    return [
        { key: 'literal', label: 'token' },
        { key: 'lemma2', label: props.referenceJob + ' lemma' },
        { key: 'pos2', label: props.referenceJob + ' PoS' },
        { key: 'lemma1', label: props.hypothesisJob + ' lemma' },
        { key: 'pos1', label: props.hypothesisJob + ' PoS' },
    ]
})
const items = computed(() => {
    if (!props.samples.samples) return []
    return props.samples.samples.map((sample: TermComparison) => {
        return {
            literal: literalsForTermComparison(sample),
            lemma1: sample.hypoTerm.lemma,
            pos1: sample.hypoTerm.pos,
            lemma2: sample.refTerm.lemma,
            pos2: sample.refTerm.pos
        }
    })
})

</script>

<style scoped>
button,
p {
    margin: 5px auto;
    display: block;
    width: fit-content;
}

.fa-download {
    padding: 0 1em;
}

:deep(td):nth-child(1),
:deep(td):nth-child(3) {
    border-right: 1px solid var(--int-very-light-grey-hover);
}
</style>