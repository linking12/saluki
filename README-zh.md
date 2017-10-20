# 概述

* saluki是以Grpc作为底层，提供一套高性能、易于使用的服务化治理框架
* 对Grpc的开发进行扩展，支持java开发服务端，其他语言(php、go、c++)做客户端语言，不会对Grpc原生开发方式产生破坏

# 功能

* 服务注册及发现，提供集群环境的服务发现及治理能力
* 服务路由，基于javascript路由规则配置及host的路由规则配置(<a href="http://dubbo.io/User+Guide-zh.htm#UserGuide-zh-%E8%B7%AF%E7%94%B1%E8%A7%84%E5%88%99">路由规则示例</a>)
* 熔断及隔离，基于hystrix来进行的熔断、隔离、服务降级
* 简化开发方式，对于Grpc原生stub方式进行封装，提供了使用interface和java Bean作为服务契约的方式
* 与spring-boot进行集成，提供了autoconfig的方式
* ha重试功能，针对幂等服务可以开启ha功能，当前服务实例存在问题，会剔除当前实例，选择另外实例进行负载均衡
* 可以自由选择三种模式，genric、Stub、proxy，其中泛化在框架集成上，原生Stub模式针对原生已有的Grpc服务、普通代理模式推荐使用
* 通过在proto文件中配置options，使用hibernate validator进行参数的检查

# 详细说明

* saluki-plugin提供的插件可以在grpc插件基础上生成interface及java bean
* saluki-serializer提供了将protobuf与java bean两者对象互相转换
* saluki-registry提供了服务注册，可以在此扩展，目前仅支持consul

# Compile
```
   mvn install -Dmaven.test.skip=true
   
```
# 关于服务调用Sample
  详细请查看 <a href="https://github.com/linking12/saluki/tree/master/saluki-example">sample</a>
  
# Quick Start

* 首先在proto文件工程配置grpc提供的gradle或maven插件生成stub

示例：<a href="https://github.com/linking12/saluki/tree/master/saluki-service"> api </a>

```
<build>
  <extensions>
    <extension>
      <groupId>kr.motd.maven</groupId>
      <artifactId>os-maven-plugin</artifactId>
      <version>1.4.1.Final</version>
    </extension>
  </extensions>
  <plugins>
    <plugin>
      <groupId>org.xolstice.maven.plugins</groupId>
      <artifactId>protobuf-maven-plugin</artifactId>
      <version>0.5.0</version>
      <configuration>
        <protocArtifact>com.google.protobuf:protoc:3.0.2:exe:${os.detected.classifier}</protocArtifact>
        <pluginId>grpc-java</pluginId>
        <pluginArtifact>io.grpc:protoc-gen-grpc-java:1.2.0:exe:${os.detected.classifier}</pluginArtifact>
      </configuration>
      <executions>
        <execution>
          <goals>
            <goal>compile</goal>
            <goal>compile-custom</goal>
          </goals>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>

```
* 再次添加saluki提供的gradle或maven插件根据protoc文件生成interface及pojo模型

```
<dependency>
	<groupId>com.quancheng.saluki</groupId>
	<artifactId>saluki-core</artifactId>
	<version>1.5.7.RELEASE</version>
	<scope>provided</scope>
</dependency>

<plugin>
	<groupId>com.quancheng.saluki</groupId>
	<artifactId>saluki-maven-plugin</artifactId>
	<version>1.5.7.RELEASE</version>
	<configuration>
		<protoPath>src/main/proto</protoPath>
		<buildPath>target/generated-sources/protobuf/java</buildPath>
	</configuration>
	<executions>
		<execution>
			<goals>
				<goal>proto2java</goal>
			</goals>
		</execution>
	</executions>
</plugin>

```

添加后在生成Grpc的相关类之后，同时会生成interface及bean
![interface](./doc/interface.jpeg)
![bean](./doc/bean.jpeg)

* 应用上两步生成的artifactId，添加spring-boot-saluki依赖

```
<dependency>
	<groupId>com.quancheng.saluki</groupId>
	<artifactId>spring-boot-starter-saluki</artifactId>
	<version>1.5.7.RELEASE</version>
</dependency>
<dependency>
	<groupId>com.quancheng.saluki</groupId>
	<artifactId>spring-boot-starter-saluki-monitor</artifactId>
	<version>1.5.7.RELEASE</version>
</dependency>
```

* 启动spring boot main，并访问localhost:8080/doc,可进行服务测试
![login](./doc/service.jpeg)
