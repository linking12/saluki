/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.plugin.common;

import java.util.List;

import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos.EnumValueDescriptorProto;

/**
 * @author shimingliu 2016年12月19日 下午3:14:06
 * @version GenerateFileUtil.java, v 0.0.1 2016年12月19日 下午3:14:06 shimingliu
 */
public final class PrintEnumFile extends AbstractPrint {

    private List<EnumValueDescriptorProto> enumFields;

    public PrintEnumFile(String fileRootPath, String sourcePackageName, String className){
        super(fileRootPath, sourcePackageName, className);
    }

    public void setEnumFields(List<EnumValueDescriptorProto> enumFields) {
        this.enumFields = enumFields;
    }

    @Override
    protected List<String> collectFileData() {
        String className = super.getClassName();
        String packageName = super.getSourcePackageName().toLowerCase();
        List<String> fileData = Lists.newArrayList();
        fileData.add("package " + packageName + ";");
        fileData.add("");

        fileData.add("public enum " + className + "{");
        fileData.add("");

        for (int i = 0; i < enumFields.size(); i++) {
            EnumValueDescriptorProto enumField = enumFields.get(i);
            if (i == enumFields.size() - 1) {
                fileData.add("  " + enumField.getName() + "(" + enumField.getNumber() + ");");
            } else {
                fileData.add("  " + enumField.getName() + "(" + enumField.getNumber() + "),");
            }
        }

        fileData.add("");

        fileData.add("  private final int value;");
        fileData.add("");

        fileData.add("  private " + className + "(int value) {");
        fileData.add("      this.value = value;");
        fileData.add("  }");
        fileData.add("");

        fileData.add("  public final int getNumber() {");
        fileData.add("      return value;");
        fileData.add("  }");
        fileData.add("");

        fileData.add("  public static " + className + " forNumber(Integer value) {");
        fileData.add("      switch (value) {");
        for (int i = 0; i < enumFields.size(); i++) {
            EnumValueDescriptorProto enumField = enumFields.get(i);
            if (i == enumFields.size() - 1) {
                fileData.add("      case " + enumField.getNumber() + ":");
                fileData.add("          return " + enumField.getName() + ";");
                fileData.add("      default:");
                fileData.add("          return null;");
            } else {
                fileData.add("      case " + enumField.getNumber() + ":");
                fileData.add("          return " + enumField.getName() + ";");
            }
        }
        fileData.add("      }");
        fileData.add("");

        fileData.add("  }");
        fileData.add("");

        fileData.add("}");
        return fileData;
    }

}
