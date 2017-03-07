<template>
  <div id="service-chart">
    <div class="panel panel-default">
      <div class="panel-heading">消费者端</div>
      <div class="panel-wrapper collapse in">
        <div class="panel-body">
          <div class="row">
            <div class="col-md-10">
              <div class="white-box service-chart" style="opacity: 0.8">
                <div ref="serviceChart1"></div>
              </div>
            </div>
            <div class="col-md-12"><hr></div>
            <div class="col-md-10">
              <div class="white-box service-chart" style="opacity: 0.8">
                <div ref="serviceChart2"></div>
              </div>
            </div>
            <div class="col-md-12"><hr></div>
            <div class="col-md-10">
              <div class="white-box service-chart" style="opacity: 0.8">
                <div ref="serviceChart3"></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="panel panel-default">
      <div class="panel-heading">发布者端</div>
      <div class="panel-wrapper collapse in">
        <div class="panel-body">
          <div class="row">
            <div class="col-md-10">
              <div class="white-box service-chart" style="opacity: 0.8">
                <div ref="clientChart1"></div>
              </div>
            </div>
            <div class="col-md-12"><hr></div>
            <div class="col-md-10">
              <div class="white-box service-chart" style="opacity: 0.8">
                <div ref="clientChart2"></div>
              </div>
            </div>
            <div class="col-md-12"><hr></div>
            <div class="col-md-10">
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

<script type="text/babel">
  import echarts from 'echarts';
  // import 'echarts/lib/component/title';
  // import 'echarts/lib/component/tooltip';
  // import 'echarts/lib/component/legend';
  export default {
    mounted(){
      const option = {
        // title: {
        //     text: '动态数据 + 时间坐标轴'
        // },
        legend: {
            show: true,
            data:['192.168.0.1:8080','192.168.0.2:8080']
        },
        tooltip: {
            trigger: 'axis'
        },
        xAxis: {
            type: 'time',
            splitLine: {
                show: true
            }
        },
        yAxis: {
            type: 'value',
            max: 'dataMax',
            boundaryGap: [0, '100%'],
            splitLine: {
                show: false
            },
            nameTextStyle: {
              fontSize: 12,
              fontStyle: 'italic'
            }
        },
        series: [{
            name: '192.168.0.1:8080',
            type: 'line',
            stack: '总量',
            showSymbol: false,
            hoverAnimation: false,
            // data: data
        },
        {
            name: '192.168.0.2:8080',
            type: 'line',
            stack: '总量',
            showSymbol: false,
            hoverAnimation: false,
            // data: data
        }],
        dataZoom: [
            {
                show: true,
                realtime: true,
                start: 30,
                end: 70,
                xAxisIndex: [0]
            }
        ],
        grid: [{
            left: 50,
            right: 50,
            height: '50%'
        }]
    };
      // serviceChart1;
      // serviceChart2;
      // clientChart1;
      // clientChart2;
      // lossChart1;
      this.$nextTick(function () {
        // 保证 this.$el 已经插入文档
        const serviceChart1 = echarts.init(this.$refs.serviceChart1);
        const serviceChart2 = echarts.init(this.$refs.serviceChart2);
        const serviceChart3 = echarts.init(this.$refs.serviceChart3);
        const clientChart1 = echarts.init(this.$refs.clientChart1);
        const clientChart2 = echarts.init(this.$refs.clientChart2);
        const clientChart3 = echarts.init(this.$refs.clientChart3);
        console.info(json);

        const times = [];
        const chartData = json2.map((item)=>{
          times.push(item.invokeDate);
          const date = new Date(item.invokeDate);
          return {
            name: 'item.invokeDate',
            value: [
              [date.getFullYear(), date.getMonth()+1, date.getDay()].join('/') + ' ' +
              [date.getHours(), date.getMinutes(), date.getSeconds()].join(':'),
              item.tps
            ]
          }
        });
        const chartData2 = json3.map((item)=>{
          times.push(item.invokeDate);
          const date = new Date(item.invokeDate);
          return {
            name: 'item.invokeDate',
            value: [
              [date.getFullYear(), date.getMonth()+1, date.getDay()].join('/') + ' ' +
              [date.getHours(), date.getMinutes(), date.getSeconds()].join(':'),
              item.tps
            ]
          }
        });
        option.series[0].data = chartData;
        option.series[1].data = chartData2;
        serviceChart1.setOption(option);
        serviceChart1.setOption({title:{textStyle:{fontSize:14},text:'TPS'}});
        serviceChart2.setOption(option);
        serviceChart2.setOption({title:{textStyle:{fontSize:14},text:'响应时间(ms)'}});
        serviceChart3.setOption(option);
        serviceChart3.setOption({title:{textStyle:{fontSize:14},text:'平均网络传输速度'}});
        clientChart1.setOption(option);
        clientChart1.setOption({title:{textStyle:{fontSize:14},text:'TPS'}});
        clientChart2.setOption(option);
        clientChart2.setOption({title:{textStyle:{fontSize:14},text:'响应时间(ms)'}});
        clientChart3.setOption(option);
        clientChart3.setOption({title:{textStyle:{fontSize:14},text:'平均网络传输速度'}});
        console.info(chartData);
        // serviceChart1.setOption([{
        //   data: chartData
        // }])

        //测试重复时间戳
        console.info(times);
        const newTimes = []
        for(var i=0; i<times.length; i++){
          if(times.indexOf(times[i])==-1){
            newTimes.push(times[i])
          }
        }
        console.info(newTimes);
      });

      //
      // let url = 'http://10.110.0.14:8788/salukiMonitor/data?service=com.quancheng.examples.service.HelloService';
      // url = 'filesystem:http://10.110.0.14:8788/temporary/ivniymo2/1479457776962.json';
      // this.$http.get(url).then((response)=>{ //http://10.110.0.13:9999
        // this.applicationList = response.body;
        // console.info(response.body);
      // })

    },
    methods:{

    }
  }

  function randomData() {
      now = new Date(+now + oneDay);
      value = value + Math.random() * 21 - 10;
      return {
          name: now.toString(),
          value: [
              [now.getMonth()+1, now.getDay()].join('/'),
              Math.round(value)
          ]
      }
  }

  var data = [];
  var now = +new Date(1997, 9, 3);
  var oneDay = 5 * 3600 * 1000;
  var value = Math.random() * 1000;
  for (var i = 0; i < 1000; i++) {
      data.push(randomData());
  }


  var json = [{"id":"c0895d8b77c94d84adf6291f5f59fbb9","invokeDate":1479457669380,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":1,"maxInput":12,"maxOutput":12,"maxElapsed":13,"maxConcurrent":1,"success":1,"failure":0,"input":12,"output":12,"elapsed":13,"tps":80,"kbps":0.8},{"id":"b876ac2fffa946dd8bf24911d207ea4d","invokeDate":1479457669373,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":3,"maxInput":12,"maxOutput":12,"maxElapsed":20,"maxConcurrent":3,"success":1,"failure":0,"input":12,"output":12,"elapsed":20,"tps":150,"kbps":1.5},{"id":"b46c2225d5854f31a759465dc50fd473","invokeDate":1479457669368,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":4,"maxInput":12,"maxOutput":12,"maxElapsed":24,"maxConcurrent":4,"success":1,"failure":0,"input":12,"output":12,"elapsed":24,"tps":170,"kbps":1.7},{"id":"2c3aa43ad3fd4c83a33ed3d55b261371","invokeDate":1479457669362,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":4,"maxInput":12,"maxOutput":12,"maxElapsed":30,"maxConcurrent":4,"success":1,"failure":0,"input":12,"output":12,"elapsed":30,"tps":130,"kbps":1.3},{"id":"6d463e6288244901b3066a9655ef18bf","invokeDate":1479457669349,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":5,"maxInput":12,"maxOutput":12,"maxElapsed":41,"maxConcurrent":5,"success":1,"failure":0,"input":12,"output":12,"elapsed":41,"tps":120,"kbps":1.2},{"id":"23c43a50134a49e29e7992fd7a36b0b1","invokeDate":1479457669349,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":7,"maxInput":12,"maxOutput":12,"maxElapsed":40,"maxConcurrent":7,"success":1,"failure":0,"input":12,"output":12,"elapsed":40,"tps":170,"kbps":1.7},{"id":"44bc76b8b0044d72b0c9ed2b9c7f1ff9","invokeDate":1479457669347,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":7,"maxInput":12,"maxOutput":12,"maxElapsed":42,"maxConcurrent":7,"success":1,"failure":0,"input":12,"output":12,"elapsed":42,"tps":170,"kbps":1.7},{"id":"655ad2dab5874e4ba0256e4421a56eff","invokeDate":1479457669344,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":9,"maxInput":12,"maxOutput":12,"maxElapsed":33,"maxConcurrent":9,"success":1,"failure":0,"input":12,"output":12,"elapsed":33,"tps":270,"kbps":2.7},{"id":"a53823aa97044e5da621296bd8e61ed6","invokeDate":1479457669344,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":7,"maxInput":12,"maxOutput":12,"maxElapsed":35,"maxConcurrent":7,"success":1,"failure":0,"input":12,"output":12,"elapsed":35,"tps":200,"kbps":2},{"id":"8e87d296b90a40ea83b413ed5dbc58e2","invokeDate":1479457669344,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":8,"maxInput":12,"maxOutput":12,"maxElapsed":33,"maxConcurrent":8,"success":1,"failure":0,"input":12,"output":12,"elapsed":33,"tps":240,"kbps":2.4},{"id":"5d547da57fb44f71bddb21ee1e9e9148","invokeDate":1479457669341,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":35,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":35,"tps":290,"kbps":2.9},{"id":"ef508f2d918448f0be71666416cc6a58","invokeDate":1479457669327,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":37,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":37,"tps":270,"kbps":2.7},{"id":"4aa95665c00246ea8cb36c790ce61c32","invokeDate":1479457669324,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":45,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":45,"tps":220,"kbps":2.2},{"id":"371126a8778846a6be9139f3c2d016b5","invokeDate":1479457669323,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":36,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":36,"tps":280,"kbps":2.8},{"id":"e8926067de304b3d8e4d83d7fcd8ec2a","invokeDate":1479457669322,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":9,"maxInput":12,"maxOutput":12,"maxElapsed":24,"maxConcurrent":9,"success":1,"failure":0,"input":12,"output":12,"elapsed":24,"tps":370,"kbps":3.7},{"id":"258b90f70d404434bcdfeeb1cd225232","invokeDate":1479457669321,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":9,"maxInput":12,"maxOutput":12,"maxElapsed":25,"maxConcurrent":9,"success":1,"failure":0,"input":12,"output":12,"elapsed":25,"tps":360,"kbps":3.6},{"id":"38d0a6966d214e1688bf9659f4420d69","invokeDate":1479457669319,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":9,"maxInput":12,"maxOutput":12,"maxElapsed":22,"maxConcurrent":9,"success":1,"failure":0,"input":12,"output":12,"elapsed":22,"tps":410,"kbps":4.1},{"id":"b8ff97c349ba459a9c2cde57d2dd7139","invokeDate":1479457669319,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":9,"maxInput":12,"maxOutput":12,"maxElapsed":22,"maxConcurrent":9,"success":1,"failure":0,"input":12,"output":12,"elapsed":22,"tps":410,"kbps":4.1},{"id":"088774f7c7b64a66848d66471ec30bc9","invokeDate":1479457669317,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":7,"maxInput":12,"maxOutput":12,"maxElapsed":27,"maxConcurrent":7,"success":1,"failure":0,"input":12,"output":12,"elapsed":27,"tps":260,"kbps":2.6},{"id":"4129ad934e694c8f90ff1fd8bfcd0f43","invokeDate":1479457669317,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":8,"maxInput":12,"maxOutput":12,"maxElapsed":24,"maxConcurrent":8,"success":1,"failure":0,"input":12,"output":12,"elapsed":24,"tps":330,"kbps":3.3},{"id":"a4c74db3772542e4b0fac08306315dc2","invokeDate":1479457669293,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":44,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":44,"tps":230,"kbps":2.3},{"id":"fe6a175f9b4a4fa4aa2d472115d48212","invokeDate":1479457669289,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":7,"maxInput":12,"maxOutput":12,"maxElapsed":25,"maxConcurrent":7,"success":1,"failure":0,"input":12,"output":12,"elapsed":25,"tps":280,"kbps":2.8},{"id":"d440426f390840e68b233bde918af6fe","invokeDate":1479457669287,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":26,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":26,"tps":380,"kbps":3.8},{"id":"d383d9fb0eef496e95a2f5320337c729","invokeDate":1479457669287,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":9,"maxInput":12,"maxOutput":12,"maxElapsed":27,"maxConcurrent":9,"success":1,"failure":0,"input":12,"output":12,"elapsed":27,"tps":330,"kbps":3.3},{"id":"904212a0227e4ab5bc0ea01f2e9e4fd9","invokeDate":1479457669287,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":9,"maxInput":12,"maxOutput":12,"maxElapsed":32,"maxConcurrent":9,"success":1,"failure":0,"input":12,"output":12,"elapsed":32,"tps":280,"kbps":2.8},{"id":"e70f02b5581840a299728419c455b7bb","invokeDate":1479457669283,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":8,"maxInput":12,"maxOutput":12,"maxElapsed":37,"maxConcurrent":8,"success":1,"failure":0,"input":12,"output":12,"elapsed":37,"tps":220,"kbps":2.2},{"id":"c655b3c2b15c41f29d6510d8cef279df","invokeDate":1479457669277,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":8,"maxInput":12,"maxOutput":12,"maxElapsed":46,"maxConcurrent":8,"success":1,"failure":0,"input":12,"output":12,"elapsed":46,"tps":170,"kbps":1.7},{"id":"95d648d402fe4c9abf647b66dcf7e130","invokeDate":1479457669277,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":8,"maxInput":12,"maxOutput":12,"maxElapsed":41,"maxConcurrent":8,"success":1,"failure":0,"input":12,"output":12,"elapsed":41,"tps":200,"kbps":2},{"id":"9c81b68781fb4e0c9a7d381b952b5fc5","invokeDate":1479457669276,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":37,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":37,"tps":270,"kbps":2.7},{"id":"bbfa045c4cc748388ce5057ee1b131a7","invokeDate":1479457669274,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":7,"maxInput":12,"maxOutput":12,"maxElapsed":47,"maxConcurrent":7,"success":1,"failure":0,"input":12,"output":12,"elapsed":47,"tps":150,"kbps":1.5},{"id":"6389ab96a0474ed1afc4215ef3771df3","invokeDate":1479457669253,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":37,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":37,"tps":270,"kbps":2.7},{"id":"489a59c98dce4afe903f69a031d33c1b","invokeDate":1479457669247,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":7,"maxInput":12,"maxOutput":12,"maxElapsed":39,"maxConcurrent":7,"success":1,"failure":0,"input":12,"output":12,"elapsed":39,"tps":180,"kbps":1.8},{"id":"10f18832bda64dec91abbea3e88a0165","invokeDate":1479457669246,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":37,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":37,"tps":270,"kbps":2.7},{"id":"2dde9e20fbf648188ce438fddfaaa5ec","invokeDate":1479457669244,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":7,"maxInput":12,"maxOutput":12,"maxElapsed":30,"maxConcurrent":7,"success":1,"failure":0,"input":12,"output":12,"elapsed":30,"tps":230,"kbps":2.3},{"id":"a08cc1a2de81406d982f626d1113c417","invokeDate":1479457669244,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":39,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":39,"tps":260,"kbps":2.6},{"id":"bafeff8848284d76a2273fa7b33ba7eb","invokeDate":1479457669243,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":40,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":40,"tps":250,"kbps":2.5},{"id":"69bd6a0ddbac4dc5a3f1663fc8a738fc","invokeDate":1479457669235,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":7,"maxInput":12,"maxOutput":12,"maxElapsed":40,"maxConcurrent":7,"success":1,"failure":0,"input":12,"output":12,"elapsed":40,"tps":170,"kbps":1.7},{"id":"a2e2f6b1d1164a85a1113239ae6d2f10","invokeDate":1479457669233,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":9,"maxInput":12,"maxOutput":12,"maxElapsed":37,"maxConcurrent":9,"success":1,"failure":0,"input":12,"output":12,"elapsed":37,"tps":240,"kbps":2.4},{"id":"ded3aef1ee5f4e4284aa4038f24a2d88","invokeDate":1479457669228,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":41,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":41,"tps":240,"kbps":2.4},{"id":"23a3e7880d5748e8b8bcc3470885a38c","invokeDate":1479457669228,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":41,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":41,"tps":240,"kbps":2.4},{"id":"f32895418d58481d8edc78152cd3e64e","invokeDate":1479457668511,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":4,"maxInput":12,"maxOutput":12,"maxElapsed":672,"maxConcurrent":4,"success":1,"failure":0,"input":12,"output":12,"elapsed":672,"tps":10,"kbps":0.1},{"id":"8e8c20266a844e45a9d06b1d8cb452f0","invokeDate":1479457668511,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":4,"maxInput":12,"maxOutput":12,"maxElapsed":672,"maxConcurrent":4,"success":1,"failure":0,"input":12,"output":12,"elapsed":672,"tps":10,"kbps":0.1},{"id":"ec45d866c5aa408886c9325f524fd0cc","invokeDate":1479457668510,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":665,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":665,"tps":20,"kbps":0.2},{"id":"35625a9049c146ef963fe05e7ddc7bf2","invokeDate":1479457668510,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":667,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":667,"tps":10,"kbps":0.1},{"id":"c2502728bef04df7b48ec531c027ae2d","invokeDate":1479457668510,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":4,"maxInput":12,"maxOutput":12,"maxElapsed":674,"maxConcurrent":4,"success":1,"failure":0,"input":12,"output":12,"elapsed":674,"tps":10,"kbps":0.1},{"id":"931b07cd8a074c0e9dd74fc12d23caf1","invokeDate":1479457668510,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":663,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":663,"tps":20,"kbps":0.2},{"id":"f718cd9e132d4ba2a56509557c222b61","invokeDate":1479457668510,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":666,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":666,"tps":20,"kbps":0.2},{"id":"7e13dbe2026d440f837add0d10a85421","invokeDate":1479457668510,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":666,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":666,"tps":20,"kbps":0.2},{"id":"e2e3e34fd08a48dfa134942492af55cf","invokeDate":1479457668510,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":10,"maxInput":12,"maxOutput":12,"maxElapsed":664,"maxConcurrent":10,"success":1,"failure":0,"input":12,"output":12,"elapsed":664,"tps":20,"kbps":0.2},{"id":"4fb65d950ea74555b15c5ec2c4e74d74","invokeDate":1479457668510,"service":"com.quancheng.examples.service.HelloService","method":"sayHello","consumer":"10.110.0.14","provider":"10.110.0.14","type":"consumer","concurrent":4,"maxInput":12,"maxOutput":12,"maxElapsed":673,"maxConcurrent":4,"success":1,"failure":0,"input":12,"output":12,"elapsed":673,"tps":10,"kbps":0.1}];
  var json2 = [];
  var json3 = [];
  var json2Date = new Date().getTime();

  var xDate = [];
  var num = 100;
    // ms
  for(let i=0; i< 5*60; i++){
    var x = Math.random();
    var xxx = Math.floor(x*100);
    var tps = 0;
    if(xxx/10==4){
      tps = Math.floor(xxx/10)+num+30
    }else if(xxx==81){
      tps = Math.floor(xxx/10)+num-30
    }else{
      tps = x*10%2!=1 ? Math.floor(xxx/20)+num : 10
    }


    json2.push({
      invokeDate: json2Date+(i*1000),
      tps:  tps
    })

    json3.push({
      invokeDate: json2Date+(i*1000)+3000,
      tps: tps + Math.floor(Math.random()*100)
    })
  }

</script>

<style lang="stylus">
  #service-chart
    .service-chart
      height 300px
      padding 5px
      &>div
        height 100%
</style>
