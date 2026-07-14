<script setup lang="ts">
import ExternalLink from "@/components/links/ExternalLink.vue"
</script>

<template>
    <p>
        The <router-link to="/overview/benchmarks">benchmarks page</router-link> shows the performance of taggers on the
        default datasets. Choose a dataset and the annotation(s) you want to evaluate to see the <i>micro</i> and
        <i>macro</i> scores (<i>accuracy, precision, recall, F1</i>). Grouping by an annotation determines the classes
        used for macro scoring. Multiple analyses, like <code>tboek (het+boek)</code>, can also be evaluated separately.
        For more details, visit
        <ExternalLink href="/galahad/help/general#evaluation-terminology"
            >the manual on evaluation terminology</ExternalLink
        >.
    </p>
    <p>
        Clicking on <i>true positives / false negatives / no match</i> shows some samples in that category. To inspect
        the performance of an individual tagger, click on the <i>detailed evaluation</i>.
    </p>
    <p>
        For more details on the datasets, see the
        <router-link to="/overview/datasets">datasets overview</router-link>.
    </p>
    <p>
        <i>
            <strong>Note:</strong>
            When taggers use a different tagset than the reference tagset, the score can be very low.
        </i>
    </p>
</template>
