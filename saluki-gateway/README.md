# 概述

 saluki gateway是以zuul、saluki、oauth2为基础构建的网关系统

# 功能

* api限流，利用令牌桶机制进行限流操作
* 授权，oauth2进行api的授权
* 协议转化，将http的请求转化为grpc的服务请求

# 系统后台管理
![login](./imgs/login.jpeg)

![token](./imgs/token.jpeg)

![token](./imgs/route.jpeg)