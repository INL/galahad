<template>
  <svg height="100" width="100" viewBox="0 0 100 100" 
    fill="var(--int-yellow-outline)"
    stroke="none"
    :class="cssclass">
    <path class="grail" d="M10 10 L90 10 Q90 40 65 40 L65 80 Q80 80 80 90 L20 90 Q20 80 35 80 L35 80 L35 40 Q10 40 10 10 Z"/>
    <ellipse class="wine" cx="60" cy="90" rx="30" ry="10" />
    ...
  </svg>
</template>

<script lang='ts'>
import {defineComponent} from 'vue';

export default defineComponent({
  name: "GSpinner",
  props: {
    error: { type: Boolean, default: false },
    large: { type: Boolean, default: false },
    medium: { type: Boolean, default: false },
    small: { type: Boolean, default: false },
    still: { type: Boolean, default: false }
  },
  computed: {
    cssclass() {
      let ret = ""
      ret += this.error ? 'error ' : ''
      ret += this.small ? 'small ' : ''
      ret += this.large ? 'large ' : ''
      ret += this.medium ? 'medium ' : ''
      ret += this.still ? 'still ' : ''
      return ret
    }
  }
});
</script>

<style scoped lang="scss">
svg {
  height: 40px;
  width: 40px;
  animation-name: spin;
  animation-duration: 4s;
  animation-timing-function:cubic-bezier(0.68, -0.55, 0.265, 1.55); //linear;
  animation-iteration-count: infinite;
}

svg .wine {
  visibility: hidden;
}

svg.error .grail {
  transform-origin: 45% 45%;
  transform: scale(0.6,0.6) rotate(150deg);
}

svg.error .wine {
  visibility: visible;
  fill: var(--int-red);
}

svg.small {
  height: 20px;
  width: 20px;
}

svg.medium {
  height: 40px;
  width: 40px;
}

svg.large {
  height: 100px;
  width: 100px;
}

svg.still {
  animation: none;
}

@keyframes spin {
  0% {
    transform: rotate(0deg);
  }
  25% {
    transform: rotate(90deg);
  }
  50% {
    transform: rotate(180deg);
  }
  75% {
    transform: rotate(270deg);
  }
  100% {
    transform: rotate(360deg);
  }
}

</style>