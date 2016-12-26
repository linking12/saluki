# 概述

saluki-plugin是讲protobuf的proto文件转化为标准的Java interface和pojo的标准插件


# 功能
gradle 
```
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'com.google.protobuf:protobuf-gradle-plugin:0.8.0'
        classpath 'com.quancheng.saluki:saluki-gradle-plugin:1.5.1+'
    }
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.0.0"
    }
    plugins {
        grpc {
            artifact = 'io.grpc:protoc-gen-grpc-java:1.0.1'
        }
    }
    generateProtoTasks {
        all()*.plugins {
            grpc {}
        }
    }
}
apply plugin: 'proto2java'
compileJava.dependsOn proto2java
```
maven

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
					<protocArtifact>com.google.protobuf:protoc:3.0.0:exe:${os.detected.classifier}</protocArtifact>
					<pluginId>grpc-java</pluginId>
					<pluginArtifact>io.grpc:protoc-gen-grpc-java:1.0.1:exe:${os.detected.classifier}</pluginArtifact>
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
			<plugin>
				<groupId>com.quancheng.saluki</groupId>
				<artifactId>saluki-maven-plugin</artifactId>
				<version>1.5.1-SNAPSHOT</version>
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
		</plugins>
	</build>
```
