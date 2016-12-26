package com.quancheng.saluki.plugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import groovy.io.FileType
import com.quancheng.plugin.common.Proto2ServicePojo;


class Proto2Java implements Plugin<Project> {
    void apply(Project project) {
        project.task('proto2java') << {
            String directoryPath = project.projectDir.path + "/src/main/proto";
            String buildPath = project.projectDir.path + "/build/generated/source/proto/main/java";
            Proto2ServicePojo protp2ServicePojo = Proto2ServicePojo.forConfig(directoryPath, buildPath);
            def dir = new File(project.projectDir.path + "/src/main/proto")
            dir.traverse(type: FileType.FILES,
                    nameFilter: ~/.*\.proto/
            ){
                protp2ServicePojo.generateFile(it.path);
            }
        }
    }
}