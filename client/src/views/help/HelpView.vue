<template>

  <GTabs class="level-2" :basePath="'/help'" :tabs="[
    {id: 'general', title: 'General'},
    {id: 'formats', title: 'Formats' },
    {id: 'glossary', title: 'Glossary' },
  ]"/>

</template>

<script lang='ts'>
import {defineComponent} from 'vue'
import { GTabs } from '@/components'

export default defineComponent({
  name: "HelpView",
  components: {
    GTabs
  }
})
</script>

