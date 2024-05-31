<template>
  <GCard :title="`Logged in as ${user.user.id}`">
    <template #help>Find information about your account here</template>
    <p>
      Find information about your account here.
      <br /><br />
      Username:<br />
      <b>{{ user.user.id }}</b>
      <br /><br />
      <b v-if="user.user.admin"> You are an admin.</b>
    </p>
    <!-- <div>
      Things that could be listed here:
      <ul>
        <li>last login</li>
        <li>first login</li>
        <li>number of corpora</li>
        <li>used processing time</li>
        <li>etc...</li>
      </ul>
    </div> -->
  </GCard>
</template>

<script setup lang='ts'>
// Libraries & stores
import stores, { UserStore } from '@/stores'
// Components
import { GCard } from '@/components'

// Stores
const user = stores.useUser() as UserStore

</script>

<style scoped="true" lang="scss">
.center {
  text-align: center
}

.content {
  background-color: white !important;
}

.g-card {
  overflow-y: auto
}
</style>
