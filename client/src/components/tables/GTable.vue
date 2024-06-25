<template>
    <GCard :showHelp="showHelp" :headless="headless" :helpSubject="helpSubject" :noHelp>
        <template #title>
            <slot name="title">{{ title }}</slot>
        </template>
        <template #help>
            <slot name="help"></slot>
        </template>

        <div v-if="loading" style="text-align: center; margin:10px 0">
            <GSpinner medium />
        </div>

        <template #header>
            <slot name="header"></slot>
        </template>
        <div id="prepend">
            <slot name="prepend"></slot>
        </div>

        <slot v-if="items && items.length === 0 && !loading" name="table-empty-instruction">
            Here should be an instruction how to fill the content.
        </slot>
        <table :class="`${cssClass} ${loading ? ' loading' : ''} ${selectable ? ' selectable' : ''}`">
            <thead v-if="!(isEmpty && !displayOnEmpty)">
                <tr>
                    <th v-for="field in visibleFields" :key="field.key" style="text-align: center;">
                        <div style="white-space: pre-line">
                            <!-- specific head -->
                            <slot :name="'head-' + field.key" :field=field>
                                <!-- generic head -->
                                <slot name="head" :field=field>{{ field.label || field.key }}</slot>
                            </slot>

                        </div>
                        <span v-if="field.sortOn">
                            <span v-if="sortedBy != field.key" class="sort-control">
                                <span @click="sortBy(field.key, false)">▲</span> | <span
                                    @click="sortBy(field.key, true)">▼</span>
                            </span>
                            <span v-else class="sort-control">
                                <span v-if="!sortIsDesc" class="sort-control active">▲</span>
                                <span v-else @click="sortBy(field.key, false)" class="sort-control">▲</span>
                                <span class="sort-control active"> | </span>
                                <span v-if="sortIsDesc" class="sort-control active">▼</span>
                                <span v-else @click="sortBy(field.key, true)" class="sort-control">▼</span>
                            </span>
                        </span>
                        <span v-else></span>
                    </th>
                </tr>
            </thead>
            <tbody>
                <!-- the rows -->
                <template v-for="item, i in itemsToDisplay" :key="'row' + i">
                    <tr @click="rowClicked(item)" @dblclick="rowClicked(item); $emit('rowDoubleClicked')"
                        :class="(equal(modelValue, item) ? 'selected' : '') + ' ' + (selectable ? 'cursor-pointer' : '')">
                        <td v-for="field in visibleFields" :key="field.key"
                            :style="`text-align: ${field.textAlign || 'center'};`">
                            <!-- specific cell rendering -->
                            <slot :name="'cell-' + field.key" :field="_field(field)" :item="item"
                                :value="item[field.key] || ''">
                                <!-- generic cell rendering -->
                                <slot name="cell" :field="_field(field)" :item="item" :value="item[field.key] || ''">
                                    {{ item[field.key] }}</slot>
                            </slot>
                        </td>
                    </tr>
                    <!-- details -->
                    <tr :key="'_details' + i" v-if="item._showDetails" class="details">
                        <td :colspan="visibleFields.length">
                            <slot name="_details" :item="item"></slot>
                        </td>
                    </tr>
                </template>
                <!-- filler -->
                <template v-if="fill && items.length > 0">
                    <tr v-for="filler in (0, pageSize - itemsToDisplay.length)" :key="itemsToDisplay.length + filler">
                        <td v-for="field in visibleFields" :key="field.key"><br></td>
                    </tr>
                </template>
                <!-- <tr id="append-row" key="append-row">
                        <slot name="append-row"></slot>
                    </tr> -->
            </tbody>
        </table>
        <div id="footer" v-if="!(isEmpty && !displayOnEmpty)">
            <!-- page controls -->
            <div v-if="numPages > 1" id="page-controls" @click="this.$nextTick(() => this.$refs.test.scrollIntoView())"
                ref="test">
                <GButton plain @click="page = 1" :disabled="page == 1">1</GButton>
                <GButton plain @click="page > 1 ? page -= 1 : null" :disabled="page == 1" title="Previous">
                    <i class="fa fa-arrow-left"></i>
                </GButton>
                <select v-model=page>
                    <option v-for="pageNumber in numPages" :key="pageNumber" :value=pageNumber>{{ pageNumber }}</option>
                </select>
                <GButton plain @click="page < numPages ? page += 1 : null" :disabled="page == numPages" title="Next">
                    <i class="fa fa-arrow-right"></i>
                </GButton>
                <GButton plain @click="page = numPages" :disabled="page == numPages">{{ numPages }}</GButton>
            </div>
        </div>

        <!-- append will be displayed even when isEmpty && !displayOnEmpty -->
        <div id="append">
            <slot name="append"></slot>
        </div>
    </GCard>
</template>

<script lang='ts'>
// Libraries & stores
import { defineComponent, PropType } from 'vue'
// API & types
import { Field } from '@/types/table'
// Components
import help from '@/components/help'
import GButton from '@/components/input/GButton.vue'
import GCard from '@/components/GCard.vue'

type Item = { [key: string]: unknown }

export default defineComponent({
    name: "GTable",
    emits: ['rowClicked', 'rowDoubleClicked', 'update:modelValue'],
    components: { GCard, GButton },
    props: {
        title: { type: String, default: "You forgot the title" },
        displayOnEmpty: { type: Boolean, default: false },
        columns: { type: Array as PropType<Field[]>, default() { return [] } },
        fill: { type: Boolean, default: false },
        headless: { type: Boolean, default: false },
        loading: { type: Boolean, default: false },
        selectable: { type: Boolean, default: false },
        sortedByColumn: { type: String, default: null },
        sortDesc: { type: Boolean, default: true },
        compact: { type: Boolean, default: false },
        showHelp: { type: Boolean, default: false },
        items: { type: Array as PropType<Item[]>, default() { return [] } },
        modelValue: { type: Object as PropType<Item>, default: null }, // use in conjunction with 'selectable' to make a v-model
        helpSubject: { type: String as () => keyof typeof help },
        noHelp: { type: Boolean, default: false }
    },
    //   model: {
    //       prop: 'value',
    //       event: 'valueUpdated'
    //   },
    data() {
        return {
            page: 1,
            sortIsDesc: this.sortDesc as boolean,
            sortedBy: this.sortedByColumn as null | string
        }
    },
    computed: {
        cssClass() {
            let ret = ""
            this.compact ? ret += "compact " : ''
            return ret
        },
        isEmpty(): boolean {
            return !this.items || this.items.length === 0;
        },
        itemsToDisplay(): Item[] {
            const page = this.page
            const pageSize = this.pageSize
            function getPageItems(allItems: Item[]) {
                return allItems.slice((page - 1) * pageSize, (page) * pageSize)
            }

            // only paginate
            if (this.sortedBy === null) return getPageItems(this.items)

            const sortOn = this.columns.filter(field => (field.key == this.sortedBy))[0]?.sortOn //hmm
            function mapToSortProp(x: any) {
                return sortOn ? sortOn(x) : x
            }

            if (this.sortedBy === null) {
                // no sort, just paginate
                return getPageItems(this.items)
            } else {
                // sort and then paginate
                const allItems = this.items.slice().sort(
                    (a: Item, b: Item) =>
                        (-1) ** (+this.sortIsDesc | 0) *
                        this.compareAny(mapToSortProp(a), mapToSortProp(b))
                )
                return getPageItems(allItems)
            }
        },
        numPages(): number {
            return Math.ceil(this.items.length / this.pageSize)
        },
        pageSize(): number {
            if (!this.items) return 20
            // We allow for some leniency since we don't want the user to go to the next page just to see one entry
            if (this.items.length <= 50) return this.items.length
            return this.items.length > 20 ? 20 : this.items.length
        },
        primaryKeyFields(): string[] {
            return this.columns.filter(field => field.isPrimaryField).map(field => field.key)
        },
        visibleFields() {
            return this.columns.filter(field => !field.hidden);
        }
    },
    methods: {
        anyIncludes(whole: unknown, part: unknown): boolean {
            if (!whole) return false
            return (whole as Record<string, unknown> | unknown[]).toString().includes((part as Record<string, unknown> | unknown[]).toString())
        },
        compareAny(a: unknown, b: unknown): number {
            // null and undefined are always smaller
            if (this.nu(a) && this.nu(b)) return 0
            if (this.nu(a)) return -1
            if (this.nu(b)) return 1

            // Infinity is always bigger
            if (a === Infinity) return 1
            if (b === Infinity) return -1

            if (typeof a === "number" && typeof b === "number") {
                return a - b
            } else if (typeof a === "string" && typeof b === "string") {
                return a.localeCompare(b)
            } else if (Array.isArray(a) && Array.isArray(b)) {
                if (a.length === 0 && b.length === 0) return 0
                if (a.length === 0) return -1
                if (b.length === 0) return 1
                return this.compareAny(a[0], b[0]) // Approximate               
            } else if (typeof a === 'boolean' && typeof b === 'boolean') {
                if (a === b) return 0
                if (a) return 1
                return -1
            } else {
                //garbage
                return 0
            }
        },
        _field(field: Field): Field {
            if (!field.label) {
                field.label = field.key
            }
            return field
        },
        equal(item1: Item, item2: Item): boolean {
            // tests for equality of two items based an the primary key fields
            if (this.primaryKeyFields.length === 0) return item1 === item2
            if (this.nu(item1) && this.nu(item2)) return true
            if (this.nu(item1) || this.nu(item2)) return false
            return this.primaryKeyFields.map((key: string) => item1[key] === item2[key]).filter(x => !x).length === 0
        },
        nu(v: unknown) { return v === null || v === undefined }, //utility
        rowClicked(item: Item): void {
            if (this.selectable) {
                this.$emit('update:modelValue', item)
            } else {
                this.$emit('rowClicked', item)
            }
        },
        sortBy(key: string, sortIsDesc: boolean): void {
            this.sortedBy = key
            this.sortIsDesc = sortIsDesc
            // Reset to first page to see the effect of sorting.
            this.page = 1
        }
    },
    watch: {
        numPages(newVal) {
            if (this.page > newVal && newVal > 0) {
                this.page = newVal
            }
        },
    }
});
</script>

<style scoped lang="scss">
*:deep(.table-controls) {
    display: flex;
    flex-wrap: wrap;
    gap: 20px;
    justify-content: center;
    align-content: stretch;

    .table-control {
        flex-basis: 100px;
        min-height: 100px;

        &.slider {
            /*Some overrides because the slider looks bad when small*/
            flex: 1;
            min-width: 200px;
            padding: 0px 20px;
            max-width: 400px;
        }
    }
}

.cursor-pointer {
    cursor: pointer;
}

#footer {
    overflow: hidden;
    display: flex;
    justify-content: center;
}

#prepend {
    margin-bottom: 15px;
}

#page-controls {
    background-color: var(--white);
    border: 1px solid none;
    color: var(--black);
    float: right;
    padding: 10px;
    -webkit-user-select: none;
    /* Safari */
    -moz-user-select: none;
    /* Firefox */
    -ms-user-select: none;
    /* IE10+/Edge */
    user-select: none;
    /* Standard */
}

#page-controls select {
    margin: 5px;
}

#page-controls span {
    margin: 5px;
}

#page-controls .inactive {
    color: var(--int-light-grey);
}

.sort-control {
    white-space: nowrap;
    color: var(--white);
    -webkit-user-select: none;
    /* Safari */
    -moz-user-select: none;
    /* Firefox */
    -ms-user-select: none;
    /* IE10+/Edge */
    user-select: none;

    /* Standard */
    span {
        cursor: pointer
    }
}

.sort-control.active {
    color: black;
}

table.loading tbody,
table.loading thead {
    filter: grayscale(100%) sepia(100%) blur(5px);
}

table.loading .loading-symbol {
    opacity: 1;
    visibility: visible;
}

table .loading-symbol {
    transition: opacity 2s ease, visibility 2s ease;
    opacity: 0;
    z-index: 1;
    position: absolute;
    top: 50%;
    left: 50%;
    transform: translate(-50%, -50%);
    visibility: hidden;
}

table {
    border-collapse: collapse;
    margin: 0 auto;
    margin-top: 5px;
    padding: 0;

    caption {
        font-size: 1.5em;
        margin: .5em 0 .75em;
    }

    tr {
        border: 1px solid var(--int-very-light-grey-hover);

        &:nth-child(even) {
            background: #fff;
        }

        &:nth-child(odd) {
            background: var(--int-very-light-grey);
        }
    }

    thead {
        >tr {
            background-color: var(--int-theme) !important;
        }
    }

    th {
        padding: .6em;
        text-align: center;
        font-size: .85em;
        letter-spacing: .1em;
        text-transform: uppercase;
    }

    td {
        padding: .5em;
        text-align: center;
        min-width: 60px;
    }

    overflow-x: auto;
}

table.compact {

    td,
    th {
        padding: .1em 2em;
    }

    margin: 0;
}

table.selectable {
    tr:hover:not(.selected) {
        background: var(--int-very-light-grey-hover);
    }
}

table tr.selected {
    background-color: var(--int-theme-lighter);
}

* {
    box-sizing: border-box;
}
</style>