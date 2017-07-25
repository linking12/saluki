README: [English](https://github.com/linking12/saluki/blob/master/README.md) | [中文](https://github.com/linking12/saluki/blob/master/README-zh.md)

# Overview

* saluki is a microservice framework rely on grpc-java

# Features
* support genric,proxy,stub module 
* provide the service definition by interface,and the parameter definition by pojo，developer can develop service by interface and pojo
* service registry and discover
* service route rule config(<a href="http://dubbo.io/User+Guide-zh.htm#UserGuide-zh-%E8%B7%AF%E7%94%B1%E8%A7%84%E5%88%99">sample</a>)
* failover by retry
* hystrix integrated
* spring-boot integrated
* validator interfrated


# Detail

* saluki-plugin provide maven or gradle plugin to generate interface及java bean
* saluki-serializer provide transform pojo to protobuf model and  protobuf model to pojo
* saluki-registry provide consul registered and discover

# Compile
```
   mvn install -Dmaven.test.skip=true
   
```
# Sample
  <a href="https://github.com/linking12/saluki/tree/master/saluki-example">sample</a>
  
# Quick Start

* dependency

```
<dependency>
	<groupId>com.quancheng.saluki</groupId>
	<artifactId>spring-boot-starter-saluki</artifactId>
	<version>1.5.5-SNAPSHOT</version>
</dependency>
<dependency>
	<groupId>com.quancheng.saluki</groupId>
	<artifactId>spring-boot-starter-saluki-monitor</artifactId>
	<version>1.5.5-SNAPSHOT</version>
</dependency>
```

* start spring boot main function，and viste http://localhost:8080/doc, you can test your service
![login](./doc/service.jpeg)
