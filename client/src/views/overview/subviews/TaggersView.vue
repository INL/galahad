<template>
    <div>
        <GTable title="Taggers overview" :columns :items="taggerStore.taggers" sortedByColumn="id" :sortDesc="false">
            <template #help>
                Here you can see an overview of all available taggers within Galahad. <br>
                For more information on the taggers, please visit GitHub:
                <ExternalLink href="https://github.com/INL/galahad-taggers-dockerized/">
                    galahad-taggers-dockerized
                </ExternalLink>
            </template>

            <template #table-empty-instruction>
                No taggers appeared? That is not right! Please contact the INT at
                <MailAddress />
            </template>

            <!-- id -->

            <template #cell-id="d">
                <span :class="markActive(d.item.id)">{{ d.value }}</span>
            </template>

            <!-- tagset -->

            <template #cell-tagset="d">
                <span v-if="d.value">{{ d.value }}</span>
                <i v-else>Unknown</i><br />
            </template>

            <!-- era -->

            <template #cell-era="d"> {{ d.item.eraFrom }} - {{ d.item.eraTo }} </template>

            <!-- produces -->

            <template #cell-produces="d">
                {{ sort_tagger_produces(d.value).join(", ") }}
            </template>

            <!-- attributions -->

            <template #cell-attributions="d">
                <table class="attrib">
                    <!-- ugly, but it works -->
                    <tr v-for="key in Object.keys(d.value)" :key="key">
                        <td style="text-align: right; white-space: nowrap">
                            <b>{{ key }}</b>
                        </td>
                        <td style="text-align: left; overflow-wrap: break-word">{{ d.value[key] }}</td>
                    </tr>
                </table>
            </template>

            <!-- links -->

            <template #cell-model="d">
                <ExternalLink :href="d.value.href">
                    {{ d.value.name }}
                </ExternalLink>
            </template>
            <template #cell-software="d">
                <ExternalLink :href="d.value.href">
                    {{ d.value.name }}
                </ExternalLink>
            </template>
            <template #cell-dataset="d">
                <ExternalLink :href="d.value.href">
                    {{ d.value.name }}
                </ExternalLink>
            </template>
        </GTable>
    </div>
</template>

<script setup lang="ts">
// Libraries & stores
import stores, { TaggersStore } from "@/stores"
// Components
import { MailAddress, GTable, ExternalLink } from "@/components"
// API & types
import { sort_tagger_produces } from "@/stores/taggers"

// Stores
const taggerStore = stores.useTaggers() as TaggersStore

// Fields
const columns = [
    { key: "id", label: "name", sortOn: (x: any) => x.id },
    { key: "description" },
    { key: "tagset" },
    { key: "era", label: "period", sortOn: (x: any) => x.eraFrom.toString() + x.eraTo.toString() },
    { key: "produces" },
    { key: "model" },
    { key: "software" },
    { key: "dataset" },
]

// Methods
/**
 * Mark the active row, retrieved from the url anchor.
 */
function markActive(id: string) {
    const hash = window.location.hash.substring(1)
    if (id == hash) {
        return "active"
    }
    return ""
}
</script>

<style scoped lang="scss">
.attrib {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
    display: inline-block;
}

:deep(tr):has(> td > span.active) {
    background-color: var(--int-theme-lighter);
}
</style>
