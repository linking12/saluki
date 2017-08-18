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



# Usage
### Checking app is deployed sucessfullly
```sh
curl -i http://localhost:9191/api/hello
Hello User!
```
### Access secure resource with token
```sh
curl -i http://localhost:9191/api/secure

{"timestamp":1444985908768,"status":401,"error":"Unauthorized","message":"Access Denied","path":"/api/secure"}
```

### Fetching refresh_token
```sh
curl -vu rajithapp:secret 'http://localhost:9191/api/oauth/token?username=admin&password=admin&grant_type=password'

{"access_token":"91202244-431f-444a-b053-7f50716f2012","token_type":"bearer","refresh_token":"e6f8624f-213d-4343-a971-980e83f734be","expires_in":1738,"scope":"read write"}
```

### Fetching acess_token by submitting refresh_token
```sh
curl -vu rajithapp:secret 'http://localhost:9191/api/oauth/token?grant_type=refresh_token&refresh_token=<refresh_token>'

{"access_token":"821c99d4-2c9f-4990-b68d-18eacaff54b2","token_type":"bearer","refresh_token":"e6f8624f-213d-4343-a971-980e83f734be","expires_in":1799,"scope":"read write"}
```

### Access secure resource sucessfully
```sh
curl -i -H "Authorization: Bearer <access_token>" http://localhost:9191/api/secure

Secure Hello!
```