<template>
  <div id="loading" class="animated fadeOutLeft">
    <div class="num">42</div>
  </div>
</template>

<script type="text/babel">
  export default {
    data(){
      return {
        first: true
      }
    },
    mounted(){
      setTimeout(()=>{
        this.first = false
      },1000)
    }
  }
</script>

<style lang="stylus">
  #loading
    position absolute
    z-index 100
    top 0
    left 0
    width 100%
    height 100%
    background #edf1f5
    .num
      position absolute
      left 50%
      top 50%
      font-size 200px
      color rgba(255,255,255,0)
      font-weight bolder
</style>
