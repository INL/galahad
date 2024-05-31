<template>
    <button :disabled="disabled || loading" :class="cssClass" @click="$emit('click')">
        <GSpinner small v-if="loading" />
        <slot></slot>
    </button>
</template>

<script setup lang='ts'>
// Libraries & stores
import { computed } from 'vue'
// Components
import { GSpinner } from '@/components/'

const props = defineProps<{
    disabled?: boolean
    red?: boolean
    orange?: boolean
    green?: boolean
    plain?: boolean
    loading?: boolean
}>()

const cssClass = computed(() => {
    return {
        red: props.red,
        orange: props.orange,
        green: props.green,
        plain: props.plain,
        disabled: props.disabled,
    }
})

defineEmits(['click'])
</script>

<style scoped lang="scss">
button {
    background-color: var(--int-theme);
    font-size: inherit;
    font-family: inherit;
    padding: 10px;
    font-size: 1em;
    border: none;
    word-break: keep-all;
    white-space: nowrap;
    width: max-content;
    margin: 0px 2px;
    cursor: pointer;
    line-height: 1.2em;
    display: inline-flex;
    gap: 5px;

    // Align holy grail.
    svg {
        vertical-align: bottom;
    }

    &:disabled {
        opacity: 0.5;
        cursor: not-allowed;
    }
}

// Button colors.
button {
    // Default
    background-color: var(--int-theme);

    &.orange {
        background-color: var(--int-orange);
    }

    &.green {
        background-color: var(--int-green);
    }

    &.red {
        background-color: var(--int-red);
    }

    &.plain {
        background-color: var(--white);
    }

    // Only use hover & active when not disabled.
    &:not(:disabled) {

        &:hover {
            background-color: var(--int-theme-hover);

            &.orange {
                background-color: var(--int-orange-hover);
            }

            &.green {
                background-color: var(--int-green-hover);
            }

            &.red {
                background-color: var(--int-red-hover);
            }

            &.plain {
                background-color: var(--int-very-light-grey);
            }
        }

        &:active {
            background-color: var(--int-theme-active);

            &.orange {
                background-color: var(--int-orange-active);
            }

            &.green {
                background-color: var(--int-green-active);
            }

            &.red {
                background-color: var(--int-red-active);
            }

            &.plain {
                background-color: var(--int-very-light-grey-hover);
            }
        }
    }
}
</style>