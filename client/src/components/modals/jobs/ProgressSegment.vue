<template>
  <div
    :style="`display: inline-block; text-align: center; border-top: 1px solid var(--black); width: ${Math.floor(100 * value / total)}%; background-color: ${color}; color: ${textcolor}`">
    {{ value > 0 ? labelText : '' }}
  </div>
</template>

<script lang='ts'>
import { defineComponent } from 'vue';

export default defineComponent({
  name: "ProgressSegment",
  props: {
    color: { type: String, default: 'var(--int-very-light-grey)' },
    textcolor: { type: String, default: 'var(--black)' },
    label: { type: String },
    value: { type: Number, default: 0 },
    total: { type: Number, default: 0 },
  },
  computed: {
    labelText(): string {
      return `${this.label}: ${this.value} ${this.value === 1 ? 'document' : 'documents'}`
    },
  },
});
</script>

<style scoped lang="scss">
div {
  white-space: nowrap;
  overflow: hidden;
  transition: width 1s;
}
</style>