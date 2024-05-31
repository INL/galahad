<template>
  <div class="flex" style="padding: 0 !important;">
    <GCard headless noHelp class="left">
      <h2>Contents</h2>
      <ul>
        <li v-for="component, id in help">
          <a :href="`#${id}`">{{ id }}</a>
        </li>
      </ul>
    </GCard>
    <div class="right">
      <GCard :id="id" v-for="component, id in help" headless noHelp>
        <component :is="component" showLink />
      </GCard>
    </div>
  </div>
</template>

<script setup>
import { GCard } from '@/components'
import help from '@/components/help'
</script>

<style scoped>
.right :deep(.content-wrapper)>.content {
  flex: 0 1 800px !important;
}

a,
a:hover,
a:active,
a:visited {
  color: #000;
}

.flex {
  display: flex !important;
  flex-direction: row !important;
  overflow: hidden !important;
}

.right {
  overflow-y: auto;
}

.left {
  min-width: initial !important;
  flex: 1;
  border-right: 1px solid var(--int-light-grey);
  padding-right: 3em;
}

@media (max-width: 800px) {
  .flex {
    flex-direction: column !important;
  }

  .left {
    border-right: none;
    display: block;
  }

  :deep(.left) .content-wrapper {
    justify-content: initial;
  }
}
</style>