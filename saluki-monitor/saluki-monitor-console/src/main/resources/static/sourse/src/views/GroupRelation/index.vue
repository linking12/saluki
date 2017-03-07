<template>
	<div class="row" id="group-relation">
		<div class="col-md-8 relation-chart" ref="groupRelation"></div>
		<div class="col-md-4 relation-info">
			<template v-if="activeAppName==null">
				<p><b>README:</b></p>
				<p>
					点击 “圆点” 看详情
				</p>
				<p>
					- 应用调用方向 [ <i class="ti-arrow-right"></i>] <br>
					- 服务名 <br>
					- 服务调用次数 [ <i class="ti-clip"></i>]
				</p>
			</template>
			<template>
				<p><b>{{activeAppName}}</b></p>
				<div v-for="(apps, type) in appInfo">
					<hr v-if="apps.length>0">
					<div v-for="app in apps" class="relation-info__item">
						<div class="relation-info__service"><b>{{app.source}} <i class="ti-arrow-right"></i> <span>{{app.target}}</span></b></div>
						<div v-for="service in app.dependcyServices">
							{{service.serviceName}} <span><i class="ti-clip"></i>{{service.callCount}}</span>
						</div>
					</div>
				</div>
			</template>
		</div>
	</div>
</template>

<script type="text/babel">
	import echarts from 'echarts';
	import apiData from './mockApiJson.js';
	export default {
		data(){
			return{
				data: [],
				appEdges: [],
				activeAppName: null
			}
		},
		mounted() {
			const chartsInstance = echarts.init(this.$refs.groupRelation);
			chartsInstance.on('click', (params)=>{
		    console.log(params);
				this.selectApp(params.data.id);
			});

			let url = '/api/application/dependcy';
      this.$http.get(url).then((response)=>{
				const data = this.formatChartData(response.body);
				this.data = response.body;
				this.appEdges = data.edges;
				const option = this.getDefaultChartOption(data);
				chartsInstance.setOption(option);
      }, (response)=>{
				this.data = [];
				this.appEdges = [];
				this.activeAppName = null;
      });

		},
		methods: {
			getDefaultChartOption(data){
				return {
					title: {},
					animation: true,
					animationDurationUpdate: 500,
					animationEasingUpdate: 'quinticInOut',
					series: [{
						type: 'graph',
						layout: 'none',//'force',//'circular',//
						// progressiveThreshold: 700,
						label: {
							normal:{
								show: true,
								position: 'top'
							}
						},
						data: data.nodes,
						edgeLabel: {
							// emphasis: {
							// 	show: true,
							// 	position: 'end',
							// 	textStyle: {
							// 		color: '#000',
							// 		fontStyle: 'italic'
							// 	}
							// }
						},
						links: data.edges,
						edgeSymbol: ['', 'arrow'],
						roam: true,
						focusNodeAdjacency: true,
						lineStyle: {
							normal: {
								width: 0.5,
								curveness: 0.3,
								opacity: 0.5
							}
						}
					}]
				}
			},
			formatChartData(data){
				const num = data.length;
				const nodes = [];
				const edges = [];
				const wNum = Math.ceil(Math.sqrt(num)); //矩阵图，每行个数
				const split = 1/wNum;
				const line = 1000;	//长度

				data.map((item,index)=>{
					const z = (index+1)/wNum;
					const yx = z.toString().split('.');
					let y = yx.length==1 ? yx[0]-1 : yx[0];
					let x = yx.length==2 ? yx[1] : 1;
					y = y*split*line;
					x = x==1 ? 1*line : ('0.'+x)*line;
					nodes.push({
						"x": x,
						"y": y,
						"name": item.appName,
						"id": item.appName,
						"symbolSize": 5,
						"itemStyle": {
							"normal": {
								"color": '#C23531'
							}
						}
					});

					item.dependcyApps.map((app,appIndex)=>{
						edges.push({
							source: item.appName,
							target: app.appName
						})
					});
				});
				return {nodes, edges}
			},
			selectApp(name){
				this.activeAppName = name
			},
			cancelSelectApp(){
				this.activeAppName = null
			}
		},
		computed:{
			appInfo(){
				if(this.activeAppName){
					const sourceApps = [];
					const targerApps = [];
					const names = [this.activeAppName];
					for(let i=0; i<this.appEdges.length; i++){
						if(this.appEdges[i].target == this.activeAppName){
							names.push(this.appEdges[i].source)
						}
					}
					this.data.map((item,index)=>{
						const k = names.indexOf(item.appName);
						if(k==0){
							item.dependcyApps.map((app,appIndex)=>{
								sourceApps.push({
									source: item.appName,
									target: app.appName,
									dependcyServices: app.dependcyServices
								})
							})
						}else if(k>0){
							item.dependcyApps.map((app,appIndex)=>{
								if(app.appName==this.activeAppName){
									targerApps.push({
										source: item.appName,
										target: app.appName,
										dependcyServices: app.dependcyServices
									})
								}
							})
						}
					})
					return {sourceApps,targerApps};
				}else{
					return null
				}
			}
		}
	}
</script>

<style lang="stylus">
	#group-relation
		width 100%
		height 100%
		.relation-chart
			height 100%
		.relation-info
			font-size 12px
			background rgba(255,255,255,0.1)
			border-radius 5px
			padding 5px 2px
			&__item
				margin-bottom 5px
			&__service
				span
					font-style oblique
</style>
