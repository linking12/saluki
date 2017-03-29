# 概述

saluki是以Grpc作为底层，提供一套高性能、易于使用的分布式远程服务调用(RPC)框架

# 功能

* 与consul配置服务组件进行集成，提供集群环境的服务发现及治理能力。
* 简化开发方式，对于Grpc原生stub方式进行封装，提供了使用interface和pojo作为服务契约的方式
* 与spring-boot进行集成，提供了autoconfig的方式
* 扩展grpc的NameResolver，提供了RoundRobin负载均衡方式

# Compile
```
   mvn install -Dmaven.test.skip=true
   
```
# Quick Start

* 首先使用saluki提供的gradle或maven插件根据protoc文件生成interface及pojo模型,包括grpc提供的插件
```
        <dependency>
			<groupId>com.quancheng.saluki</groupId>
			<artifactId>saluki-core</artifactId>
			<version>1.5.2</version>
			<scope>provided</scope>
		</dependency>
		<plugin>
				<groupId>com.quancheng.saluki</groupId>
				<artifactId>saluki-maven-plugin</artifactId>
				<version>1.5.2</version>
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

* 添加spring-boot-saluki依赖

```
        <dependency>
			<groupId>com.quancheng.saluki</groupId>
			<artifactId>spring-boot-starter-saluki</artifactId>
			<version>1.5.2</version>
		</dependency>
		<dependency>
			<groupId>com.quancheng.saluki</groupId>
			<artifactId>spring-boot-starter-saluki-monitor</artifactId>
			<version>1.5.2</version>
		</dependency>
```
