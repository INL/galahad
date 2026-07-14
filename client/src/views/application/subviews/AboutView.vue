<template>
    <GCard article>
        <h2>About the application</h2>
        <p>
            GaLAHaD is created by the
            <ExternalLink href="https://www.ivdnt.org">Dutch Language Institute</ExternalLink>. It consists of a
            TypeScript Vue 3 frontend, a Kotlin Spring Boot backend, and nginx proxy. Annotation tools are provided by
            the companion project
            <ExternalLink href="https://github.com/instituutnederlandsetaal/galahad-taggers-dockerized"
                >galahad-taggers-dockerized on GitHub</ExternalLink
            >
            which maintains a Bottle API in Python.
        </p>

        <p>
            Visit
            <ExternalLink href="https://github.com/instituutnederlandsetaal/galahad">GaLAHaD on GitHub</ExternalLink> or
            download the Docker images from the
            <ExternalLink href="https://hub.docker.com/u/instituutnederlandsetaal">INT Docker Hub</ExternalLink>.
        </p>

        <h2>About GaLAHaD</h2>
        <p>
            GaLAHaD (Generating Linguistic Annotations for Historical Dutch) enables linguists to enrich their corpora
            with automatic annotations and allows for evaluation of annotation tools on a deep level.
        </p>
        <p>
            For more information on methodologically sound evaluation and data enrichment, please visit
            <ExternalLink href="https://enrichment.ivdnt.org/">enrichment.ivdnt.org</ExternalLink>: the website for
            <i>Task 3.1</i> in the <ExternalLink href="https://sshoc.nl/">SSHOC-NL project</ExternalLink>.
        </p>
        <p>
            For this work, funding was received from NWO.
            <br />
            (Clariah Plus project 184.034.023 & SSHOC-NL 184.036.020)
        </p>

        <h2>Colophon</h2>
        <h3>About the logo</h3>
        <figure>
            <img
                src="@/assets/logo/galahad-graal-klein.png"
                alt="In surprise, Galahad drops the grail from his hands when he sees what's inside."
            />
            <figcaption>In surprise, Galahad drops the grail from his hands when he sees what's inside.</figcaption>
        </figure>

        <p>
            For this watercolor, Karina van Dalen-Oskam was inspired by a detail from a miniature in a manuscript by
            Jacob van Maerlants Spiegel Historiael (
            <ExternalLink href="https://manuscripts.kb.nl/zoom/BYVANCKB%3Amimi_ka20%3A052r_min_b-c">
                Den Haag, Koninklijke Bibliotheek, KA XX fol. 52r
            </ExternalLink>
            ).
        </p>

        <h2>References</h2>
        <p>When referring to this work, please cite:</p>
        <p>
            <cite>
                Depuydt, Katrien, Jesse De Does, Vincent Prins, Mathieu Fannee, Roland de Bonth, Thomas Haga (2026), An
                infrastructure for Historical Dutch Corpus Development. In: Selected papers from the CLARIN Annual
                Conference 2025. DOI:
                <ExternalLink href="https://doi.org/10.3384/ecp222.1606"
                    >https://doi.org/10.3384/ecp222.1606</ExternalLink
                >
            </cite>
        </p>

        <h2 id="version">Version</h2>
        <KeyValues :data="versionYaml" />

        <footer>
            <a href="https://www.ivdnt.org">
                <img
                    src="@/assets/logo/ivdNt-logo-3regels.svg"
                    alt="Logo Instituut voor de Nederlandse taal"
                    title="Instituut voor de Nederlandse taal"
                />
            </a>
            <a href="https://www.clariah.nl">
                <img src="@/assets/logo/clariah.png" alt="Clariah logo" title="Clariah" />
            </a>
            <a href="https://www.nwo.nl/projecten/184036020">
                <img src="@/assets/logo/sshoc-nl.jpg" alt="SSHOC-NL logo" title="SSHOC-NL" />
            </a>
        </footer>
    </GCard>
</template>

<script setup lang="ts">
import versionYaml from "@/../version.yml"
</script>

<style scoped lang="scss">
footer {
    padding: 2rem 0rem;
    display: flex;
    flex-direction: row;
    gap: 1rem;
    flex-wrap: wrap;
    justify-content: center;
    align-items: center;

    img {
        width: 200px;
    }
}

cite {
    display: inline;
}

figure {
    width: 100%;
    display: flex;
    flex-direction: column;
    justify-content: center;
    align-items: center;

    img {
        max-width: 350px;
    }

    figcaption {
        text-align: center;
        font-family: "Times New Roman", Times, serif;
        font-style: italic;
    }
}
</style>
