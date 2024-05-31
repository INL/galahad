import 'mutationobserver-shim'
import gcomponents from '@/components'
import App from './App.vue'
import router from './router'
import stores from './stores'
import { createApp, watch } from 'vue/dist/vue.esm-bundler' // bug doesn't let use use 'vue' here
import { createPinia } from 'pinia'
import { setAxiosBaseUrl } from './api/api'

setAxiosBaseUrl()

const pinia = createPinia()
const app = createApp(App)

app.use(pinia)
app.use(gcomponents)

// Stores
const jobSelection = stores.useJobSelection()
const corporaStore = stores.useCorpora()

// On pageload, retrieve values from query.
watch(() => router.currentRoute.value.query, () => {
  const q = router.currentRoute.value.query
  // Only set if q values are not null.
  if (q.corpus) corporaStore.activeUUID = router.currentRoute.value.query.corpus
  if (q.hypothesis) jobSelection.hypothesisJobId = router.currentRoute.value.query.hypothesis
  if (q.reference) jobSelection.referenceJobId = router.currentRoute.value.query.reference
})

// On change, update query.
watch(() => corporaStore.activeUUID, updateQuery)
watch(() => jobSelection.hypothesisJobId, updateQuery)
watch(() => jobSelection.referenceJobId, updateQuery)

function updateQuery() {
  let newQuery = {}
  // We do not want empty keys in the query, so we explicitly check each value before setting it
  if (corporaStore.activeUUID) newQuery["corpus"] = corporaStore.activeUUID
  if (jobSelection.hypothesisJobId) newQuery["hypothesis"] = jobSelection.hypothesisJobId
  if (jobSelection.referenceJobId) newQuery["reference"] = jobSelection.referenceJobId
  router.replace({
    query: newQuery,
    hash: router.currentRoute.value.hash // preserve the hash
  })
}

app.use(router)
app.mount('#app')
