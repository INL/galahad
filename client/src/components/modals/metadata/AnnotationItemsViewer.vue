<template>
    <GButton plain @click="showModal = true" style="justify-content: left">
        <u>
            {{ items.map((i) => i.annotation).join(", ") }}
        </u>
    </GButton>

    <GModal v-if="showModal" @hide="showModal = false">
        <template #title>Annotations and principles of {{ tagger.name }}</template>
        <template #help>
            <p>
                The principles are the guidelines that were used to create the annotations (e.g. part of speech
                tagsets). For a list of all principles in the platform, see the
                <router-link to="/overview/principles">principles overview</router-link>.
            </p>
            <HelpLink topic="principles" />
        </template>
        <ul>
            <li v-for="item in items" :key="item.annotation">
                {{ item.annotation }}
                <dl>
                    <template v-for="principle in item.principles" :key="principle.name">
                        <dt>
                            <b>{{ principle.name }}:</b>
                        </dt>
                        <dd>
                            <ExternalLink :href="principle.url">
                                {{ principle.description ?? principle.url }}
                            </ExternalLink>
                        </dd>
                    </template>
                </dl>
            </li>
        </ul>
    </GModal>
</template>

<script setup lang="ts">
import type { AnnotationItem, Tagger } from "@/types/taggers"

const { tagger } = defineProps<{ tagger: Tagger }>()
const items = computed<AnnotationItem[]>(() => tagger.annotations)
const showModal = ref<boolean>()
</script>

<style scoped lang="scss">
div {
    display: flex;
    gap: 0.5rem;
    align-items: center;
    justify-content: end;
    ul {
        padding: 0 1rem;
        li {
            padding: 0.25rem 0;
            dl {
                padding: 0 1rem;
            }
        }
    }
}
</style>
