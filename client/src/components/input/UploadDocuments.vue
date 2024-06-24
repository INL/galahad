<template>
    <div>
        <!-- Drag and drop from https://codepen.io/nekobog/pen/JjoZvBm -->
        <div id="dropZone"></div>

        <!-- Styled label for input -->
        <label for="file-upload" class="custom-file-upload">
            <svg class="svg-icon" aria-hidden="true" role="img" focusable="false" xmlns="http://www.w3.org/2000/svg"
                width="20" height="12" viewBox="0 0 20 12">
                <polygon class="st0" points="10,4.2 2.2,12 0.1,9.9 10,0 19.9,9.9 17.8,12 "></polygon>
            </svg>
            Select file(s) or drag & drop
        </label>
        <!-- Actual input -->
        <input type="file" ref="uploadInput" name="filefield" multiple id="file-upload" style="display: none;"
            accept=".xml, .tsv, .txt, .zip, .conllu, .naf"
            @change="e => filesToUpload = Object.values(e.target.files as FileList)" />


        <!-- List of currently selected files. -->
        <ul v-if="filesToUpload.length > 0">
            <!-- First 5 file names are shown-->
            <li v-for="file in filesToUpload.slice(0, 4)" :key="file.name">{{ file.name }}</li>
            <!-- Overflow -->
            <li v-if="filesToUpload.length > 4">
                + {{ filesToUpload.length - 4 }} more {{ filesToUpload.length == 5 ? "file" : "files" }}
            </li>
        </ul>

        <!-- Confirmation and clear buttons after a selection has been made -->
        <template v-if="filesToUpload.length != 0">
            <GButton green @click="documentsStore.uploadAll(); $refs.uploadInput.value = null">
                Upload
            </GButton>
            <GButton plain @click="filesToUpload = []; $refs.uploadInput.value = null">
                &#10006;&nbsp;clear
            </GButton>
        </template>

        <!-- Error for illegal selection -->
        <GInfo error v-if="illegalFiles.length > 0">
            You have selected some filetype(s) that are not supported in GaLAHaD:
            <ul>
                <li v-for="file in illegalFiles" :key="file.name">
                    {{ file.name }}
                </li>
            </ul>

            Do you want to upload the text? You can:
            <ol>
                <li>copy-paste it to NotePad</li>
                <li>save as .txt</li>
                <li>upload it to GaLAHaD</li>
            </ol>
        </GInfo>

        <!-- upload busy info -->
        <GInfo v-show="uploadBusyCount > 0" spinner>
            Upload will continue in the background, please don't close the browser. Currently uploading:
            {{ uploadBusyCount }}
        </GInfo>

        <!-- Errors for files that could not be parsed by the server (e.g. broken xml tags)-->
        <GInfo error v-show="uploadErrorCount > 0">
            <div v-for="(value, key) in uploading" :key="key">
                <span v-if="value.status == 'error'">{{ key }}: {{ value.message }}</span>
            </div>
        </GInfo>
    </div>
</template>

<script setup lang='ts'>
// Libraries & stores
import { onMounted, ref } from 'vue'
import stores from '@/stores'
import { storeToRefs } from 'pinia'
// Components
import { GButton, GInfo } from '@/components'

// Stores
const documentsStore = stores.useDocuments()
const { filesToUpload, illegalFiles, uploadBusyCount, uploadErrorCount, uploading } = storeToRefs(documentsStore)

// Fields
var dropZone = ref(null as any as HTMLElement);

// Methods
function showDropZone(e: DragEvent) {
    if (e.dataTransfer!.types.includes('Files')) {
        dropZone.value.style.display = "block"
    }
}
function hideDropZone() {
    dropZone.value.style.display = "none";
}
function handleDrop(e: DragEvent) {
    e.preventDefault();
    hideDropZone();
    filesToUpload.value = [...e.dataTransfer!.files]
}

// Watches & mounts
onMounted(() => {
    dropZone.value = document.getElementById('dropZone') as HTMLElement;
    // Register drag events
    window.addEventListener('dragenter', showDropZone)
    dropZone.value.addEventListener('dragleave', hideDropZone)
    dropZone.value.addEventListener('drop', handleDrop)
    // Apparently, this is needed to prevent the browser from opening the file.
    dropZone.value.addEventListener('dragover', e => e.preventDefault())
})
</script>

<style scoped lang="scss">
.custom-file-upload {
    border: 0px solid #ccc;
    background-color: var(--int-theme);
    display: inline-block;
    padding: 10px 12px;
    cursor: pointer;
    margin-right: 10px;
    font-style: normal;

    &:hover {
        background-color: var(--int-theme-hover);
    }

    &:active {
        background-color: var(--int-theme-active);
    }
}

ul,
ol {
    width: fit-content;
    text-align: left;
    margin: 1em auto;
}

#dropZone {
    display: none;
    position: fixed;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    background-color: var(--int-theme);
    opacity: 0.4;
    z-index: 3;
}
</style>