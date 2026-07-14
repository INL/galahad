<template>
    <GCard :title>
        <template v-if="$slots.title" #title>
            <slot name="title"></slot>
        </template>

        <template v-if="$slots.help" #help>
            <slot name="help"></slot>
        </template>

        <GSpinner v-if="loading" />

        <slot v-if="$slots.header" name="header"></slot>

        <slot v-if="isEmpty && !loading" name="empty"></slot>

        <template v-else>
            <p v-if="selectable">Click on a row to select an item.</p>

            <GTablePaginator v-if="numPages > 1" v-model="page" :numPages />

            <table :class="classes">
                <thead v-if="!isEmpty">
                    <tr>
                        <th v-for="column in visibleColumns" :key="`${column.key}-header`">
                            <div>
                                <!-- override head -->
                                <slot :name="`head-${column.key}`" :column>
                                    <!-- default head -->
                                    <slot name="head" :column>
                                        <span v-html="column.label || column.key"></span>
                                    </slot>
                                </slot>
                            </div>

                            <span class="sort-control" v-if="!column.noSort">
                                <span
                                    tabindex="0"
                                    @keypress.space.prevent="sortBy(column.key, true)"
                                    @keypress.enter="sortBy(column.key, true)"
                                    @click="sortBy(column.key, true)"
                                    :class="{ active: sortDesc && sortColumn == column.key }"
                                    title="Sort descending"
                                    >▼</span
                                >
                                |
                                <span
                                    tabindex="0"
                                    @keypress.space.prevent="sortBy(column.key, false)"
                                    @keypress.enter="sortBy(column.key, false)"
                                    @click="sortBy(column.key, false)"
                                    :class="{ active: !sortDesc && sortColumn == column.key }"
                                    title="Sort ascending"
                                    >▲</span
                                >
                            </span>
                        </th>
                    </tr>
                </thead>
                <tbody>
                    <tr
                        v-for="(item, i) in visibleItems"
                        :key="`row-${i}`"
                        @click="model = item"
                        :class="model === item ? 'selected' : ''"
                        :tabindex="selectable ? 0 : -1"
                        @keydown="
                            (e: KeyboardEvent) => {
                                if (e.key === ' ' || e.key === 'Enter') model = item
                            }
                        "
                    >
                        <td
                            v-for="column in visibleColumns"
                            :key="`row-${i}-column-${column.key}`"
                            :style="{ textAlign: column.align }"
                            :class="{ button: column.button }"
                        >
                            <!-- specific cell rendering -->
                            <slot :name="`cell-${column.key}`" :column :item :value="item[column.key]">
                                <!-- generic cell rendering -->
                                <slot name="cell" :column :item :value="item[column.key]">
                                    <!-- formatted -->
                                    <template v-if="column.format">
                                        {{ column.format(item) }}
                                    </template>
                                    <!-- default rendering -->
                                    <template v-else>
                                        {{ item[column.key] }}
                                    </template>
                                </slot>
                            </slot>
                        </td>
                    </tr>
                </tbody>
            </table>

            <GTablePaginator v-if="numPages > 1" v-model="page" :numPages />
        </template>
    </GCard>
</template>

<script setup lang="ts" generic="T">
import type { Column } from "@/types/ui/table"

// --- props ---
const {
    items,
    columns,
    title,
    loading,
    selectable,
    sortColumn: initSortColumn,
    sortDesc: initSortDesc = true,
} = defineProps<{
    items: T[]
    columns: Column<T>[]
    title?: string
    loading?: boolean
    selectable?: boolean
    sortColumn?: string
    sortDesc?: boolean
}>()

// --- data ---
const model = defineModel<T>()
const page = ref<number>(1)
const sortColumn = ref<string | undefined>(initSortColumn)
const sortDesc = ref<boolean>(initSortDesc)

// --- computed ---
const classes = computed(() => ({ loading: loading, selectable: selectable }))
const isEmpty = computed<boolean>(() => items?.length === 0)

const visibleItems = computed<T[]>(() => {
    // only paginate
    if (!sortColumn.value) return paginate(items)

    // sort and then paginate
    const sortOn =
        columns.find((column) => column.key === sortColumn.value)?.sortOn ??
        columns.find((column) => column.key === sortColumn.value)?.format ??
        ((x: T): T => x[sortColumn.value])
    const sorted = items.toSorted((a: T, b: T) => {
        const order = sortDesc.value ? -1 : 1
        return order * compareAny(sortOn(a), sortOn(b))
    })
    return paginate(sorted)
})
const numPages = computed<number>(() => Math.ceil((items?.length ?? 0) / pageSize))
const pageSize = 15
const visibleColumns = computed<Column<T>[]>(() => columns.filter((field) => !field.hidden))

// --- watch ---
// reset to first page on item change
watch(
    () => items,
    () => {
        page.value = 1
    },
)

function compareAny(a: unknown, b: unknown): number {
    // // null and undefined are always smaller
    // if (nu(a) && nu(b)) return 0
    if (nu(a)) return -1
    if (nu(b)) return 1

    // // Infinity is always bigger
    if (a === Number.POSITIVE_INFINITY) return 1
    if (b === Number.POSITIVE_INFINITY) return -1

    if (typeof a === "number" && typeof b === "number") {
        return a - b
    }
    if (typeof a === "string" && typeof b === "string") {
        return -a.localeCompare(b)
    }
    if (Array.isArray(a) && Array.isArray(b)) {
        if (a.length === 0 && b.length === 0) return 0
        if (a.length === 0) return -1
        if (b.length === 0) return 1
        return compareAny(a[0], b[0]) // Approximate
    }
    if (typeof a === "boolean" && typeof b === "boolean") {
        if (a === b) return 0
        if (a) return 1
        return -1
    }
    //garbage
    return 0
}

function nu(v: unknown): boolean {
    return v === null || v === undefined || v === ""
}

/** Return items on page N */
function paginate(items: T[]): T[] {
    return items.slice((page.value - 1) * pageSize, page.value * pageSize)
}

/** Sort by the column key, descending or ascending, and jump to the first page. */
function sortBy(key: string, desc: boolean): void {
    sortColumn.value = key
    sortDesc.value = desc
    page.value = 1
}
</script>

<style scoped lang="scss">
table {
    border-collapse: collapse;
    // scroll on overflow requires display: block
    display: block;
    max-width: 100%;
    overflow-x: auto;

    &.loading {
        filter: blur(5px);
    }

    tr {
        border: 1px solid var(--int-very-light-grey-hover);
    }

    thead {
        tr {
            background-color: var(--int-theme);

            th {
                font-size: 0.85rem;
                letter-spacing: 0.05rem;
                text-transform: uppercase;
                padding: 0.25rem 0.6rem;
                // only if it contains sort controls
                &:has(.sort-control) {
                    vertical-align: bottom;
                }

                .sort-control {
                    white-space: nowrap;
                    color: var(--white);
                    user-select: none;

                    span {
                        cursor: pointer;

                        &.active {
                            color: black;
                        }
                    }
                }
            }
        }
    }

    tbody {
        tr {
            height: 1rlh;

            &:nth-child(even) {
                background: #fff;
            }

            &:nth-child(odd) {
                background: var(--int-very-light-grey);
            }

            td {
                padding: 0.4rem;
                :deep(button) {
                    padding: 8px;
                }

                &.button {
                    padding: 0 !important;
                    height: 100%;
                    :deep(button) {
                        width: 100%;
                        height: 100%;
                        border: 0;
                    }
                }
            }
        }
    }

    &.selectable {
        tbody tr {
            cursor: pointer;

            &.selected {
                background-color: var(--int-theme-lighter);
            }

            &:hover:not(.selected) {
                background: var(--int-very-light-grey-hover);
            }
        }
    }
}
</style>
