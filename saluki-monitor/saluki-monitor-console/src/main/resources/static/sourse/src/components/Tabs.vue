<template>
  <div class="tabs">
    <ul class="nav nav-tabs" :class="navClass">
      <template v-for="(item, index) in navList">
        <li :class="{active: index===active}">
          <a @click="select(index)">{{ item }}</a>
        </li>
      </template>
    </ul>
    <div class="tab-content" ref="tabContent">
      <slot></slot>
    </div>
  </div>
</template>

<script type="text/babel">
  export default {
    props:{
      navClass:{
        type: String,
        default: ''
      },
      navList:{
        type: Array,
        default: []
      }
    },
    data(){
      return {
        active: 0
      }
    },
    mounted(){
      this.select(this.active)
    },
    methods:{
      select(num){
        if(!this.$refs.tabContent.children[num] || !this.$refs.tabContent.children[this.active]){
          console.info('warn');
          debugger;
          return false;
        }
        this.$refs.tabContent.children[this.active].classList.remove('active');
        this.$refs.tabContent.children[num].classList.add('active');
        this.active = num;
      }
    }
  }
</script>

<style lang="stylus">
  .tabs
    ul.nav
      li
        a
          cursor pointer
</style>
