# 概述

saluki是以Grpc作为底层，提供一套高性能、易于使用的分布式远程服务调用(RPC)框架

# 功能

* 与consul配置服务组件进行集成，提供集群环境的服务发现及治理能力。
* 简化开发方式，对于Grpc原生stub方式进行封装，提供了使用interface和pojo作为服务契约的方式
* 与spring-boot进行集成，提供了autoconfig的方式
* 扩展grpc的NameResolver，提供了RoundRobin负载均衡方式

# Quick Start

* 首先使用saluki提供的gradle插件根据protoc文件生成interface及pojo模型

```
classpath 'com.quancheng.gradle.plugins:salukirpc:1.0-SNAPSHOT'
apply plugin: 'salukirpc'
compileJava.dependsOn generateProtoInterface 
generateProtoInterface.dependsOn generateProtoModel
```

* 根据protoc文件生成的接口及pojo如下所示

```
//pojo
@ProtobufEntity(User.UserGetRequest.class)
public class UserGetRequest
{
  @ProtobufAttribute
  private Long id;
  //getter setter
}

@ProtobufEntity(User.UserGetResponse.class)
public class UserGetResponse
{
  @ProtobufAttribute
  private BaseResponse base;
  @ProtobufAttribute
  private Long id;
  @ProtobufAttribute
  private String name;
  @ProtobufAttribute
  private String phone;
  //getter setter
}

//interface
public abstract interface UserService
{
  public abstract UserGetResponse get(UserGetRequest paramUserGetRequest);
}

```

* 添加spring-boot-saluki依赖

```
        gradle: compile 'com.quancheng:spring-boot-starter-saluki:0.0.1+
        
        maven:
        
        <dependency>
			<groupId>com.quancheng</groupId>
			<artifactId>spring-boot-starter-saluki</artifactId>
			<version>0.0.1-SNAPSHOT</version>
		</dependency>
		
		 
```

* 添加Application.properties

```
grpc.serverPort=12201 //服务暴露的端口
grpc.consulIp=192.168.99.101 //consul注册中心Ip
grpc.consulPort=8500 //consul注册中心port

```

* 服务消费端

```
 @GRpcReference(interfaceName = "com.quancheng.boot.starter.service.UserService", group = "default", version = "1.0.0")
    private UserService userSerivce;
    
```

* 服务提供端

```
@GRpcService(interfaceName = "com.quancheng.boot.starter.service.UserService", group = "default", version = "1.0.0")
public class GreeterServiceImpl implements GreeterService {

    @Override
    public UserGetResponse get(UserGetRequest request) {
       return new UserGetResponse();
    }

}

```