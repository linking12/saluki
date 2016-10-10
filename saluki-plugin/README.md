生成grpc接口的插件

用法


classpath 'com.quancheng.gradle.plugins:salukirpc:1.0-SNAPSHOT'

//  Using salukirpc plugin
apply plugin: 'salukirpc'

compileJava.dependsOn generateProtoInterface
generateProtoInterface.dependsOn generateProtoModel
