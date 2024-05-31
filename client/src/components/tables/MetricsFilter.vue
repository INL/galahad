<template>
    <div class="table-controls">
        <div class="table-control">
            Annotation:
            <GInput type="select" :options="metricOptions" v-model="selectedMetric" />
        </div>

        <div class="table-control">
            Group by:
            <GInput type="select" :options="groupOptions" v-model="selectedGroup" />
        </div>

        <div class="table-control" v-if="selectedMetric == selectedGroup">
            Single/multiple analysis:
            <GInput type="select" :options="singleOrMultipleOptions" v-model="selectedSingleOrMultiple" />
        </div>
    </div>
</template>

<script setup lang='ts'>
// Libraries & stores
import { ref, computed } from 'vue'

// Fields
const metricOptions = computed(() => {
    return [
        { value: "pos", text: "PoS" },
        { value: "lemma", text: "Lemma" },
        { value: "lemmaPos", text: "PoS + Lemma" }
    ]
    if (metrics.value?.metrics == null) return []
    return Object.keys(metrics.value.metrics).map((key) => ({ value: key, text: key.split(/(?=[A-Z])/).join(" ") }))
})
const groupOptions = [
    { value: "pos", text: "PoS" },
    { value: "lemma", text: "Lemma" },
]
const singleOrMultipleOptions = [
    { value: "both", text: "Both" },
    { value: "single", text: "Single" },
    { value: "multi", text: "Multiple" },
]
const selectedMetric = ref(metricOptions.value[0]?.value)
const selectedGroup = ref(metricOptions.value[0]?.value)
const selectedSingleOrMultiple = ref(singleOrMultipleOptions[0]?.value)

const metricName = computed(() => {
    let annotation = null
    if (selectedSingleOrMultiple.value == "both" || selectedMetric.value != selectedGroup.value) {
        annotation = selectedMetric.value
    } else {
        annotation = selectedSingleOrMultiple.value + capitalize(selectedMetric.value)
    }
    const group = capitalize(selectedGroup.value)
    return annotation + "By" + group

})

// Methods
function capitalize(str: string): string {
    return str.charAt(0).toUpperCase() + str.slice(1)
}

defineExpose({
    metricName
})
</script>