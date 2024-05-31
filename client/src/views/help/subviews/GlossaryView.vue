<template>
    <div> <!--Extra div for padding on small screens.-->
        <GCard title="Glossary">
            <template #help>Find a description of key terms in the glossary.</template>

            <h3 id="blacklab">BlackLab</h3>
            BlackLab allows fast, complex searches with accurate hit highlighting on large, tagged and annotated, bodies of
            text.
            TBD

            <h3 id="clariah">CLARIAH</h3>
            Common Lab Research Infrastructure for the Arts and Humanities
            See <ExternalLink href="https://www.clariah.nl/">clariah.nl</ExternalLink> and
            <ExternalLink href="https://ivdnt.org/onderzoek-projecten/clariah/">CLARIAH at INT</ExternalLink>

            <h3 id="cobalt">CoBaLT</h3>
            TBD

            <h3 id="era">Era</h3>
            An era indicates a span of time.
            For corpora it spans the years during which the documents it contains were written.
            For taggers, it indicates for which years the tagger was optimized.
            The tagger may have been trained using data from the indicated era.

            <h3 id="hypothesis_layer">Hypothesis layer</h3>
            The hypothesis layer is a candidate layer. After adding it to your documents, you can evaluate it.
            You can compare the hypothesis layer to a reference layer.

            <h3 id="int">INT</h3>
            Instituut voor de Nederlandse taal, the Dutch Language Institute.
            See <ExternalLink href="https://www.ivdnt.org/">ivdnt.org</ExternalLink>

            <h3 id="reference_layer">Reference layer</h3>
            The reference layer is used as the ground truth of your documents.
            If your documents contain a source layer, you can use it as a reference. You can also choose a different layer.

        </GCard>
    </div>
</template>

<script setup lang='ts'>
import { GCard, ExternalLink } from '@/components'
</script>

<style scoped="true">
:deep(.content-wrapper)>.content {
    flex: 0 1 800px !important;
}
</style>
