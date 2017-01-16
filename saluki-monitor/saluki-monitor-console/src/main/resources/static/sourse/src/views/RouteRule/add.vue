<template>
  <div class="row" id="service-search">
    <div class="col-md-12 col-lg-12 col-sm-12">
      <div class="panel panel-default">
        <div class="panel-heading">
          添加路由规则
        </div>
        <div class="panel-wrapper collapse in">
          <div class="panel-body">
            <form class="">
              <table class="table table-bordered form-group-sm">
                <tbody>
                  <tr>
                    <td class="active">路由名称*：</td>
                    <td colspan="2"><input class="form-control"></td>
                    <td>可使用中文</td>
                  </tr>
                  <tr>
                    <td class="active">服务名*：</td>
                    <td colspan="2"><input class="form-control"></td>
                    <td></td>
                  </tr>
                  <tr>
                    <td class="active">方法名</td>
                    <td colspan="2"><input class="form-control"></td>
                    <td></td>
                  </tr>
                  <!---->
                  <tr class="active">
                    <td>匹配条件</td>
                    <td>匹配</td>
                    <td>不匹配</td>
                    <td>当消费者满足匹配条件时，使用当前规则进行过滤</td>
                  </tr>
                  <tr>
                    <td class="active">消费者IP地址：</td>
                    <td><input class="form-control"></td>
                    <td><input class="form-control"></td>
                    <td>多个用<code>,</code>逗号分割，以<code>*</code>号结尾表示通配地址段</td>
                  </tr>
                  <tr>
                    <td class="active">消费者IP地址：</td>
                    <td><input class="form-control"></td>
                    <td><input class="form-control"></td>
                    <td>多个用<code>,</code>逗号分割</td>
                  </tr>
                  <!---->
                  <tr class="active">
                    <td>过滤条件</td>
                    <td>匹配</td>
                    <td>不匹配</td>
                    <td>当消费者满足匹配条件时，使用当前规则进行过滤</td>
                  </tr>
                  <tr>
                    <td class="active">提供者IP地址：</td>
                    <td><input class="form-control"></td>
                    <td><input class="form-control"></td>
                    <td>多个用<code>,</code>逗号分割，以<code>*</code>号结尾表示通配地址段</td>
                  </tr>
                  <tr>
                    <td colspan="4"></td>
                  </tr>
                  <tr class="active">
                    <td></td>
                    <td colspan="2">
                      <textarea-pro class="form-control" rows="4" disabled>123123</textarea-pro>
                    </td>
                    <td>组合好的规则</td>
                  </tr>
                  <tr class="active">
                    <td></td>
                    <td colspan="2">
                      <button class="btn btn-lg btn-info btn-block">保存</button>
                    </td>
                    <td></td>
                  </tr>
                </tbody>
              </table>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script type="text/babel">
  import TextareaPro from '../../components/TextareaPro.vue'
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
    },
    components:{
      TextareaPro
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
