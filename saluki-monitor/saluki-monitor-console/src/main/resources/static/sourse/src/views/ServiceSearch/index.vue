<template>
  <div class="row" id="service-search">
    <div class="col-md-12 col-lg-12 col-sm-12">
      <div class="panel panel-default">
        <div class="panel-heading">服务查询</div>
        <div class="panel-wrapper collapse in">
          <div class="panel-body">
            <form class="form-group search-form" role="search" @submit.prevent="search">
              <div class="input-group">
                <span class="input-group-btn">
                  <select class="form-control" v-model="type">
                    <option v-for="(item,index) in searchType" :value="index">{{item.label}}</option>
                  </select>
                </span>
                <input type="text" class="form-control" placeholder="请输入查询关键字，回车搜索" v-model="keywords">
                <span class="input-group-btn"><button type="submit" class="btn waves-effect waves-light btn-info"><i class="fa fa-search"></i></button></span>
              </div>
            </form>
          </div>
        </div>
      </div>
    </div>
    <div class="col-md-12 col-lg-12 col-sm-12">
      <div class="white-box">
        <table class="table table-striped" v-if="list.length>0">
          <thead>
            <tr>
              <th>名称</th>
              <th>版本</th>
              <th>所属应用</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in list">
              <td>{{ item.serviceName }}</td>
              <td>{{ item.version }}</td>
              <td>{{ item.application }}</td>
              <td>
                <router-link class="btn btn-sm btn-default" :to="goChart(item)">统计</router-link>
                <router-link class="btn btn-sm btn-default" :to="{path: '/service/detail/'+item.serviceName}">详情</router-link>
              </td>
            </tr>
          </tbody>
        </table>
        <template v-else>
          <p>查询结果为空</p>
        </template>
      </div>
    </div>
  </div>
</template>

<script type="text/babel">
  export default {
    data(){
      return {
        searchType: [
          {label: '服务名', value: '/api/service/fuzzyservice'},
          {label: '应用名', value: '/api/service/fuzzyapp'}
        ],
        list: [],
        keywords: '',
        type: 0
      }
    },
    mounted(){
      if(this.$route.query.type>=0 && this.$route.query.keywords){
        this.type = this.$route.query.type;
        this.keywords = this.$route.query.keywords;
        this.getList();
      }
    },
    methods:{
      search(){
        this.$router.push({path:'/service/search', query:{type:this.type,keywords:this.keywords}});
        this.getList();
      },
      getList(){
        let url = this.searchType[this.type].value;
        url += '?search='+this.keywords;
        this.$http.get(url).then((response)=>{
          this.list = response.body;
        }, (response)=>{
          console.info();
        });
      },
      goChart(item){
        const params = {
          service: item.serviceName,
          typelist: ''
        };
        const consumerHost = item.consumerHost.map((data)=>{
          return data.host+':'+data.httpPort;
        });
        const providerHost = item.providerHost.map((data)=>{
          return data.host+':'+data.rpcPort;
        });
        params.typelist = 'consumer='+consumerHost.toString()+'|'+'provider='+providerHost.toString();

        return '/service/chart/'+params.service+'/'+params.typelist;
      }
    }
  }
</script>

<style lang="stylus">
  #service-search
    .search-form
      select
        width 80px
        margin-right 1px
</style>
