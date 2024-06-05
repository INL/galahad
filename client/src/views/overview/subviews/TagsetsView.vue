<template>
    <div>
        <GTable title="Tagsets overview" :columns :items="tagsetsStore.tagsets">
            <template #help>
                Here you can see an overview of possible tagsets to annotate Dutch. Currently only TDN-Core is used in
                the platform.
            </template>

            <template #table-empty-instruction>
                No tagsets appeared? That is not right! Please contact the INT at
                <MailAddress />
            </template>

            <template #cell-punctuationTags="d">
                {{ d.value.join(", ") }}
            </template>
        </GTable>
    </div>
</template>

<script setup lang="ts">
// Libraries & stores
import stores, { TagsetsStore } from "@/stores"
// Components
import { MailAddress, GTable } from "@/components"

// Stores
const tagsetsStore = stores.useTagsets() as TagsetsStore

// Fields
const columns = [
    { key: "shortName", label: "abbreviation" },
    { key: "longName", label: "name" },
]
</script>
