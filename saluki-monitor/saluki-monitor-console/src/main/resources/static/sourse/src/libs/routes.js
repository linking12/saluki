const RouterConfig = [
  {
    path: '/',
    name: 'view:index',
    redirect: { path: '/group/relation' }
  },
  {
    path: '/home',
    name: 'view:home',
    component: require('./../views/Home/index.vue')
  },
  {
    path: '/about',
    name: 'view:about',
    component: require('./../views/About/index.vue')
  },
  {
    path: '/group/relation',
    name: 'view:group.relation',
    title: '应用调用',
    component: require('./../views/GroupRelation/index.vue')
  },
  {
    path: '/service/group',
    name: 'view:service.group',
    title: '应用列表',
    component: require('./../views/ServiceGroup/index.vue')
  },
  {
    path: '/service/search',
    name: 'view:service.search',
    title: '服务查询',
    component: require('./../views/ServiceSearch/index.vue')
  },
  {
    path: '/service/detail/:service',
    name: 'view:service.detail',
    title: '服务详情',
    component: require('./../views/ServiceDetail/index.vue')
  },
  {
    path: '/service/map',
    name: 'view:service.map',
    title: '服务信息',
    component: require('./../views/ServiceMap/index.vue')
  },
  {
    path: '/service/chart/:service/:typelist',
    name: 'view:service.chart',
    title: '服务统计',
    component: require('./../views/ServiceChart/index4.vue')
  },
  {
    path: '/service/test/:ipPort/:service',
    name: 'view:service.test',
    title: '服务测试',
    component: require('./../views/ServiceTest/index.vue')
  },
  {
    path: '/route/rule/list',
    name: 'view:route.rule.list',
    title: '路由规则',
    component: require('./../views/RouteRule/index.vue')
  },
  {
    path: '/route/rule/add',
    name: 'view:route.rule.add',
    title: '路由规则',
    component: require('./../views/RouteRule/add.vue')
  }
]

export default RouterConfig;
