<template>
    <GCard>
        <GTable title="Type-Token Distribution" :columns :loading :items sortColumn="count">
            <template #help>
                <DistributionHelp />
            </template>

            <template #empty>
                <p v-if="distribution">No results for current filter settings.</p>
            </template>

            <template #header>
                <GForm v-if="hypothesisAnnotations.length">
                    <fieldset>
                        <label for="annotation-select">Annotation</label>
                        <GSelect id="annotation-select" :options="annotationOptions" v-model="selectedAnnotation" />
                    </fieldset>
                    <fieldset v-if="distribution">
                        <label for="lemma-input">Search {{ selectedAnnotation }}</label>
                        <GInput
                            id="lemma-input"
                            type="text"
                            v-model="annotationFilter"
                            :placeholder="selectedAnnotation"
                        />
                    </fieldset>
                    <fieldset v-if="distribution">
                        <label for="literal-input">Search types</label>
                        <GInput id="literal-input" type="text" v-model="typeFilter" placeholder="Type" />
                    </fieldset>
                    <fieldset>
                        <label for="group-select">Group by</label>
                        <GSelect id="group-select" :options="groupOptions" v-model="selectedGroup" />
                    </fieldset>
                    <fieldset v-if="distribution">
                        <label for="analysis-select">Single/multiple analyses</label>
                        <GSelect id="analysis-select" :options="analysesOptions" v-model="selectedAnalysis" />
                    </fieldset>
                    <fieldset v-if="distribution">
                        <label for="groups-select">Select {{ selectedGroup }}</label>
                        <MultiSelect
                            id="groups-select"
                            :options="groupsOptions"
                            v-model="selectedGroups"
                            :placeholder="selectedGroup"
                            :maxSelectedLabels="5"
                        />
                    </fieldset>
                </GForm>
                <p v-else>Select a hypothesis layer.</p></template
            >

            <!-- types -->
            <template #cell-types="d: TableData<TypeToken>">
                <template v-if="Object.keys(d.item.tokens).length <= 5">
                    <span
                        v-for="(literal, index) in Object.keys(d.item.tokens).sort(function (a, b) {
                            return d.item.tokens[b] - d.item.tokens[a]
                        })"
                        :key="literal"
                    >
                        {{ literal }} <b>{{ d.item.tokens[literal].toLocaleString() }}</b
                        >{{ index != Object.keys(d.item.tokens).length - 1 ? ", " : "" }}
                    </span>
                </template>
                <template v-else>
                    <RightFloatCell>
                        <template #left>
                            <span
                                v-for="literal in Object.keys(d.item.tokens)
                                    .sort(function (a, b) {
                                        return d.item.tokens[b] - d.item.tokens[a]
                                    })
                                    .slice(0, 5)"
                                :key="literal"
                            >
                                {{ literal }}
                                <b>{{ d.item.tokens[literal].toLocaleString() }}</b
                                >,
                            </span>
                            <i
                                >... and
                                {{ (Object.keys(d.item.tokens).length - 5).toLocaleString() }}
                                more</i
                            >
                        </template>
                        <template #right>
                            <InspectButton @click="typeToken = d.item" />
                        </template>
                    </RightFloatCell>
                </template>
            </template>
        </GTable>
        <TypeTokenModal
            v-if="typeToken"
            :typeToken
            :annotation="selectedAnnotation"
            :group="selectedGroup"
            @hide="typeToken = undefined"
        />
    </GCard>
</template>

<script setup lang="ts">
import useDistribution from "@/stores/evaluation/distribution"
import useLayers from "@/stores/layers"
import type { TypeToken } from "@/types/evaluation/distribution"
import type { SelectOption } from "@/types/ui/select"
import MultiSelect from "primevue/multiselect"
import type { TableData } from "@/types/ui/table"

const { hypothesisAnnotations } = storeToRefs(useLayers())
const { distribution, loading, annotation: selectedAnnotation, group: selectedGroup } = storeToRefs(useDistribution())

// Form
const annotationOptions = computed(() =>
    // only logical annotations
    hypothesisAnnotations.value.filter((option: SelectOption) => !["head", "token"].includes(option.text)),
)
const annotationFilter = ref<string>("")
const typeFilter = ref<string>("")
const groupOptions = computed(() =>
    // only logical groups
    hypothesisAnnotations.value.filter((option: SelectOption) => !["head", "token"].includes(option.text)),
)
const analysesOptions: SelectOption[] = [
    { value: "both", text: "Both" },
    { value: "single", text: "Single" },
    { value: "multiple", text: "Multiple" },
]
const selectedAnalysis = ref<string>(analysesOptions[0].value)
const groups = computed<string[]>(() =>
    [...new Set(Object.values(distribution.value ?? {}).map((t: TypeToken) => t.group))].sort(),
)
const groupsOptions = computed(() => {
    if (selectedAnalysis.value === "single") {
        return groups.value.filter((pos) => !pos.includes("+"))
    }
    if (selectedAnalysis.value === "multiple") {
        return groups.value.filter((pos) => pos.includes("+"))
    }
    return groups.value
})
const selectedGroups = ref<string[]>([])

// GModal
const typeToken = ref<TypeToken>()

// Table data
const columns = computed(() => [
    { key: "annotation", label: selectedAnnotation.value },
    { key: "group", label: selectedGroup.value },
    {
        key: "count",
        align: "right",
        format: (t: TypeToken) => t.count.toLocaleString(),
        sortOn: (t: TypeToken) => t.count,
    },
    {
        key: "unique",
        label: "unique",
        align: "right",
        format: (t: TypeToken) => Object.keys(t.tokens).length.toLocaleString(),
        sortOn: (t: TypeToken) => Object.keys(t.tokens).length,
    },
    { key: "types", noSort: true },
])
const items = computed((): TypeToken[] => {
    if (!distribution.value) return []
    return (
        distribution.value
            // // Case insensitive string comparison.
            .filter((t: TypeToken) => t.annotation.toLowerCase().includes(annotationFilter.value.toLowerCase()))
            // // join on \n, as it can't be entered into a <input type=text>
            .filter((t: TypeToken) =>
                Object.keys(t.tokens).join("\n").toLowerCase().includes(typeFilter.value.toLowerCase()),
            )
            .filter((t: TypeToken) => selectedGroups.value.includes(t.group))
    )
})

// Default select options
watchPostEffect(() => {
    selectedAnnotation.value ??= annotationOptions.value[0]?.value
})
watchPostEffect(() => {
    selectedGroup.value ??= groupOptions.value[1]?.value
})
watchPostEffect(() => {
    selectedGroups.value = groupsOptions.value
})
</script>
