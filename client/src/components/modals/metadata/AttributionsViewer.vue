<template>
    <GButton plain @click="showModal = true">
        <u> {{ Object.keys(items).length }} attributions </u>
    </GButton>

    <GModal v-if="showModal" @hide="showModal = false">
        <template #title>Attributions of {{ tagger.name }}</template>
        <ul>
            <li v-for="item in items" :key="item.name">
                <dl>
                    <dt>
                        <b>{{ item.name }}:</b>
                    </dt>
                    <dd>
                        <ExternalLink v-if="item.url" :href="item.url">
                            {{ item.description ?? item.url }}
                        </ExternalLink>
                        <template v-else>
                            {{ item.description }}
                        </template>
                    </dd>
                </dl>
            </li>
        </ul>
    </GModal>
</template>

<script setup lang="ts">
import type { LinkItem, Tagger } from "@/types/taggers"

const { tagger } = defineProps<{ tagger: Tagger }>()
const items = computed<LinkItem[]>(() => tagger.attributions)
const showModal = ref<boolean>()
</script>

<style scoped lang="scss">
ul {
    padding: 0 1rem;
    li {
        padding: 0.25rem 0;
    }
}
</style>
