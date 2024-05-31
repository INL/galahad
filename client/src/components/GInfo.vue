<template>
    <div :class="cssclass + ' infocontainer'">
        <GSpinner :error="error" :still="!spinner" :small="small" :large="large" class="symbol" />
        <div class="content">
            <slot>Someone forgot to put the info.</slot>
        </div>
    </div>
</template>

<script lang='ts'>
import { defineComponent } from 'vue'
// Component dependencies.
import GSpinner from '@/components/GSpinner.vue'

export default defineComponent({
    name: "GInfo",
    components: {
        GSpinner
    },
    props: {
        error: { type: Boolean, default: false },
        large: { type: Boolean, default: false },
        small: { type: Boolean, default: false },
        spinner: { type: Boolean, default: false }
    },
    computed: {
        cssclass() {
            let ret = ""
            ret += this.error ? 'error ' : ''
            ret += this.small ? 'small ' : ''
            ret += this.large ? 'large ' : ''
            return ret
        }
    }
});
</script>

<style scoped lang="scss">
svg path {
    fill: red;
}

.infocontainer {
    background-color: var(--white);
    color: var(--black);
    padding: 10px;
    border: 1px solid var(--int-light-grey);
    margin: 10px auto;
    max-width: 1200px;
}

.infocontainer.error {
    border: 2px solid var(--int-red);
}

svg {
    height: 40px;
    width: 40px;
    float: left;
}

svg.small {
    height: 20px;
    width: 20px;
}

svg.large {
    height: 100px;
    width: 100px;
}

.content {
    margin: 5px;
    margin-left: 60px; // 100px because of the svg
    margin-right: 20px;
}

.infocontainer.small .content {
    margin: 5px;
    margin-left: 40px; // 100px because of the svg
    margin-right: 10px;
}

.infocontainer.large .content {
    margin: 10px;
    margin-left: 120px; // 100px because of the svg
    margin-right: 50px;
}
</style>