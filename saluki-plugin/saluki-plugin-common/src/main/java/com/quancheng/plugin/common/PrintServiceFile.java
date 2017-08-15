/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.plugin.common;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.protobuf.DescriptorProtos.MethodDescriptorProto;

/**
 * @author shimingliu 2016年12月21日 下午3:39:54
 * @version PrintServiceFile.java, v 0.0.1 2016年12月21日 下午3:39:54 shimingliu
 */
public final class PrintServiceFile extends AbstractPrint {

  private Map<String, String> pojoTypeCache;

  private List<MethodDescriptorProto> serviceMethods;

  public PrintServiceFile(String fileRootPath, String sourcePackageName, String className) {
    super(fileRootPath, sourcePackageName, className);
  }

  public void setPojoTypeCache(Map<String, String> pojoTypeCache) {
    this.pojoTypeCache = pojoTypeCache;
  }

  public void setServiceMethods(List<MethodDescriptorProto> serviceMethods) {
    this.serviceMethods = serviceMethods;
  }

  @Override
  protected List<String> collectFileData() {
    String className = super.getClassName();
    String packageName = super.getSourcePackageName().toLowerCase();
    List<String> fileData = Lists.newArrayList();
    fileData.add("package " + packageName + ";");
    fileData.add("public interface " + className + "{");
    for (MethodDescriptorProto method : serviceMethods) {
      String outPutType = method.getOutputType();
      String inPutType = method.getInputType();
      String methodName = method.getName();
      inPutType = CommonUtils.findPojoTypeFromCache(inPutType, pojoTypeCache);
      outPutType = CommonUtils.findPojoTypeFromCache(outPutType, pojoTypeCache);
      String stream = generateGrpcStream(method, inPutType, outPutType);
      if (method.getServerStreaming()) {
        outPutType = "io.grpc.stub.StreamObserver<" + outPutType + ">";
      }
      String inputValue = CommonUtils.findNotIncludePackageType(inPutType).toLowerCase();
      if (method.getClientStreaming()) {
        inPutType = "io.grpc.stub.StreamObserver<" + inPutType + ">";
      }
      String methodStr =
          "public " + outPutType + " " + methodName + "(" + inPutType + " " + inputValue + ");";
      if (stream != null)
        fileData.add(stream);
      fileData.add(methodStr);
    }
    fileData.add("}");
    return fileData;
  }

  private String generateGrpcStream(MethodDescriptorProto method, String inPutType,
      String outPutType) {
    String format =
        "@com.quancheng.saluki.core.grpc.annotation.GrpcMethodType(methodType=%s,requestType=%s,responseType=%s)";
    if (method.getServerStreaming() && method.getClientStreaming()) {
      String stream = String.format(format, "io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING",
          inPutType + ".class", outPutType + ".class");
      return stream;
    } else {
      if (!method.getServerStreaming() && method.getClientStreaming()) {
        String stream =
            String.format(format, "io.grpc.MethodDescriptor.MethodType.SERVER_STREAMING",
                inPutType + ".class", outPutType + ".class");
        return stream;
      } else if (method.getServerStreaming() && !method.getClientStreaming()) {
        String stream =
            String.format(format, "io.grpc.MethodDescriptor.MethodType.CLIENT_STREAMING",
                inPutType + ".class", outPutType + ".class");
        return stream;
      }
      return String.format(format, "io.grpc.MethodDescriptor.MethodType.UNARY",
          inPutType + ".class", outPutType + ".class");
    }

  }

}
