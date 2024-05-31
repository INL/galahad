<template>
    <div><GCard>
        <template #title>Contribute a tagger</template>
        <template #help>Creating linguistic infrastructure is a lot of work. Why not do it together?</template>
        <p>If you have trained your own tagger, you can opt to make it available through Galahad. Read below how this works.
        </p>

        <h3>Docker</h3>

        <p>
            Galahad uses embedded Docker containers running the taggers. You can share your tagger as a Docker image with us
            and
            other researchers. The easiest way to create a Docker image that implements the basic Galahad API is to extend
            from
            the galahad-tagger-base image. You can get it from the docker hub.
        </p>
        <p>
            <GInfo>
                Get <ExternalLink
                    href="https://hub.docker.com/repository/docker/instituutnederlandsetaal/taggers-dockerized-base">
                    instituutnederlandsetaal/galahad-tagger-base</ExternalLink> from hub.docker.com (This docker repository
                may not
                be public during beta.)
            </GInfo>
        </p>

        <h3>Specifications</h3>

        <p>You need to provide some specifications of your tagger in a yaml file.
            The file will be named <code>TAGGER_ID.yml</code>.
            So a tagger named 'my-tagger' needs to provide a <code>my-tagger.yaml</code> file that looks like:</p>
        <p>
            <GInfo>
                <pre>
                    <code>
# my-tagger.yaml
description: "This is a short description of my tagger."
tagset: TDN # Or any other tagset
eraFrom: "0"
eraTo: "2020"
produces: # List of the output types of my-tagger
- LEM
- POS
- TOK
attributions: # List any number of attributions
creator: galahad dev
links: # List any number of links
- name: website
href: https://ivdnt.org
                    </code>
                </pre>
                <i>(subject to change during beta.)</i>
            </GInfo>
        </p>

        <h3>Github</h3>

        <p>For more info and examples see our Github</p>

        <p>
            <GInfo>
                <ExternalLink href="https://github.com/INL/Galahad">
                    INL/Galahad</ExternalLink> and
                <ExternalLink href="https://github.com/INL/galahad-taggers-dockerized">INL/galahad-taggers-dockerized</ExternalLink>
                at github.com <i>(Repository may not be public during beta.)</i>
            </GInfo>
        </p>
        Base images for several frameworks already exist. To read how to submit a tagger created with such a framework, see
        examples on GitHub.

    </GCard></div>
</template>

<script setup lang='ts'>
import { GCard, GInfo, ExternalLink } from '@/components'
</script>

