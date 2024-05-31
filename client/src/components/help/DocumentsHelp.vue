<template>
    <div>
        <h2>Step 2: Documents</h2>
        Upload documents to a corpus.
        <p>
            The platform accepts the following formats:
        </p>
        <ul>
            <li>(Text-like formats) Plain text, Tab separated Values (TSV), CoNLL-U</li>
            <li>(XML formats) TEI P5, TEI P5 Legacy, TEI P4, NLP Annotation Format (NAF), Format for Linguistic
                Annotation (FoLiA)</li>
        </ul>
        <ol>
            <li>Select files to upload in the upload field.</li>
            <li>Press the upload button.</li>
            <li>
                Keep the application open during the upload. If an upload failed (for example due
                to malformed XML) an error will be shown.
            </li>
            <li>After uploading, your documents are shown below.</li>
        </ol>
        Individual documents can be previewed, downloaded or deleted.
        <GNav v-if="showLink" :route="{ path: '/annotate/documents' }">
            Go to <i>Annotate &gt; Documents</i>
        </GNav>

        <!-- Mimic <HelpLink/>. HelpLin always points to help/general, but formats is a separate page. -->
        <template v-if="!showLink">
            <div style="text-align: center; margin-top: 1em">
                <a href='/galahad/help/formats' target="_blank" rel="noopener noreferrer">
                    read more on formats
                </a>
            </div>
        </template>

    </div>
</template>
<script setup lang="ts">
import { GNav } from "@/components";
defineProps({
    showLink: Boolean,
})
</script>

<style scoped lang="scss">
a:visited {
    color: black;

}
</style>