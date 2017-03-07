<template>
	<div id="wrapper" v-if="!loading">
		<loading></loading>
		<layout-left></layout-left>
		<layout-content>
			<template slot="content">
				<router-view></router-view>
			</template>
		</layout-content>
	</div>
</template>

<script type="text/babel">

	import Loading from './components/layout/loading.vue';
	import LayoutLeft from './components/layout/left.vue';
	import LayoutNav from './components/layout/nav.vue';
	import LayoutContent from './components/layout/content.vue';

	export default {
		components: {
			Loading,
			LayoutLeft,
			LayoutNav,
			LayoutContent
		},
		data(){
			return {
				loading: true
			}
		},
		mounted(){
			console.info('----1');
			// this.loading = false;

			this.$nextTick(function () {
		    // code that assumes this.$el is in-document
				console.info('----2');
		  });
			this.$forceUpdate(function(){
				console.info('----5');
			});

			setTimeout(()=>{
				this.loading = false
			},1000)
		},
		updated(){
			console.info('----3');
		},
		activated(){
			console.info('----4');
		}
	}
</script>

<style lang="stylus">
	html
	body
	#wrapper
	#page-wrapper
		height 100%
	html
		body
			background #edf1f5
			#page-wrapper
				overflow-y scroll
				padding-top 25px
</style>
