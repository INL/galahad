<template>
    <div>
        <h2>Step 4: Evaluate</h2>
        <p>
            Inspect the tagged layers and evaluate the performance of the taggers by selecting a
            <ExternalLink href="/galahad/help/general#evaluation">
                hypothesis layer
            </ExternalLink>
            and a
            <ExternalLink href="/galahad/help/general#evaluation">
                reference layer
            </ExternalLink>.
            As a reference layer, you can choose the source layer (uploaded annotations) or a version of your corpus
            tagged with a different tagger. Several aspects can be inspected: the distribution of the linguistic
            annotation, accuracy metrics* and further information on the Pos Confusion. You can also download these
            evaluations as CSV files.
        </p>
        <DifferentTagsetsHelp />
    </div>
</template>

<script setup lang="ts">
// Components
import DifferentTagsetsHelp from '@/components/help/DifferentTagsetsHelp.vue'
</script>
