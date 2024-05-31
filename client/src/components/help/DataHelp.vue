<template>
    <div>
        <h2>Step 1: Data</h2>
        Create one or more corpora and upload documents to them.
        <ol type="a">
            <li>
                Create a corpus
                <ol>
                    <li>Enter a name and an
                        <GlossaryLink gloss="era" /> in the bottom line of the corpora list
                    </li>
                    <li>Press 'create'</li>
                    <img width="600px" src="@/assets/get_started/create-corpus.png" />
                    <li>(Optional) Create additional corpora. Click on a corpus to select it for processing.</li>
                </ol>
                <GNav :route="{ path: '/annotate/corpora' }">Go to <i>Annotate &gt; Corpora</i></GNav>
            </li>
            <hr>
            <li>
                Upload documents
                <p>The platform accepts plain text, tab-separated text, and TEI, NAF or FoLiA XML files,
                    <GNav :route="{ path: '/help/formats' }">see <i>Help &gt; Formats</i></GNav>
                </p>
                <ol>
                    <li>Select files to upload in the upload field</li>
                    <li>Press the upload button</li>
                    <img width="600px" src="@/assets/get_started/upload-documents.png" />
                    <li>Keep the application open during the upload. If an upload failed (for example due to malformed
                        XML) an
                        error will display.</li>
                    <li>After uploading, your documents will show in <i>Documents</i></li>
                </ol>
                <GNav :route="{ path: '/annotate/documents' }">Go to <i>Annotate &gt; Documents</i></GNav>
            </li>
        </ol>
    </div>
</template>

<script setup lang="ts">
// Components
import { GNav, GlossaryLink } from '@/components'
</script>
