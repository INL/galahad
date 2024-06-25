<template>
    <div>
        <GCard noHelp>
            <template #title>
                Generating Linguistic Annotations for Historical Dutch
            </template>

            <hr>

            <div class="flex">
                <div class="left">
                    <p>
                        Historical texts are essential source material for both linguistic and digital humanities
                        research. Adding linguistic annotation (part of speech and modern Dutch lemma) to historical
                        text helps to make the data more accessible. Users need not be concerned with historical
                        spelling variation when querying and analysing the data.
                    </p>
                    <p>
                        GaLAHaD serves two purposes. One is to make annotation and tool evaluation easily accessible to
                        researchers, the other to make it easy for developers to contribute their tools and models to
                        the platform, and thus compare them to other tools with gold standard material.
                    </p>
                    <p>
                        <i>GaLAHaD</i> is designed to enable end users to choose the optimal path for their material.
                        Apart from the basic task of uploading and annotating corpus material, <i>GaLAHaD</i> provides
                        options to inspect and evaluate the result of the annotation process, in order to raise the
                        awareness of typical errors and biases in the tools. The functionality of comparing annotation
                        layers enables users to assess the accuracy of different tools on their data. It can be used
                        both to evaluate a layer added by an automatic tagger with respect to a gold standard reference
                        layer, or to compare layers added by different taggers. Disagreement between layers is not only
                        represented by global statistics, but also illustrated by examples which are immediately visible
                        in the tool. The annotated material can be uploaded to the Autosearch corpus exploration
                        environment and to the <i>LAnCeLoT</i> tool for manual correction of linguistic annotation.
                    </p>
                    <p>
                        For tool developers, the docker-based application architecture ensures easy contribution of
                        tools to the platform. The application and taggers are hosted by the INT and accessible with any
                        CLARIN-account. There is also the option to self-host an instance using the publicly available
                        docker images from the
                        <a href="https://hub.docker.com/u/instituutnederlandsetaal">INT Docker Hub</a> or the open
                        source code available on <a href="https://github.com/INL/galahad">GitHub</a>.
                    </p>
                </div>

                <figure class="right">
                    <img src="@/assets/galahad-graal-medium.png" />
                    <figcaption>
                        <i>
                            &ldquo;Ende seget datmen daer doe wan<br>
                            Galaadde den goeden man,<br>
                            Den besten riddre die wesen mochte,<br>
                            Ende die ten inde brochte,<br>
                            Nader historien tale,<br>
                            Davonturen vanden grale&rdquo;<br>
                        </i>
                    </figcaption>
                </figure>
            </div>

        </GCard>
    </div>
</template>

<script setup lang='ts'>
import { GCard, ExternalLink } from '@/components'
</script>

<style scoped="true" lang="scss">
:deep(h3) {
    font-size: 30px;
    margin-bottom: 0;
}

.flex {
    display: flex;
    padding: 0 35px;
    flex-wrap: wrap;
    flex-direction: row;
    justify-content: center;
    gap: 35px;

    .left {
        flex: 2 1 500px;
        max-width: 700px;
    }

    .right {
        margin: 0;
        margin-bottom: 3em;
        flex: 1 2 300px;
        max-width: 500px;
        text-align: center;
        font-family: 'Times New Roman', Times, serif;
        font-size: 20px;

        img {
            object-fit: contain;
            width: 100%;
        }
    }
}


.content {
    background-color: white !important;
}

.g-card {
    overflow-y: auto
}
</style>
