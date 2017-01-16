<template>
  <div id="service-chart">
    <div class="panel panel-default">
      <div class="panel-heading">
        <div class="row">
          <div class="col-md-2">发布者端</div>
          <div class="col-md-10">
            <div>
              <template v-for="(item,index) in dateType">
                <a class="btn btn-default btn-sm"
                  :class="{'active':serviceDateType==index}"
                  @click="serviceDateType=index">
                  {{ item.label }}
                </a>
              </template>
            </div>
          </div>
        </div>
      </div>
      <div class="panel-wrapper collapse in">
        <div class="panel-body">
          <p v-show="serviceData==null">无服务调用</p>
          <div class="row" v-show="serviceData!=null">
            <div class="col-md-12">
              <div class="white-box service-chart" style="opacity: 0.8">
                <div ref="serviceChart1"></div>
              </div>
            </div>
            <div class="col-md-12"><hr></div>
            <div class="col-md-12">
              <div class="white-box service-chart" style="opacity: 0.8">
                <div ref="serviceChart2"></div>
              </div>
            </div>
            <div class="col-md-12"><hr></div>
            <div class="col-md-12">
              <div class="white-box service-chart" style="opacity: 0.8">
                <div ref="serviceChart3"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="panel panel-default">
      <div class="panel-heading">
        <div class="row">
          <div class="col-md-2">消费者端</div>
          <div class="col-md-10">
            <div>
              <template v-for="(item,index) in dateType">
                <a class="btn btn-default btn-sm"
                  :class="{'active':clientDateType==index}"
                  @click="clientDateType=index">
                  {{ item.label }}
                <a>
              </template>
            </div>
          </div>
        </div>
      </div>
      <div class="panel-wrapper collapse in">
        <div class="panel-body">
          <p v-show="clientData==null">无服务调用</p>
          <div class="row" v-show="clientData!=null">
            <div class="col-md-12">
              <div class="white-box service-chart" style="opacity: 0.8">
                <div ref="clientChart1"></div>
              </div>
            </div>
            <div class="col-md-12"><hr></div>
            <div class="col-md-12">
              <div class="white-box service-chart" style="opacity: 0.8">
                <div ref="clientChart2"></div>
              </div>
            </div>
            <div class="col-md-12"><hr></div>
            <div class="col-md-12">
              <div class="white-box service-chart" style="opacity: 0.8">
                <div ref="clientChart3"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <!---->
  </div>
</template>

<script>
  import echarts from 'echarts';
  import moment from 'moment';
  export default {
    data(){
      return{
        serviceData: {},
        serviceDateType: 0,
        clientData: {},
        clientDateType: 0,
        dateType: [
          {label: '今天', limit: [0,0], type:'hour'},
          {label: '昨天', limit: [1,1], type:'hour'},
          {label: '最近7天', limit: [0,7], type:'day'},
          {label: '最近30天', limit: [0,30], type:'day'}
        ]
      }
    },
    mounted(){
      console.info(this.datetimeLimit(0,7));
      console.info(this.datetimeLimit(1,1));

      this.$nextTick(function () {

        // 保证 this.$el 已经插入文档
        // const data = {"192.168.2.14:8181":[{"invokeTime":1479958674,"sumconcurrent":562,"sumelapsed":930,"sumsuccess":100,"sumfailure":0,"suminput":1200,"tps":60430,"kbps":604.3,"elapsed":9.3},{"invokeTime":1479958822,"sumconcurrent":409,"sumelapsed":499,"sumsuccess":77,"sumfailure":0,"suminput":924,"tps":63120,"kbps":631.2,"elapsed":6.48},{"invokeTime":1479958774,"sumconcurrent":552,"sumelapsed":874,"sumsuccess":100,"sumfailure":0,"suminput":1200,"tps":63160,"kbps":631.6,"elapsed":8.74},{"invokeTime":1479958679,"sumconcurrent":565,"sumelapsed":1006,"sumsuccess":100,"sumfailure":0,"suminput":1200,"tps":56160,"kbps":561.6,"elapsed":10.06},{"invokeTime":1479958664,"sumconcurrent":564,"sumelapsed":1195,"sumsuccess":100,"sumfailure":0,"suminput":1200,"tps":47200,"kbps":472,"elapsed":11.95},{"invokeTime":1479958823,"sumconcurrent":129,"sumelapsed":165,"sumsuccess":23,"sumfailure":0,"suminput":276,"tps":17990,"kbps":179.9,"elapsed":7.17},{"invokeTime":1479958828,"sumconcurrent":513,"sumelapsed":958,"sumsuccess":100,"sumfailure":0,"suminput":1200,"tps":53550,"kbps":535.5,"elapsed":9.58},{"invokeTime":1479958783,"sumconcurrent":534,"sumelapsed":785,"sumsuccess":100,"sumfailure":0,"suminput":1200,"tps":68030,"kbps":680.3,"elapsed":7.85}]}
        // const charts = getChartsOption(data);
        // console.info(charts);
        // charts.map((item, index)=>{
        //   service[index] = echarts.init(this.$refs['serviceChart'+(index+1)]);
        //   service[index].setOption(item);
        // });
        // return;

        //invokeDateTo
        //invokeDateFrom

        //发布者端
        this.getServcieChart();
        //消费者端
        this.getClientChart();
      });
    },
    watch:{
      serviceDateType(){
        this.getServcieChart()
      },
      clientDateType(){
        this.getClientChart()
      }
    },
    methods:{
      datetimeLimit(startNum,endNum){
        const day1 = moment().subtract(endNum,'day').format('YYYY-MM-DD 00:00:00');
        const day2 = startNum==0 ? moment().format('YYYY-MM-DD HH:mm:ss') : moment().subtract(startNum,'day').format('YYYY-MM-DD 23:59:59');
        return [day1,day2];
      },
      // hostGroupName:consumer/provider,
      // dateType查询日期类型
      getChartData(hostGroupName, dateTypeInfo){
        console.info('========',dateTypeInfo);
        const groupMap = {
          consumer: 'client',
          provider: 'service'
        }
        const url     = '/api/monitor/statistics?';   //按IP查询
        const urlSum  = '/api/monitor/sumstatistics?';//统计所有IP
        const hostGroup = {};
        this.$route.params.typelist.split('|').map((item, index)=>{
          const param = item.split('=');
          hostGroup[param[0]] = param[1];
        });

        const invokeDate = this.datetimeLimit(dateTypeInfo.limit[0],dateTypeInfo.limit[1]);
        const chartPrefix = `${groupMap[hostGroupName]}Chart`;
        const dateFormat = dateTypeInfo.type=='day' ? 'YYYY-MM-DD' : 'YYYY-MM-DD HH:mm';
        console.info(dateFormat);
        const requestParams = `service=${this.$route.params.service}`+
                              `&type=${hostGroupName}`+
                              `&ips=${hostGroup[hostGroupName]}`+
                              `&invokeDateFrom=${invokeDate[0]}`+
                              `&invokeDateTo=${invokeDate[1]}`+
                              `&datatype=${dateTypeInfo.type}`;
        const sumRequestParams = `service=${this.$route.params.service}`+
                              `&type=${hostGroupName}`+
                              `&invokeDateFrom=${invokeDate[0]}`+
                              `&invokeDateTo=${invokeDate[1]}`+
                              `&datatype=${dateTypeInfo.type}`;
        // init chart
        const charts = chartsConf();
        const chartsInstance = [];
        charts.map((item, index)=>{
          chartsInstance[index] = echarts.init(this.$refs[`${chartPrefix}${index+1}`]);
        });

        // get chart data
        this.$http.get(`${url}${requestParams}`).then((response)=>{
          this.$http.get(`${urlSum}${sumRequestParams}`).then((avgResponse)=>{
            const allData = response.body;
            const avgData = {SUM: avgResponse.body};
            const data = Object.assign(avgData,allData);
            const keys = Object.keys(response.body);
            const len = keys.length;
            if(len==0){
              this[`${groupMap[hostGroupName]}Data`] = null;
              return false;
            }
            const chartsOption = getChartsOption(data, dateFormat);
            chartsOption.map((item, index)=>{
              chartsInstance[index].setOption(item);
            });
          });
          // //data null
          // const keys = Object.keys(response.body);
          // const len = keys.length;
          // if(len==0 || response.body[keys[0]].length==0){
          //   this[`${groupMap[hostGroupName]}Data`] = null;
          //   return false;
          // }
          // //data.len>1 , get sum
          // if(len==1){
          //   const chartsOption = getChartsOption(response.body, dateFormat);
          //   chartsOption.map((item, index)=>{
          //     chartsInstance[index].setOption(item);
          //   });
          // }else if(len>1){
          //   // sum search
          //   this.$http.get(`${urlSum}${requestParams}`).then((avgResponse)=>{
          //     const allData = response.body;
          //     const avgData = {SUM: avgResponse.body};
          //     const data = Object.assign(avgData,allData);
          //     const chartsOption = getChartsOption(data, dateFormat);
          //     chartsOption.map((item, index)=>{
          //       chartsInstance[index].setOption(item);
          //     });
          //   });
          // }
        }, (response)=>{
          this[`${groupMap[hostGroupName]}Data`] = null;
        });
      },
      getServcieChart(){
        this.getChartData('provider', this.dateType[this.serviceDateType]);
      },
      getClientChart(){
        this.getChartData('consumer', this.dateType[this.serviceDateType]);
      }
    }
  }

  function chartsConf(){
    return [
      {key:'tps', title:'TPS', series:[], unit:''},
      {key:'sumElapsed', title:'平均响应时间', series:[], unit:'ms'},
      {key:'kbps', title:'平均网络传输速度', series:[], unit:'kb'}
    ]
  }

  function getChartsOption(data, dateFormat){
    //data: host list
    //tag: legend.data[0]==series[0].name; 显示legend(图例组件)
    const legendData = [];
    const charts = chartsConf();
    for(const prop in data){
      // prop;                     --ip:port
      // data[prop]                --list
      // data[prop][0].invokeTime; ---时间戳(m)
      // data[prop][0].tps;        ---tps
      // data[prop][0].kbps;       ---响应时间
      // data[prop][0].elapsed;    ---平均网络传输速度
      legendData.push(prop);

      //线
      const lines = {};
      charts.map((type, typeIndex)=>{
        lines[type.key] = []
      });
      data[prop].map((point, pointIndex)=>{
        // const date = new Date();
        // const datetime = [date.getFullYear(), date.getMonth()+1, date.getDay()].join('/') + ' ' +[date.getHours(), date.getMinutes(), date.getSeconds()].join(':');
        const datetime = moment(point.invokeDate).format(dateFormat);
        charts.map((type, typeIndex)=>{
          lines[type.key].push(chartPointFormat(datetime, point[type.key]));
        })
      })

      //chart
      console.info('charts go',lines);
      charts.map((item, index)=>{
        console.info('shuzu',lines[item.key]);
        charts[index].series.push({
          name: prop,
          type: 'line',
          // stack: '总量',
          data: lines[item.key]
        })
      })
      console.info('getChartsOption() charts',charts);
    };

    console.info('getChartsOption() legendData',legendData)

    return charts.map((item, index)=>{
      return getDefaultChartOption(item.title, legendData, item.series, dateFormat, item.unit)
    })
  }

  function chartPointFormat(datetime, number){
    return {
      name: datetime,
      value: [
        datetime,
        number
      ]
    }
  };

  //default option
  //y:value, x:datetime
  function getDefaultChartOption(title, legendData, series, dateFormat, yName){
    // xName = '小时';
    // yName = 'ms';
    let xName = '';
    //
    let xAxisInterval = 'auto';
    switch(dateFormat){
      case 'YYYY-MM-DD HH:mm':
        xAxisInterval = 3600*1000;
        xName = '24小时';
        break;
      case 'YYYY-MM-DD':
        xAxisInterval = 24*3600*1000;
        xName = '日期';
        break;
    }
    //
    const dataZoom = {start:0, end:100};
    if(xName=='日期'){
      const x = 7; //日期默认 刻度数<=7
      let len = Math.floor(100/x);
      let num = Math.floor(series[0].data.length/x);
      if(num>1){
        dataZoom.start = len * num
      }
    }
    //
    function xAxisFormatter(value, index){
      if(dateFormat=='YYYY-MM-DD HH:mm'){
        return moment(value).format('H')+'h';
      }else{
        return moment(value).format('MM-DD');
      }
    }
    //
    function yAxisFormatter(value, index){
      if(yName){
        return value+yName;
      }else{
        return value;
      }
    }

    const option = {
      title: {
        textStyle: {fontSize:14},
        text: title
      },
      tooltip: {
        trigger: 'axis',
        axisPointer: {
          axis: 'x'
        }
      },
      xAxis: {
        name: xName,
        type: 'time',
        splitLine: {
            show: false
        },
        interval: xAxisInterval,
        axisLabel:{
          formatter: xAxisFormatter
        }
      },
      yAxis: {
        name: yName,
        type: 'value',
        boundaryGap: [0, '100%'],
        splitLine: {
            show: false
        },
        max: 'dataMax',
        nameTextStyle: {
          fontSize: 12,
          fontStyle: 'italic'
        },
        // axisLabel:{
        //   formatter: yAxisFormatter
        // }
      },
      legend: {
        show: true,
        data: legendData
      },
      series: series,
      dataZoom: [
        {
          show: true,
          realtime: true,
          start: dataZoom.start,
          end: dataZoom.end,
          xAxisIndex: [0]
        }
      ],
      grid: [{
        left: 60,
        right: 50,
        height: '50%'
      }]
    }
    return option;
  }


</script>

<style lang="stylus">
  #service-chart
    .service-chart
      height 300px
      padding 5px
      &>div
        height 100%
    .btn-sm
      padding 0px 10px
</style>
