// import '!!script!jquery';

import Vue from 'vue';
import VueRouter from 'vue-router';
// import Vuex from 'vuex';
import VueResource from 'vue-resource';
// import { sync } from 'vuex-router-sync';

import routes from './libs/routes';
// import store from './vuex/store';
import App from './App.vue';

//开启debug模式
Vue.config.debug = true;

Vue.use(VueResource);

//路由
Vue.use(VueRouter);
const router = new VueRouter({
  // mode: 'history',
  routes,
  linkActiveClass: 'active'
});
// sync(store, router);

//创建和挂载根实例。
const app = new Vue({
  router,
  render: h => h(App)
}).$mount('#app')
