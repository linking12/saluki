/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.plugin.common;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * @author shimingliu 2016年12月21日 下午4:12:41
 * @version AbstractPrint.java, v 0.0.1 2016年12月21日 下午4:12:41 shimingliu
 */
public abstract class AbstractPrint {

    protected final String fileRootPath;

    protected final String sourcePackageName;

    protected final String className;

    public AbstractPrint(String fileRootPath, String sourcePackageName, String className){
        this.fileRootPath = fileRootPath;
        this.sourcePackageName = sourcePackageName;
        this.className = className;
    }

    public String getClassName() {
        return this.className;
    }

    public String getSourcePackageName() {
        return this.sourcePackageName;
    }

    protected abstract List<String> collectFileData();

    public void print() {
        String fileName = fileRootPath + "/" + StringUtils.replace(sourcePackageName.toLowerCase(), ".", "/") + "/"
                          + className + ".java";
        File javaFile = new File(fileName);
        List<String> fileData = collectFileData();
        if (fileData != null) {
            try {
                FileUtils.writeLines(javaFile, "UTF-8", fileData);
            } catch (IOException e) {
                throw new IllegalArgumentException("can not write file to" + fileName, e);
            }
        }
    }

}
