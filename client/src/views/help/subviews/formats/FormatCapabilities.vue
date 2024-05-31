<template>
    <div>
        <h4>Supports:</h4>
        <span v-if="uploadable">
            <i class="fa fa-fw fa-upload" aria-hidden="true"></i>Uploading
        </span>
        <span v-if="exportable">
            <i class="fa fa-fw fa-share-square" aria-hidden="true"></i>Exporting
        </span>
        <span v-if="mergeable">
            <i class="fa fa-fw fa-compress" aria-hidden="true"></i>Merging
        </span>
    </div>
</template>

<script setup lang="ts">
defineProps({
    uploadable: { type: Boolean, default: false },
    exportable: { type: Boolean, default: false },
    mergeable: { type: Boolean, default: false },
})
</script>

<style scoped lang="scss">
h4 {
    display: inline;
    margin: 0 0.3rem 0 0;
}

span {
    display: inline-block;
    border: 1px solid var(--int-grey);
    padding: 0.3rem;
    margin: 0 0 0 0.3rem;
    background-color: var(--int-green);
}

.fa {
    position: relative;
    top: 0.1rem;
}
</style>