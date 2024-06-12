<template>
    <div>
        <GCard noHelp>
            <template #title>Contribute a dataset</template>
            <p>
                Apart from contributing new taggers to the GaLAHaD platform, you can also contribute training and
                evaluation gold standard datasets you have produced. Please contact us at <MailAddress />.
            </p>
        </GCard>
    </div>
</template>

<script setup lang='ts'>
import { GCard } from '@/components'
import MailAddress from '@/components/links/MailAddress.vue';
</script>
