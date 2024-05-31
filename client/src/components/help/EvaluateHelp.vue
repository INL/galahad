<template>
    <div>
        <h2>Step 4: Evaluate</h2>
        <p>
            Inspect the tagged layers and evaluate the performance of the taggers by selecting a <GlossaryLink
                gloss="hypothesis_layer">
                hypothesis layer</GlossaryLink> and a <GlossaryLink gloss="reference_layer">reference layer
            </GlossaryLink>.
            As a reference layer, one can choose the source layer (uploaded annotations) or a version of your corpus
            tagged
            with a different tagger. Several aspects can be inspected: the distribution of the linguistic annotation,
            accuracy metrics* and further information on the PoS confusion. You can also download these evaluations as
            CSV
            files.
        </p>
        <DifferentTagsetsHelp />
    </div>
</template>

<script setup lang="ts">
// Components
import { GlossaryLink } from '@/components'
import DifferentTagsetsHelp from '@/components/help/DifferentTagsetsHelp.vue'
</script>
