<template>
  <textarea>
    <slot></slot>
  </textarea>
</template>

<script>
export default {
  props: {
    autoHeight: {
      type: Boolean,
      default: true
    }
  }
}
</script>

<style lang="stylus">
</style>
