<template>
    <table style="font-family: monospace">
        <tr v-for="key in keys" :key="key">
            <td>
                <span>{{ key }}</span>
            </td>
            <td><KeyValues v-if="isObject(data[key])" :data="data[key]" :highDuration="innerHighDuration"/>
                <span v-else-if="key === 'FAILURE'">{{format( key, data[key] )}}</span>
                <a v-else-if="key === 'link'" :href="data[key]" target="_blank" rel="noopener noreferrer">here</a>
                <b v-else-if="key === 'duration'" :style="data[key] === highDuration && highDuration > 0 ? 'color: var(--int-red);' : ''">{{ format( key, data[key]) }}</b>
                <span v-else>{{ format( key, data[key] ) }}</span>
            </td>
        </tr>
    </table>
</template>

<script lang='ts'>
import {defineComponent} from 'vue';

export default defineComponent({
    name: "KeyValues",
    props: {
        data: { default: "No data" },
        highDuration: { default: -1 }
    },
    computed: {
        keys() {
            if (!this.data || typeof this.data !== "object")
                return [];
            return Object.keys(this.data)
                .filter(key => key !== "preview" && key !== "lastModified" && key !== "tagger");
        },
        innerHighDuration() {
            if (typeof this.data !== "object")
                return -1;
            return Math.max(...Object.values(this.data)
                .filter(x => x)
                .filter(x => typeof x === "object")
                .filter(x => !isNaN((x as any).duration))
                .map(x => parseInt((x as any).duration)));
        }
    },
    methods: {
        format(key: string, value: any) {
            // This is a coarse ad hoc formatting function for my needs
            // feel free to refine it and/or make it configurable
            if (key === "duration") {
                if (isNaN(value))
                    return value;
                const num = parseInt(value);
                const G = 1000000000;
                if (num < 1000)
                    return num + "ns";
                if (num < 1000000)
                    return num / 1000 + "us";
                if (num < G)
                    return num / 1000000 + "ms";
                if (num < 60 * G)
                    return num / G + "s";
                if (num < 3600 * G)
                    return num / (60 * G) + "m";
                return num / (3600 * G) + "h";
            }
            if (key === "start" || key === "end" || key === "FAILURE") {
                return new Date(value / 1000000).toLocaleString();
            }
            return value;
        },
        isObject(value: any) {
            return typeof value === "object";
        }
    },
});
</script>

<style scoped lang="scss">
table {
  border-collapse: collapse;
}

tr {
    border-bottom: 1px dashed var(--int-very-light-grey);
    opacity: 0.8;
}

tr tr {
    border-bottom: 1px dotted var(--int-very-light-grey)
}

tr:last-child {
    border-bottom: none;
}

td {
    vertical-align: text-top;
    padding-right: 5em;
}

tr:hover {
    opacity: 1;
}

</style>