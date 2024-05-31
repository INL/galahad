<template>
    <div>
        <div class="left">
        </div>
        <GTable class="right" :title="'Distribution of ' + (jobSelection.hypothesisJobId || 'the hypothesis layer')"
            helpSubject="evaluate" :columns :items="itemsToDisplay" :loading="distributionStore.loading"
            sortedByField="count">
            <template #table-empty-instruction>
                <p v-if="distribution.generated">No results for current filter settings.</p>
                <p v-else>Select a hypothesis layer to generate a distribution.</p>
            </template>
            <template #help>
                <p>The distribution shows what lemma, part-of-speech pairs have been assigned to which types. When there
                    are more than five types you can click on the inspect symbol to view all types of a lemma-PoS
                    combination.</p>
            </template>
            <template #header>
                <p>
                    <b v-if="distribution.trimmed">
                        Because of the large corpus size only the 1000 most frequent lemma,
                        part-of-speech pairs are shown.
                    </b>
                </p>
            </template>

            <!-- count -->
            <template #cell-count="data">
                <div>{{ `${data.value}` }}</div>
            </template>

            <!-- variantCount -->
            <template #cell-variantCount="data">
                <div>{{ `${Object.keys(data.item.literals.literals).length}` }}</div>
            </template>

            <!-- variants-->
            <template #cell-variants="data">
                <div style="min-width:200px">
                    <template v-if="Object.keys(data.item.literals.literals).length <= 5">
                        <span
                            v-for="(literal, index) in Object.keys(data.item.literals.literals).sort(function (a, b) { return data.item.literals.literals[b] - data.item.literals.literals[a] })"
                            :key="literal">
                            {{ literal }} <b>{{ `${data.item.literals.literals[literal]}` }}</b>{{ index !=
                                Object.keys(data.item.literals.literals).length - 1 ? ', ' : '' }}
                        </span>
                    </template>
                    <template v-else>
                        <RightFloatCell>
                            <template #left>
                                <span
                                    v-for="literal in Object.keys(data.item.literals.literals).sort(function (a, b) { return data.item.literals.literals[b] - data.item.literals.literals[a] }).slice(0, 5)"
                                    :key="literal">
                                    {{ literal }} <b>{{ `${data.item.literals.literals[literal]}` }}</b>,
                                </span>
                                <i>... and {{ Object.keys(data.item.literals.literals).length - 5 }} more</i>
                            </template>
                            <template #right>
                                <InspectButton @click="variantsToDisplay = data.item" />
                            </template>
                        </RightFloatCell>
                    </template>
                </div>
            </template>

            <template #prepend v-if="distribution.generated">
                <div class="table-controls">
                    <!-- search lemma-->
                    <div class="table-control" id="searchLemma">
                        Search lemma:
                        <GInput type="text" v-model="lemmaFilter" placeholder="Lemma" clearBtn />
                    </div>
                    <!-- search literals -->
                    <div class="table-control" id="searchWordForms">
                        Search types:
                        <GInput type="text" v-model="literalFilter" placeholder="Type" clearBtn />
                    </div>
                    <div class="table-control" id="searchWordForms">
                        Single/multiple PoS:
                        <GInput type="select" :options="singMultiPosOptions" v-model="selectedSingMultiPos"
                            placeholder="Type" clearBtn />
                    </div>
                    <!-- filter PoS -->
                </div>
                <div id="filterPos">
                    Include PoS: <br>
                    <div class="posGrid">
                        <span v-for="pos in filteredPosses" :key="pos">
                            <GInput style="display: inline-block" type="checkbox" v-model="includePos[pos]">
                                <span v-if="pos">{{ pos }}</span>
                                <span v-else><i>None</i></span>
                            </GInput>
                        </span>
                    </div>
                </div>
            </template>

        </GTable>

        <EvaluationInfoBox :eval="distribution" />

        <VariantsModal :variantsToDisplay="variantsToDisplay" :show="variantsToDisplay !== null"
            @hide="variantsToDisplay = null" id="modal" />
    </div>
</template>

<script setup lang='ts'>
// Libraries & stores
import { computed, ref, watch } from 'vue'
import stores, { DistributionStore } from '@/stores'
import { storeToRefs } from 'pinia'
// API & types
import { Distribution } from '@/types/evaluation'
// Components
import { GInput, GTable, EvaluationInfoBox, InspectButton, RightFloatCell, VariantsModal } from '@/components'

// Stores
const distributionStore = stores.useDistribution() as DistributionStore
// Doesn't need to be ref'ed, but it's easier to read.
const { distribution } = storeToRefs(distributionStore)
const jobSelection = stores.useJobSelection()

// Fields
// Table controls.
const includePos = ref({} as { [pos: string]: boolean })
const lemmaFilter = ref('')
const literalFilter = ref('')
// GModal for variants
const variantsToDisplay = ref(null as null | Distribution)
// Filtered table items.
const itemsToDisplay = computed((): Distribution[] => {
    // When distribution not yet generated.
    if (!distribution.value?.distribution?.length) return []

    return distribution.value?.distribution
        // Case insensitive string comparison.
        .filter(x => x.lemma.toLowerCase().includes(lemmaFilter.value.toLowerCase()))
        .filter(x => includePos.value[x.pos] !== false)
        // Filter by single/multiple PoS
        .filter(x => {
            if (selectedSingMultiPos.value == "single") return !x.pos.includes("+")
            if (selectedSingMultiPos.value == "multiple") return x.pos.includes("+")
            return true
        })
        // Case insensitive string comparison.
        // join on \n, as it can't be entered into a <input type=text>
        .filter(x => Object.keys(x.literals.literals).join('\n').toLowerCase().includes(literalFilter.value.toLowerCase()))
})
const columns = [
    { key: 'lemma', label: 'lemma', sortOn: (x: Distribution) => x.lemma },
    { key: 'pos', label: 'PoS', sortOn: (x: Distribution) => x.pos },
    { key: 'count', label: 'total\noccurrences', sortOn: (x: Distribution) => x.count },
    { key: 'variantCount', label: 'number\nof types', sortOn: (x: Distribution) => Object.keys(x.literals.literals).length },
    { key: 'variants', label: 'types' },
]
const singMultiPosOptions = [
    { value: 'single', text: 'Single' },
    { value: 'multiple', text: 'Multiple' },
    { value: 'both', text: 'Both' },
]
const selectedSingMultiPos = ref(singMultiPosOptions[0].value)
const filteredPosses = computed(() => {
    if (selectedSingMultiPos.value === 'single') {
        return distributionStore.posses.filter(pos => !pos.includes("+"))
    } else if (selectedSingMultiPos.value === 'multiple') {
        return distributionStore.posses.filter(pos => pos.includes("+"))
    } else {
        return distributionStore.posses
    }
})

// Watches
/**
 * On switching jobs, turn on all PoS checkboxes. We check for change in distributionStore.posses, not in 
 * jobSelection.hypothesisJobId, because of the network delay.
 */
watch(() => distributionStore.posses, () => {
    distributionStore.posses.forEach(pos => includePos.value[pos] = true)
}, { immediate: true })
</script>

<style scoped lang="scss">
#searchWordForms,
#searchLemma {
    flex: 1;
    max-width: 200px;
}

div:not(#modal)::v-deep() .g-card .content-wrapper .content {
    display: flex;
    flex-direction: column;
    justify-content: safe center;
    align-items: safe center;
}

:deep(table) {
    max-width: 100%;

    th {
        word-break: break-word;
    }
}

.posGrid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    column-gap: 0px;
}

.posGrid span>div {
    width: fit-content;
}

:deep(#prepend) {
    display: flex;
    flex-direction: column;
    width: 100%;
}
</style>
