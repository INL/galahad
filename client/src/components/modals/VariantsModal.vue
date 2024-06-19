<template>
    <GModal small :show="show" @hide="$emit('hide')" headless>
        <GTable compact showHelp :columns :items="items" sortedByColumn="occurrences" :sortDesc="true">
            <template #title>
                Types of lemma <i>{{ variantsToDisplay?.lemma }}</i> and part-of-speech <i>{{ variantsToDisplay?.pos
                    }}</i>
            </template>
            <template #help>
                This is an overview of all types belonging to the chosen lemma, part-of-speech pair.
            </template>
        </GTable>
    </GModal>
</template>

<script setup lang="ts">
// Libraries & stores
import { computed } from 'vue'
// Types & API
import { Field } from '@/types/table'
import { Distribution } from '@/types/evaluation'
// Components
import { GModal, GTable } from '@/components'

// Custom types
type DistEntry = { variant: string, occurrences: number }

// Props
const props = defineProps<{
    show: boolean
    variantsToDisplay: Distribution
}>();

// Fields
const columns: Field[] = [
    { key: 'variant', label: 'Type', sortOn: (x: DistEntry) => x.variant },
    { key: 'occurrences', label: 'Occurrences', sortOn: (x: DistEntry) => x.occurrences }
]
const items: DistEntry[] = computed(() => {
    return Object.entries(props.variantsToDisplay.literals.literals).map(([variant, occurrences]) => ({ variant, occurrences }))
})

</script>
<style scoped>
:deep(.my-small) {
    padding: 1em;
}
</style>