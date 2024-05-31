<template>
    <a :href="url" @click.prevent="$router.push(contextualRoute)">
        <slot></slot>
    </a>
</template>

<script setup lang='ts'>
// Libraries & stores
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const $route = useRoute()

// Props
const props = defineProps({
    route: { type: Object, default: () => { return { path: "", query: "" } } },
})

// Fields
const contextualRoute = computed(() => {
    return {
        path: props.route.path || $route.path,
        query: props.route.query || $route.query,
    }
})
const url = computed(() => {
    return `${contextualRoute.value.path}?${serialize(contextualRoute.value.query)}`
})

// Methods
function serialize(obj) {
    const str = [];
    for (const p in obj)
        if (obj.hasOwnProperty(p)) {
            str.push(encodeURIComponent(p) + "=" + encodeURIComponent(obj[p]));
        }
    return str.join("&");
}
</script>
