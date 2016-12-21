/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.maven;

import java.io.File;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import com.google.common.collect.Lists;
import com.quancheng.saluki.plugin.Proto2ServicePojo;

/**
 * @author shimingliu 2016年12月17日 下午12:31:10
 * @version CountMojo.java, v 0.0.1 2016年12月17日 下午12:31:10 shimingliu
 */
@Mojo(name = "proto2java", defaultPhase = LifecyclePhase.GENERATE_SOURCES)
public class Proto2Java extends AbstractMojo {

    @Parameter(defaultValue = "src/main/proto")
    private String     protoPath;

    @Parameter(defaultValue = "src/main/java")
    private String     buildPath;

    private List<File> allProtoFile = Lists.newArrayList();

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        File deirectory = new File(protoPath);
        listAllProtoFile(deirectory);
        Proto2ServicePojo protp2ServicePojo = Proto2ServicePojo.forConfig(protoPath, buildPath);
        for (File file : allProtoFile) {
            if (file.exists()) {
                String protoFilePath = file.getPath();
                protp2ServicePojo.generateFile(protoFilePath);
            }
        }
    }

    private File listAllProtoFile(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                File[] fileArray = file.listFiles();
                if (fileArray != null) {
                    for (int i = 0; i < fileArray.length; i++) {
                        listAllProtoFile(fileArray[i]);
                    }
                }
            } else {
                if (StringUtils.endsWith(file.getName(), "proto")) {
                    allProtoFile.add(file);
                }
            }
        }
        return null;
    }
}
