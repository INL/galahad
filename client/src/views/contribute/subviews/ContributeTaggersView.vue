<template>
    <div>
        <GCard noHelp>
            <template #title>Contribute a tagger</template>
            <p>
                If you have trained your own tagger, you can make it available on GaLAHaD.
                This works by creating a Docker container for it with a GaLAHaD-standardized API. A base
                implementation is
                available on our GitHub.
            </p>
            <p>
                See <ExternalLink href="https://github.com/INL/galahad-taggers-dockerized">galahad-taggers-dockerized
                </ExternalLink> for more information.
            </p>
        </GCard>
    </div>
</template>

<script setup lang='ts'>
import { GCard, GInfo, ExternalLink } from '@/components'
</script>
