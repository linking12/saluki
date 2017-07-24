/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the confidential and
 * proprietary information of Quancheng-ec.com ("Confidential Information"). You shall not disclose
 * such Confidential Information and shall use it only in accordance with the terms of the license
 * agreement you entered into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.config;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.Constants;
import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.utils.CollectionUtils;
import com.quancheng.saluki.core.utils.ReflectUtils;

/**
 * @author shimingliu 2016年12月14日 下午2:10:17
 * @version RpcReferenceConfig.java, v 0.0.1 2016年12月14日 下午2:10:17 shimingliu
 */
public class RpcReferenceConfig extends RpcBaseConfig {

  private static final long serialVersionUID = 837201897877342163L;

  private Class<?> serviceClass;

  private String serviceName;

  private String group;

  private String version;

  private boolean isGeneric;

  private boolean isGrpcStub;

  private boolean async;

  private boolean isEnabledFallBack;

  private Integer timeout;

  private Set<String> retryMethods;

  private Set<String> fallbackMethods;

  private Integer reties;

  private Set<Class> validatorGroups;


  private transient Object ref;

  public RpcReferenceConfig() {}

  public Class<?> getServiceClass() {
    return serviceClass;
  }

  public void setServiceClass(Class<?> serviceClass) {
    this.serviceClass = serviceClass;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public boolean isGeneric() {
    return isGeneric;
  }

  public void setGeneric(boolean isGeneric) {
    this.isGeneric = isGeneric;
  }

  public boolean isGrpcStub() {
    return isGrpcStub;
  }

  public void setGrpcStub(boolean isGrpcStub) {
    this.isGrpcStub = isGrpcStub;
  }

  public boolean isAsync() {
    return async;
  }

  public void setAsync(boolean async) {
    this.async = async;
  }

  public Integer getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public Set<String> getRetryMethods() {
    return retryMethods;
  }

  public void setRetryMethods(Set<String> retryMethods) {
    this.retryMethods = retryMethods;
  }

  public Integer getReties() {
    return reties;
  }

  public void setReties(int reties) {
    this.reties = reties;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public boolean isEnabledFallBack() {
    return isEnabledFallBack;
  }

  public void setEnabledFallBack(boolean isEnabledFallBack) {
    this.isEnabledFallBack = isEnabledFallBack;
  }



  public Set<String> getFallbackMethods() {
    return fallbackMethods;
  }

  public void setFallbackMethods(Set<String> fallbackMethods) {
    this.fallbackMethods = fallbackMethods;
  }

  public Set<Class> getValidatorGroups() {
    return validatorGroups;
  }

  public void setValidatorGroups(final Set<Class> validatorGroups) {
    this.validatorGroups = validatorGroups;
  }

  public synchronized Object getProxyObj() {
    if (ref == null) {
      try {
        Map<String, String> params = Maps.newHashMap();
        if (isGeneric()) {
          params.put(Constants.GENERIC_KEY, Boolean.TRUE.toString());
        }
        if (isGrpcStub()) {
          params.put(Constants.GRPC_STUB_KEY, Boolean.TRUE.toString());
        }
        if (isEnabledFallBack()) {
          params.put(Constants.GRPC_FALLBACK_KEY, Boolean.TRUE.toString());
        }
        String serviceName = getServiceName();
        this.addGroup(params);
        this.addVersion(params);
        this.addApplication(params);
        this.addTimeOut(params);
        this.addRetryMethods(params);
        this.addFallBackMethods(params);
        this.addReties(params);
        this.addInterval(params);
        this.addServiceClass(params);
        this.addAsync(params);
        this.addMonitorInterval(params);
        this.addHttpPort(params);
        this.addValidatorGroups(params);
        GrpcURL refUrl = new GrpcURL(Constants.REMOTE_PROTOCOL, super.getHost(),
            super.getHttpPort(), serviceName, params);
        ref = super.getGrpcEngine().getClient(refUrl);
      } catch (Exception e) {
        throw new IllegalStateException("Create Grpc client failed!", e);
      }
    }
    return ref;
  }

  private void addValidatorGroups(final Map<String, String> params) {
    if (this.getValidatorGroups() != null && this.getValidatorGroups().size() > 0) {
      StringBuffer sb = new StringBuffer();
      this.getValidatorGroups().stream().forEach(new Consumer<Class>() {
        @Override
        public void accept(final Class aClass) {
          sb.append(aClass.getName()).append(";");
        }
      });

      params.put(Constants.VALIDATOR_GROUPS, sb.substring(0, sb.length() - 1));
    }
  }

  private void addAsync(Map<String, String> params) {
    if (this.isAsync()) {
      params.put(Constants.ASYNC_KEY, String.valueOf(Constants.RPCTYPE_ASYNC));
    } else {
      params.put(Constants.ASYNC_KEY, String.valueOf(Constants.RPCTYPE_BLOCKING));
    }
  }

  private void addGroup(Map<String, String> params) {
    String group = getGroup();
    if (StringUtils.isNotBlank(group)) {
      params.put(Constants.GROUP_KEY, group);
    } else {
      String application = super.getApplication();
      if (StringUtils.isNotBlank(application)) {
        params.put(Constants.GROUP_KEY, application);
      }
    }
  }

  private void addVersion(Map<String, String> params) {
    String version = getVersion();
    if (StringUtils.isNotBlank(version)) {
      params.put(Constants.VERSION_KEY, version);
    }
  }

  private void addApplication(Map<String, String> params) {
    String application = super.getApplication();
    if (StringUtils.isNotBlank(application)) {
      params.put(Constants.APPLICATION_NAME, application);
    }
  }

  private void addTimeOut(Map<String, String> params) {
    Integer timeOut = getTimeout();
    if (timeOut != null && timeOut != 0) {
      params.put(Constants.TIMEOUT, timeOut.toString());
    }
  }

  private void addRetryMethods(Map<String, String> params) {
    Set<String> retryMethods = getRetryMethods();
    validateMethods(retryMethods);
    params.put(Constants.RETRY_METHODS_KEY, StringUtils.join(retryMethods, ","));
  }

  private void addFallBackMethods(Map<String, String> params) {
    Set<String> fallbackMethods = getFallbackMethods();
    validateMethods(fallbackMethods);
    params.put(Constants.FALLBACK_METHODS_KEY, StringUtils.join(fallbackMethods, ","));
  }


  private void validateMethods(Set<String> methods) {
    Boolean isNotGeneric = !isGeneric();
    Boolean isNotStub = !isGrpcStub();
    if (isNotGeneric && isNotStub && CollectionUtils.isNotEmpty(methods)) {
      try {
        String serviceName = getServiceName();
        Class<?> serviceClzz = ReflectUtils.name2class(serviceName);
        setServiceClass(serviceClzz);
        for (String methodName : methods) {
          Method hasMethod = null;
          for (Method method : serviceClzz.getMethods()) {
            if (method.getName().equals(methodName)) {
              hasMethod = method;
            }
          }
          if (hasMethod == null) {
            throw new IllegalArgumentException(
                "The interface " + serviceName + " not found method " + methodName);
          }
        }
      } catch (ClassNotFoundException e) {
        throw new IllegalArgumentException(e.getMessage(), e);
      }
    }
  }

  private void addReties(Map<String, String> params) {
    Integer reties = getReties();
    if (reties != null && reties != 0) {
      params.put(Constants.METHOD_RETRY_KEY, reties.toString());
    }
  }

  private void addInterval(Map<String, String> params) {
    Integer interval = getMonitorinterval();
    if (interval != null && interval != 0) {
      params.put(Constants.MONITOR_INTERVAL, interval.toString());
    }
  }

  private void addServiceClass(Map<String, String> params) {
    if (isGrpcStub()) {
      String serviceClassName = getServiceName();
      Class<?> serviceClass = getServiceClass();
      if (serviceClass != null) {
        serviceClassName = serviceClass.getName();
      }
      params.put(Constants.INTERFACECLASS_KEY, serviceClassName);
    }
  }
}
