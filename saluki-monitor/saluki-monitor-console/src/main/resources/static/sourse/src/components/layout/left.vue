<template>
  <div class="navbar-default sidebar animated fadeInLeft" role="navigation">
    <div class="tool-name">{{host.nameEn}}<span>{{host.name}}</span></div>
    <div class="sidebar-nav navbar-collapse slimscrollsidebar">
      <ul class="nav" id="side-menu">
        <!--<li class="nav-small-cap m-t-10">-&#45;&#45; Main Menu</li>-->
        <li><router-link to="/group/relation" class="animated"><i class="ti-sharethis fa-fw"></i> 应用关系</router-link></li>
        <li><router-link to="/service/group" class="animated"><i class="ti-wallet fa-fw"></i> 应用列表</router-link></li>
        <li><router-link to="/service/search" class="animated"><i class="ti-search fa-fw"></i> 服务查询</router-link></li>
        <li><router-link to="/route/rule/list" class="animated"><i class="ti-search fa-fw"></i> 路由规则</router-link></li>
        <!-- <li><router-link to="/service/map" class="animated"><i class="ti-clipboard fa-fw"></i> 服务信息</router-link></li> -->
        <!-- <li><router-link to="/service/test" class="animated"><i class="ti-paint-bucket fa-fw"></i> 服务测试</router-link></li> -->
      </ul>
    </div>
  </div>
</template>

<script type="text/babel">
  export default{
    data(){
      return {
        list: [
          {
            host: 'saluki.dev.quancheng-ec.com',
            name: '开发环境',
            nameEn: 'Dev.'
          },
          {
            host: 'saluki.quancheng-ec.com',
            name: '线上环境',
            nameEn: 'Prod.'
          }
        ]
      }
    },
    computed:{
      host(){
        let host = window.location.host;
        // host = this.list[1].host;
        for(let i=0; i<this.list.length; i++){
          if(host.indexOf(this.list[i].host)>-1){
            return this.list[i]
          }
        }
        return this.list[0];
      }
    }
  }
</script>

<style lang="stylus">
  .tool-name
    margin-top -55px
    position absolute
    padding-left 10px
    width 220px
    text-align left
    border-bottom 1px solid #5a5f75
    color #707692
    height 50px
    line-height 50px
    font-size 30px
    span
      font-size 14px
</style>
