<template>
  <div>

    <transition name="fade" mode="out-in">
      <!-- v-if instead of v-show such that elements inside a GModal can rely on onMounted()-->
      <div v-if="show" class="bg" @click.self="$emit('hide')">
        <GCard :showHelp="showHelp" :title="title" :class="`content ${small ? 'my-small' : ''}`" :noHelp="noHelp" :headless="headless">
          <template #title>
            <slot name="title"></slot>
          </template>
          <template #help>
            <slot name="help"></slot>
          </template>
          <slot>
            Somenone forgot to put the content
          </slot>
        </GCard>
        <div class="buttons">
          <GButton @click="$emit('hide')" red>Close</GButton>
          <slot name="buttons"></slot>
        </div>
      </div>
    </transition>

  </div>
</template>

<script lang='ts'>
import { defineComponent } from 'vue';

export default defineComponent({
  name: "GModal",
  props: {
    noHelp: { type: Boolean, default: false },
    headless: { type: Boolean, default: false },
    showHelp: { type: Boolean, default: true },
    show: { type: Boolean },
    small: { type: Boolean, default: false },
    title: { type: String }
  },
  mounted(): void {
    // smort
    const alias = this.handleGlobalKeyDown
    window.addEventListener('keyup', function (e) {
      alias(e)
    });
  },
  methods: {
    handleGlobalKeyDown(e: KeyboardEvent): void {
      if (e.key === "Escape") {
        this.$emit('hide')
      }
    }
  }
});
</script>

<style scoped lang="scss">
.bg {
  left: 0;
  top: 0;
  background-color: var(--int-very-light-grey);
  position: fixed;
  height: 100%;
  width: 100%;
  z-index: 1000;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  justify-content: center;
  align-items: center;
  padding: 2em;
  padding-bottom: 1em; // Override for bottom button
  gap: 1em;
}

.content {
  overflow: auto;
  border: 1px solid var(--int-light-grey);
  box-sizing: border-box;
  width: 100%;
  padding: 2em;
}

.content.my-small {
  // 'small' collides with a name from bootstrap(?). Hope te remove it in the future.
  width: fit-content;
}

.content>* {
  margin: 0px; // override default margin
}

.buttons {
  display: flex;
  justify-content: center;
  box-sizing: border-box;
  gap: 1em;
}

@media (max-width: 800px) or (max-height: 700px) {
  .bg {
    padding: 0;
    padding-bottom: 0.5em; // Override for bottom button
    gap: 0.5em;
  }
}
</style>