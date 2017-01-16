<template>
  <div class="row" id="service-group">
    <div class="col-md-4 app-list">
      <div class="panel panel-default">
        <div class="panel-heading clearfix">
          <input class="form-control" placeholder="filter" v-model="appKeywords">
          <small class="app-count pull-right">{{appFilterNum}}/{{applicationList.length}}</small>
        </div>
        <div class="panel-wrapper collapse in">
          <div class="panel-body">
            <div class="list-group">
              <template v-for="(item,index) in appList" v-if="item!=undefined">
                <a class="list-group-item clearfix"
                  :class="{active: index==currentApp}"
                  @click="selectApp(index)">

                  <span class="pull-left">{{ item.appName }}</span>
                  <span class="pull-right" v-if="item.statusCount.failing>0"><small class="text-danger">{{ item.statusCount.failing }} failing</small></span>
                  <span class="pull-right" v-else><small class="text-defaul">passing</small></span>
                </a>
              </template>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="col-md-8 service-list anim fadeInLeft" v-if="currentApp!=null">
      <div class="panel panel-default">
        <div class="panel-wrapper collapse in">
          <div class="panel-body">
            <p>{{ applicationList[currentApp].appName }}</p>
            <hr>
            <table class="table">
              <tbody>
                <tr v-for="(item,index) in applicationList[currentApp].services">
                  <td>
                    <div class="row">
                      <div class="col-md-12">
                        <p>
                          <router-link :to="{path:'/service/detail/'+item.serviceName}">
                            <!-- com.quancheng.zeus.service.AccountService-->
                            {{ item.serviceName }} <span class="label label-default">{{ item.version }}</span>
                          </router-link>
                        </p>
                        <p v-for="host in item.providerHost" class="service-host">
                          <span>{{ host.url }}</span>
                          <span class="service-host-tag" :class="statusClass[host.status]">
                            {{ host.status }}
                          </span>
                        </p>
                      </div>
                    </div>
                  </td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script type="text/babel">
  export default {
    data(){
      return{
        currentApp: null,
        appLocalList: [
          {appName: 'Default', failing: 3},
          {appName: 'Robert', failing: 0},
          {appName: 'Brondon', failing: 0},
          {appName: 'dev', failing: 0},
          {appName: 'dsk', failing: 23},
          {appName: 'hedong', failing: 2},
          {appName: 'lizhuliang', failing: 0},
          {appName: 'sunyun', failing: 12}
        ],
        applicationList: [],
        appKeywords: '',
        statusClass: {
          'passing': 'text-success',
          'failing': 'text-danger',
          'critical': 'text-warning'
        }
      }
    },
    mounted(){
      this.$http.get('/api/application/list').then((response)=>{ //http://10.110.0.13:9999
        this.applicationList = this.serviceStatusFormat(response.body);
        if(this.$route.query.appKey>=0){
          this.selectApp(this.$route.query.appKey)
        }
        // this.applicationList.push(this.applicationList[0]);
        // this.applicationList.push(this.applicationList[0]);
        // this.applicationList.push(this.applicationList[0]);
        // this.applicationList.push(this.applicationList[0]);
        // this.applicationList.push(this.applicationList[0]);
      }, (response)=>{
        debugger;
      });
    },
    computed:{
      appList(){
        if(this.appKeywords==''){
          return this.applicationList
        }
        const list = this.applicationList.map((app,appIndex)=>{
          if(appIndex==this.currentApp){
            return app
          }else if(app.appName.toLowerCase().indexOf(this.appKeywords.toLowerCase())!=-1){
            return app
          }
          return null;
        })
        return list;
      },
      appFilterNum(){
        let num = 0;
        this.appList.map((item)=>{
          if(item!=null){
            num++
          }
        })
        return num;
      }
    },
    methods:{
      selectApp(index){
        this.currentApp = index;
        this.$router.push({path:'/service/group', query:{appKey:index}});
      },
      filterAppName(){

      },
      serviceStatusFormat(apps){
        /**
        status: passing,failing,critical
        ---
        app:[
          service:[
            host
          ]
        ]
        ---
        [status roles]:
        all  host passing : service passing
        all  host failing : service failing
        some host failing : service critical (passing)
        all  service passing : app passing
        has  servcie failing : app failing number
        */
        apps.map((app, appIndex)=>{
          app.statusCount = {
            passing: 0,
            failing: 0,
            critical: 0
          }
          app.services.map((service, serviceIndex)=>{
            service.statusCount = {
              passing: 0,
              failing: 0
            }
            service.statusCount.passing += service.providerHost.length==0 ? 1 : 0;
            service.providerHost.map((host, hostIndex)=>{
              switch(host.status){
                case 'passing':
                  service.statusCount.passing++;
                  break;
                case 'failing':
                  service.statusCount.failing++;
                  break;
              }
            });
            if(service.statusCount.failing==0){
              service.status = 'passing';
            }else if(service.statusCount.failing>0 && service.statusCount.passing==0){
              service.status = 'failing';
            }else if(service.statusCount.failing>0 && service.statusCount.passing==0){
              service.status = 'critical'
            };
            app.statusCount[service.status]++;
          });
        })
        return apps;
      }
    }
  }
</script>

<style lang="stylus">
  #service-group
    .app-count
      margin -27px 5px 0 0
      opacity 0.4
      font-weight 100
      font-size 10px
      padding 2px 5px
      border-radius 10px
      background #eee
    .app-list
      .list-group
        .list-group-item
          margin-bottom 5px
          cursor pointer
          &.active
            font-weight bolder
            border 1px solid #ccc
            background #fff//#f5f5f5
            color #000
            border-radius 5px 5px 5px 5px
            /*border-radius 4px 150px 3px 4px*/
            /*border-left 20px solid #ff*/
            border-right 25px solid #fff//#EDF1F5
            margin-left -5px
            margin-right -35px
            box-shadow -1px 0px 0px 0px #ccc inset, 0px 0px 0px 1px #fff //, 1px 2px 0px 2px #eee

    .service-list
      .table
        tr:first-child
          td
            border-top 0
      .service-host
        font-size 12px
        border-left 3px solid #eee
        padding-left 5px
        margin-bottom 3px
        font-family Helvetica
        &-tag
          border-left 3px solid #eee
          padding 0 5px
          margin-left 3px
        :hover
          background #eee
    .label
      border-radius 2px
      padding 2px 5px 0px
</style>
