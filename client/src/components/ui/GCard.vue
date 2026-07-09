<template>
    <section class="g-card">
        <header v-if="$slots.title || title || $slots.help" class="header">
            <hgroup v-if="$slots.title || title" class="title">
                <h3 class="h3">
                    <slot name="title">{{ title }}</slot>
                </h3>
                <GButton
                    v-if="$slots.help"
                    class="help-btn"
                    title="Help"
                    plain
                    @click="
                        () => {
                            expand = !expand
                            if (expand) {
                                plausible.help.clicked()
                            }
                        }
                    "
                >
                    {{ expand ? "&times;" : "?" }}
                </GButton>
            </hgroup>
            <GInfo v-if="expand && $slots.help">
                <slot name="help"></slot>
                <template v-if="helpLink" #footer>
                    <HelpLink :helpLink />
                </template>
            </GInfo>
        </header>
        <article v-if="article" class="content article">
            <slot></slot>
        </article>
        <div v-else class="content">
            <slot></slot>
        </div>
    </section>
</template>

<script setup lang="ts">
import { plausible } from "@/ts/plausible"
import type { HelpLink } from "@/types/ui/help"

const { helpLink, title } = defineProps<{ helpLink?: HelpLink | string; title?: string; article?: boolean }>()

const expand = ref<boolean>()
</script>

<style scoped lang="scss">
.view > .g-card,
.view.g-card,
.modal .g-card {
    padding: 1rem;
}

.view.g-card > .content {
    flex: 1;
}

.g-card {
    background-color: var(--white);
    max-width: 100%;
    display: flex;
    flex-direction: column;
    gap: 1rem;
    align-items: stretch !important;

    .header {
        display: flex;
        flex-direction: column;
        gap: 1rem;
        align-items: center;

        .title {
            display: flex;
            align-items: center;
            gap: 0.5rem;

            .help-btn.plain {
                border: 1px solid var(--int-grey);
                font-weight: bold;
                cursor: help;
                font-size: 1.5rem;
                width: 2rem;
                height: 2rem;
            }
        }
    }

    .content {
        display: flex;
        flex-direction: column;
        align-items: safe center;
        gap: 1rem;
        max-width: 100%;

        &.article {
            max-width: 800px;
            align-items: start;
            align-self: center;

            :deep(h1) {
                font-size: 2rem;
            }
        }
    }
}
</style>
