<template>
    <div :id="id" :class="`g-card ${disabled ? 'disabled' : ''} ${highlight ? 'highlight' : ''}`">

        <div v-if="!headless">

            <!-- title -->
            <div class="title" style="text-align: center">
                <h3>
                    <slot name="title">
                        <template v-if="title">
                            {{ title }}
                        </template>
                        <template v-else>
                            Someone forgot to put a title
                        </template>
                    </slot>
                </h3>
                <span v-if="!noHelp">
                    <GButton plain v-if="!showHelp" class="question-mark" @click="expand = !expand" :disabled="disabled"
                        title="Help">
                        <!-- show &#10006; if expanded, else 𝚒 -->
                        {{ expand ? '&times;' : '?' }}
                    </GButton>
                </span>
            </div>

            <!-- help -->
            <Transition name="help">
                <GInfo class="help" v-if="!noHelp" v-show="expand">
                    <slot name="help">Someone forgot to put a help primer</slot>
                    <div style="text-align: center; margin-top: 1em">
                        <HelpLink v-if="helpSubject" :subject="helpSubject" />
                    </div>
                </GInfo>
            </Transition>

            <!-- header -->
            <i class="header">
                <slot name="header"></slot>
            </i>

        </div>

        <!-- content -->
        <div class="content-wrapper">
            <div class="content">
                <slot>Somenone forgot to put content</slot>
            </div>
        </div>

        <!-- navs -->
        <!-- <div class="navs">
          <slot name="navs">
            <div v-for="nav in navs" :key="nav.label"><GNav :route="nav.route">{{nav.label}}</GNav></div>
          </slot>
        </div> -->

    </div>
</template>

<script setup lang='ts'>
import { ref } from 'vue'
import { GButton, GInfo, HelpLink } from '@/components'

const props = defineProps({
    disabled: { type: Boolean, default: false },
    headless: { type: Boolean, default: false },
    helpSubject: { type: String },
    highlight: { type: Boolean, default: false },
    id: { type: String, default: "" },
    navs: { type: Array, default: () => [] }, // [ { route: {}, label: 'This could have been a useful nav' } ]},
    noHelp: { type: Boolean, default: false },
    showHelp: { type: Boolean, default: false },
    title: { type: String }
})

const expand = ref(props.showHelp)
</script>

<style scoped lang="scss">
.g-card {
    background-color: var(--white);
    padding: 1em;
    padding-top: 0;
    min-width: 250px;
    display: flex;
    flex-direction: column;
    align-items: center;
}

.g-card h3 {
    display: inline-block;
}

.g-card .header {
    text-align: center;
}

.g-card.highlight {
    box-shadow: 0px 0px 2em 1em var(--int-theme);
}

.g-card.disabled {
    opacity: 0.5;
}

@keyframes wiggle {
    0% {
        transform: translateY(-0px);
        background-color: var(--int-theme-active);
    }

    50% {
        transform: translateY(-10px);
        background-color: var(--int-theme);
    }

    100% {
        transform: translateY(-0px);
        background-color: var(--int-theme-active);
    }
}

.badge {
    -webkit-user-select: none;
    /* Safari */
    -moz-user-select: none;
    /* Firefox */
    -ms-user-select: none;
    /* IE10+/Edge */
    user-select: none;
    /* Standard */
}

button.plain {
    border: 1px solid var(--int-grey);
    width: fit-content;
    padding: 0 0.6em;
    font-size: 1.5em;
    cursor: help;
    box-sizing: border-box;
    width: 28px;
    justify-content: center;
    font-weight: bold;
    margin-left: 0.5em;
}

.help-enter-active,
.help-leave-active {
    transition: opacity 0.2s ease;
}

.help-enter-from,
.help-leave-to {
    opacity: 0;
    transition: opacity 0.2s ease;
}

.question-mark {
    cursor: pointer;
}

.navs {
    text-align: right;
}

.content {
    min-width: 0;
}

.content-wrapper {
    display: flex;
    min-width: 80%;
    max-width: 100%;
    align-items: center;
    justify-content: center;
}
</style>