<template>
  <div class="layerpreview">
    <table v-if="layerNotEmpty">
      <thead>
        <tr>
          <th>Token</th>
          <th>Lemma</th>
          <th>PoS</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(term, i) in layer.terms.slice(cursor, cursor + pagesize)" :key="uid + 'term' + i">
          <td>{{ term.targets.map(x => x.literal).join('_') }}</td>
          <td>{{ term.lemma }}</td>
          <td>{{ term.pos }}</td>
        </tr>
      </tbody>
    </table>
    <p v-else><i>No layer found</i></p>
    <div v-if="layerNotEmpty">
      <GButton plain :disabled="cursor < pagesize" @click="cursor -= pagesize" title="Previous">
        <i class="fa fa-arrow-left"></i>
      </GButton>
      {{ cursor + 1 }} - {{ cursor + pagesize }}
      <GButton plain :disabled="cursor >= layer.terms.length - pagesize" @click="cursor += pagesize" title="Next">
        <i class="fa fa-arrow-right"></i>
      </GButton>
    </div>
  </div>
</template>

<script lang='ts'>
import { defineComponent } from 'vue'
import { LayerPreview } from '@/types/jobs'
// Component dependencies.
import { GButton } from '@/components'

export default defineComponent({
  name: "LayerViewer",
  props: {
    layer: {
      type: Object as () => LayerPreview
    },
    uid: {
      type: String
    }
  },
  computed: {
    layerNotEmpty() { return this.layer?.terms?.length > 0 }
  },
  data() {
    return {
      cursor: 0,
      pagesize: 10,
    }
  },
  watch: {
    layer() {
      this.cursor = 0
    }
  }
});
</script>

<style scoped>
table {
  display: block;
  border-collapse: collapse;
  margin: 0;
  margin-top: 5px;
  margin-bottom: 10px;
  padding: 0;

  caption {
    font-size: 1.5em;
    margin: .5em 0 .75em;
  }

  tr {
    border: 1px solid #ddd;

    &:nth-child(even) {
      background: #fff;
    }

    &:nth-child(odd) {
      background: var(--int-very-light-grey);
    }
  }

  thead {
    >tr {
      background-color: var(--int-theme) !important;
    }
  }

  th {
    padding: .6em;
    text-align: center;
    font-size: .85em;
    letter-spacing: .1em;
    text-transform: uppercase;
  }

  td {
    padding: .2em 1em;
    text-align: center;
    min-width: 60px;
  }

  overflow-x: auto;
}
</style>