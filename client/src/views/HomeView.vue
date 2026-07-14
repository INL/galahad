<template>
    <GCard article>
        <h1>Generating Linguistic Annotations for Historical Dutch</h1>
        <div>
            <section>
                <p>
                    Historical texts are essential source material for both linguistic and digital humanities research.
                    Adding linguistic annotation (part of speech and modern Dutch lemma) to historical text helps to
                    make the data more accessible. Users need not be concerned with historical spelling variation when
                    querying and analysing the data.
                </p>
                <p>
                    <em>GaLAHaD</em> serves two purposes. One is to make annotation and tool evaluation easily
                    accessible to researchers, the other to make it easy for developers to contribute their tools and
                    models to the platform, and thus compare them to other tools with gold standard material.
                </p>
                <p>
                    <em>GaLAHaD</em> is designed to enable end users to choose the optimal path for their material.
                    Apart from the basic task of uploading and annotating corpus material, <em>GaLAHaD</em> provides
                    options to inspect and evaluate the result of the annotation process, in order to raise the
                    awareness of typical errors and biases in the tools. The functionality of comparing annotation
                    layers enables users to assess the accuracy of different tools on their data. It can be used both to
                    evaluate a layer added by an automatic tagger with respect to a gold standard reference layer, or to
                    compare layers added by different taggers. Disagreement between layers is not only represented by
                    global statistics, but also illustrated by examples which are immediately visible in the tool. The
                    annotated material can be uploaded to the Autosearch corpus exploration environment and to the
                    <em>LAnCeLoT</em> tool for manual correction of linguistic annotation.
                </p>
                <p>
                    For tool developers, the docker-based application architecture ensures easy contribution of tools to
                    the platform. The application and taggers are hosted by the INT and accessible with any
                    CLARIN-account. There is also the option to self-host an instance using the publicly available
                    docker images from the
                    <ExternalLink href="https://hub.docker.com/u/instituutnederlandsetaal"
                        >INT Docker Hub
                    </ExternalLink>
                    or the open source code available on
                    <ExternalLink href="https://github.com/instituutnederlandsetaal/galahad">GitHub</ExternalLink>.
                </p>
            </section>
            <figure>
                <img
                    src="@/assets/logo/galahad-graal-klein.png"
                    alt="In surprise, Galahad drops the grail from his hands when he sees what's inside."
                />
                <figcaption>
                    <blockquote cite="https://galerij.kb.nl/kb.html#/nl/lancelot/page/45/zoom/5/lat/-66/lng/-107">
                        <p>
                            &ldquo;Ende seget datmen daer doe wan <br />
                            Galaadten, den goeden man, <br />
                            ja Den besten riddre die wesen mochte <br />
                            Ende die ten inde brochte <br />
                            Nader historien tale <br />
                            Davonturen vanden Grale,&rdquo;
                        </p>
                    </blockquote>
                    <cite>
                        <ExternalLink href="https://galerij.kb.nl/kb.html#/nl/lancelot/page/45/zoom/5/lat/-66/lng/-107">
                            Lancelotcompilatie, fol. 41vb, vers 15.444-15449
                        </ExternalLink>
                    </cite>
                </figcaption>
            </figure>
        </div>
    </GCard>
</template>

<style scoped lang="scss">
:deep(article) {
    align-items: center !important;
    max-width: none !important;

    div {
        display: flex;
        flex-flow: row wrap;
        justify-content: center;
        gap: 1rem;

        section {
            display: flex;
            gap: 1rem;
            flex-direction: column;
            flex: 2 1 500px;
            max-width: 700px;
        }

        figure {
            flex: 1 2 300px;
            max-width: 500px;
            display: flex;
            flex-direction: column;

            figcaption {
                text-align: center;
                font-family: "Times New Roman", Times, serif;
                font-style: italic;

                img {
                    width: 500px;
                    max-width: 100%;
                }

                blockquote {
                    font-size: 1.2rem;
                }
            }
        }
    }
}
</style>
