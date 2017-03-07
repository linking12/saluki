<template>
  <div class="row" id="service-detail">
    <div class="col-md-12 col-lg-12 col-sm-12">
      <div class="panel panel-default">
        <div class="panel-heading">基本信息</div>
        <div class="panel-wrapper collapse in">
          <div class="panel-body">
            <table class="table table-condensed table-bordered">
              <tbody>
                <tr>
                  <td width='100' align="center">服务名</td>
                  <td>{{ service.serviceName }}</td>
                </tr>
                <tr>
                  <td align="center">应用名</td>
                  <td>{{ service.application }}</td>
                </tr>
                <tr>
                  <td align="center">负责人</td>
                  <td>
                    <!-- <span><small><i class="ti-user fa-fw"></i></small>小明</span>&nbsp; -->
                    <!-- <span><small><i class="ti-user fa-fw"></i></small>小花</span> -->
                    <span><small><i class="ti-user fa-fw"></i> - </small></span>
                  </td>
                </tr>
                <tr>
                  <td align="center">操作</td>
                  <td><router-link class="btn btn-sm btn-default" :to="goChart(service)">统计</router-link></td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
      <!---->
      <div class="panel panel-default">
        <div class="panel-heading">服务器列表</div>
        <div class="panel-wrapper collapse in">
          <div class="panel-body">
            <tabs :navList="ipType" navClass="customtab">
              <tabs-content v-for="(item,itemType) in {'provider':service.providerHost,'consumer':service.consumerHost}">
                <table class="table text-center table-bordered">
                    <thead>
                      <tr>
                        <td>IP </td>
                        <td>HTTP端口</td>
                        <td>RPC端口</td>
                        <td>超时时间（ms）</td>
                        <td>序列化方式</td>
                        <td>操作</td>
                      </tr>
                    </thead>
                    <tbody v-for="(h,hIndex) in item">
                      <tr>
                        <td>{{ h.host }}</td>
                        <td>{{ h.httpPort }}</td>
                        <td>{{ h.rpcPort }}</td>
                        <td>5000</td>
                        <td>protobuf</td>
                        <td>
                          <button class="btn btn-sm btn-default" @click="getHostInfo(itemType, hIndex)">查看系统</button>
                          <template v-if="itemType!='consumer'">
                            <router-link class="btn btn-sm btn-default" :to="{path:'/service/test/'+h.host+':'+h.httpPort+'/'+service.serviceName}">去测试</button>
                          </template>
                          <!-- <button class="btn btn-sm btn-default">复制IP端口</button> -->
                        </td>
                      </tr>
                      <tr>
                        <td colspan="6" class="host-info">
                          <table v-show="hostInfoList[h.host+':'+h.httpPort]">
                            <tr>
                              <td colspan="3" align="left">
                                <span style="color:#505058;font-weight:bolder">>> {{ h.host+':'+h.httpPort }}</span>
                              </td>
                            </tr>
                            <tr v-for="item in hostInfoList[h.host+':'+h.httpPort]">
                              <td align="right">{{ item[0] }}</td>
                              <td>:</td>
                              <td align="left">{{ item[1] }}</td>
                            </tr>
                          </table>
                        </td>
                      </tr>
                    </tbody>
                  </table>
              </tebs-content>
            </tabs>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script type="text/babel">
  import Tabs from '../../components/Tabs.vue';
  import TabsContent from '../../components/TabsContent.vue';
  export default {
    components:{
      Tabs,
      TabsContent
    },
    data(){
      return{
        ipType:['服务发布者','服务消费者'],
        service: {},
        hostInfoList: {},
        title: '123123'
      }
    },
    mounted(){
      let url = '/api/service/accurateservice';
      url += '?search='+this.$route.params.service;
      this.$http.get(url).then((response)=>{
        this.service = response.body[0] ? response.body[0] : {};
      }, (response)=>{
        this.service = {};
        // console.info(response);
      });
    },
    methods:{
      getHostInfo(type, hostIndex){
        const typeMap = {
          provider: 'providerHost',
          consumer: 'consumerHost'
        }
        const hostInfo = this.service[typeMap[type]][hostIndex];
        const ipPort = hostInfo.host+':'+hostInfo.httpPort;
        let url = '/api/monitor/system?ipPort='+ipPort;
        this.$http.get(url).then((response)=>{
          // this.hostInfoList[ipPort] = response.body.rows;
          this.$set(this.hostInfoList, ipPort, response.body.rows);
        }, (response)=>{
          console.info(response);
          const data = [];
          for(const prop in response.body){
            data.push([prop,response.body[prop]])
          }
          this.$set(this.hostInfoList, ipPort, data);
        });
      },
      goChart(item){
        if(!item || !item.serviceName){
          return ''
        }
        const params = {
          service: item.serviceName,
          typelist: ''
        };
        const consumerHost = !item.consumerHost ? [] : item.consumerHost.map((data)=>{
          return data.host+':'+data.httpPort;
        });
        const providerHost = !item.providerHost ? [] : item.providerHost.map((data)=>{
          return data.host+':'+data.rpcPort;
        });
        params.typelist = 'consumer='+consumerHost.toString()+'|'+'provider='+providerHost.toString();

        return '/service/chart/'+params.service+'/'+params.typelist;
      }
    }
  }
</script>

<style lang="stylus">
  #service-detail
    .host-info
      font-size 12px
      table
        width 100%
        border 5px solid #efefef
        /*box-shadow 0px 0px 0px 2px #000 inset*/
        background #292935
        padding 5px 0
        color #abb2be
        tr
          td
            padding 2px
            &:last-of-type
              padding-left 5px
</style>
