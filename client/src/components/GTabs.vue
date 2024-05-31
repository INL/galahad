<!-- This is a Galahad specific version of tabs -->
<template>
    <div class="tabs">
        <!-- header -->
        <div class="header">

            <div class="top">
                <div v-if="$slots['tabs-start']" class="nav tabs-start">
                    <slot :name="'tabs-start'"></slot>
                </div>
                <slot :name="'title'"></slot>
                <div v-if="$slots['tabs-end']" class="nav tabs-end">
                    <slot :name="'tabs-end'"></slot>
                </div>
            </div>

            <div class="bottom">
                <div v-for="tab in _tabs" :key="tab.id">
                    <a v-if="!tab.disabled && !tab.stub" disabled="disabled" :href='urlForTab(tab.id)'
                        :class="'textcolor ' + navLinkClass(tab.id)" @click.prevent="navigateTo(tab.id)">
                        <slot :name="`${tab.id}-title`" :isActive="currentTab === tab.id">{{ tab.title || tab.id }}
                        </slot>
                    </a>
                    <span :class="`nav-link ${tab.disabled ? 'disabled' : ''}`" v-else>
                        <slot :name="`${tab.id}-title`">{{ tab.title || tab.id }}</slot>
                    </span>
                </div>
            </div>

        </div>

        <!-- content -->
        <RouterView class="content" @navigate="x => { $router.push(x); induceCurrentTab() }" v-slot="{ Component }">
            <transition name="fade" mode="out-in">
                <component :is="Component" />
            </transition>
        </RouterView>

    </div>
</template>

<script lang='ts'>
import { defineComponent, PropType } from 'vue'

// Stub means it does display as title, but it is not interactive, so you can implement custom functionality
export type Tab = { id: string, title: string, disabled?: boolean, stub?: boolean }

export default defineComponent({
    name: "GTabs",
    props: {
        basePath: { type: String },
        replace: { type: Boolean, default: false },
        tabs: {
            type: Array as PropType<Tab[]>,
            // Ids to be used by the tabs. Pick anything that can be used as an attribute
            default() {
                return [{ id: 'id', title: 'You forgot the titles', disabled: true, stub: false }] as Tab[]
            }
        }
    },
    data() {
        return {
            currentTab: null // this.tabs[0].id
        }
    },
    mounted() {
        if (this.$route.path.split("/").length > this.basePath.split("/").length) {
            // respect the url
            this.induceCurrentTab()
            return
        }
        const state = localStorage.getItem('galahad:' + this.basePath)
        if (state !== null && state !== undefined) {
            // load state from local storage
            this.navigateTo(state, true)
        } else {
            // default
            this.navigateTo(this._tabs[0].id, true)
        }
    },
    computed: {
        _tabs(): Tab[] {
            // To allow for more flexible input, we calculate the actual tabs here
            function isObject(objValue: unknown) {
                return objValue && typeof objValue === 'object' && objValue.constructor === Object;
            }
            // Just assume it is either a correct object, or a string.
            return this.tabs.map((x: Tab | string) => isObject(x) ? x : { id: x, title: x }) as Tab[]
        },

    },
    methods: {
        induceCurrentTab() {
            const split = this.$route.path.split('/').reverse()
            let induction = null // this._tabs[0].id // default
            split.forEach(x => {
                this._tabs.forEach(y => {
                    if (x === y.id) induction = y.id
                })
            })
            if (induction !== null)
                this.setCurrentTab(induction) // TODO do this properly, this is bound to fail
        },
        navigateTo(tabId: string, replace = false) {
            const path = this.basePath + '/' + tabId
            if (!this.$route.path.startsWith(path)) {
                if (replace || this.replace) {
                    this.$router.replace({ path: path, query: this.$route.query })
                } else {
                    this.$router.push({ path: path, query: this.$route.query })
                }
            }
            this.setCurrentTab(tabId)
        },
        navLinkClass(tabId: string) {
            return "nav-link" + (this.currentTab == tabId ? " active" : "")
        },
        setCurrentTab(tabId: string) {
            // Since the route is not reactive, we have to update the value like this
            this.currentTab = tabId
            if (tabId !== null && tabId !== undefined) localStorage.setItem('galahad:' + this.basePath, tabId)
        },
        urlForTab(tabId: string) {
            const qs = Object.entries(this.$route.query).map(([k, v]) => `${k}=${encodeURIComponent(typeof (v) === "object" ? JSON.stringify(v) : v)}`).join('&')
            return '/galahad' + this.basePath + '/' + tabId + '?' + qs;
        }
    },
    watch: {
        '$route'() {
            this.induceCurrentTab()
        }
    }
});
</script>

<style lang="scss" scoped>
.tabs {
    min-height: 0;
    flex: 1;
}

.header {
    position: -webkit-sticky;
    /* Safari */
    position: sticky;
    font-family: Schoolboek, Helvetica, sans-serif;
}

// Header top
.tabs>.header> :deep(.top) {
    background-color: white;
    align-items: center;
    display: flex;
    flex-wrap: wrap;

    // header top tabs start
    .tabs-start {

        img {
            position: relative;
            left: -6px;
            height: 125px;
            pointer-events: none;
        }

        a {
            display: block;
            height: 110px;
            width: 232px;
            position: relative;
        }
    }

    // header top title
    .title {
        display: inline-flex;
        flex-direction: column;
        padding: 0px 20px;

        a {
            font-style: normal;
            color: black;
            text-decoration: none;
            font-size: 16px;

            &:last-child {
                font-size: 50px;
                line-height: 50px;
            }
        }
    }

    // header top tabs end
    .tabs-end {
        flex: 1;
        display: flex;
        justify-content: flex-end;

        a {
            margin-right: 30px;
            font-size: 16px;
            color: black;
            text-decoration: none;
            font-style: normal;

            &:hover {
                text-decoration: underline;
                cursor: pointer;
            }
        }
    }
}

// Header bottom for all tabs
.tabs .header .bottom {
    display: inline-flex;
    flex-wrap: wrap;
    line-height: 50px;
    background-color: var(--int-theme);

    >div {
        flex: 1 1 auto;
    }

    .nav-link {
        font-style: normal;
        text-align: center;
        display: block;
        min-width: 80px;
        padding: 0 20px;
        line-height: inherit;
        text-decoration: none;
        color: black;
        user-select: none;
        transition: background-color 0.1s linear;

        &:hover:not(.disabled):not(.active) {
            cursor: pointer;
            background-color: var(--int-theme-hover);
        }

        &.disabled {
            opacity: 0.5;
            background-color: var(--int-very-light-grey);
            // Same color as default css button:disabled
            color: rgb(109, 109, 109);
            cursor: not-allowed;
        }

        &.active {
            background-color: var(--int-theme-outline);
        }

        &:active:not(.disabled):not(.active) {
            background-color: var(--int-theme-active);
        }
    }
}

// tabs level 1
.tabs.level-1 {
    display: flex;
    flex-direction: column;
    height: 100%;

    >.header {
        background-color: var(--int-theme);
        z-index: 100;
        top: 0;
        box-shadow: 0px 0px 10px black;

        .top {
            height: 110px;
        }

        .bottom {
            line-height: 70px;
            font-size: 18px;
        }
    }

    >.content {
        background-color: var(--int-very-light-grey);
    }
}

/* It's nice to read with a bit more space at the bottom*/
:deep(.tabs.level-2)>.content {
    padding-bottom: 2em !important;
}

.tabs>.content {
    padding: 10px;
    min-height: 0;
    display: flex;
    flex-direction: column;
    flex: 1;
}

.tabs.level-2,
.tabs.level-3 {
    box-sizing: border-box;

    .content {
        background-color: white;
        border: var(--int-light-grey) 1px solid;
    }

    .bottom {
        border-top: var(--int-light-grey) 1px solid;
        border-left: var(--int-light-grey) 1px solid;
        border-right: var(--int-light-grey) 1px solid;
    }

}

.tabs.level-2 {
    display: flex;
    flex-direction: column;

    .content {
        overflow-y: auto;
    }
}
</style>