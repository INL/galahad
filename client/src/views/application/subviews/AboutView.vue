<template>
    <GCard title="About">
        <template #help>Here you can find some information about the application and the exact version you are currently
            running.</template>
        <div>
            <a style="margin-left: auto;" href="https://www.ivdnt.org">
                <img src="@/assets/ivdNt-logo-3regels.svg" alt="Logo Instituut voor de Nederlandse taal"
                    title="Instituut voor de Nederlandse taal" />
            </a>
            <p>Galahad is created by the
                <ExternalLink href="https://www.ivdnt.org">
                    Dutch Language Institute
                </ExternalLink>
            </p>
            <h2>Version</h2>
            <KeyValues :data="versionYaml" />
        </div>
    </GCard>
</template>

<script setup lang='ts'>
import versionYaml from '@/../version.yml'
import { GCard, KeyValues } from '@/components'
</script>

<style scoped="true">
.center {
    text-align: center
}
</style>
