package com.quancheng.saluki.core.config;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.quancheng.saluki.core.common.SalukiConstants;
import com.quancheng.saluki.core.common.SalukiURL;
import com.quancheng.saluki.core.grpc.exception.RpcFrameworkException;
import com.quancheng.saluki.core.utils.NetUtils;
import com.quancheng.saluki.core.utils.ReflectUtil;

public class ReferenceConfig extends BasicConfig {

    private static final long         serialVersionUID = -9023239057692247223L;

    // 分组
    protected String                  group;

    // 服务版本
    protected String                  version;

    // 接口名
    private String                    interfaceName;

    // 接口类型
    private Class<?>                  interfaceClass;

    // 是否使用泛接口
    private boolean                   generic;

    // 是否是injvm调用
    private boolean                   injvm;

    // 原生grpc stub调用
    private boolean                   grpcStub;

    // 是否异步
    private boolean                   async;

    // 请求超时时间
    private int                       requestTimeout;

    // 方法参数
    private List<MethodConfig>        methods;

    private transient volatile Object ref;

    public ReferenceConfig(){
    }

    public String getInterfaceName() {
        return interfaceName;
    }

    public void setInterfaceName(String interfaceName) {
        this.interfaceName = interfaceName;
    }

    public Class<?> getInterfaceClass() {
        return interfaceClass;
    }

    public void setInterfaceClass(Class<?> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    public Boolean getGeneric() {
        return generic;
    }

    public void setGeneric(Boolean generic) {
        this.generic = generic;
    }

    public Boolean getInjvm() {
        return injvm;
    }

    public void setInjvm(Boolean injvm) {
        this.injvm = injvm;
    }

    public Boolean getAsync() {
        return async;
    }

    public void setAsync(Boolean async) {
        this.async = async;
    }

    public Integer getRequestTimeout() {
        return requestTimeout;
    }

    public void setRequestTimeout(Integer requestTimeout) {
        this.requestTimeout = requestTimeout;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
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

    public Boolean getGrpcStub() {
        return grpcStub;
    }

    public void setGrpcStub(Boolean grpcStub) {
        this.grpcStub = grpcStub;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public synchronized Object get() {
        if (ref == null) {
            init();
        }
        return ref;
    }

    private void init() {
        loadRegistry();
        try {
            ref = grpcEngine.getProxy(buildRefUrl());
        } catch (Exception e) {
            throw new IllegalStateException("Create proxy failed ", e);
        }
    }

    private SalukiURL buildRefUrl() {
        Map<String, String> params = Maps.newHashMap();
        if (this.injvm) {
            params.put(SalukiConstants.GRPC_IN_LOCAL_PROCESS, Boolean.TRUE.toString());
        }
        if (!this.async) {
            params.put(SalukiConstants.RPCTYPE_KEY, Integer.valueOf(SalukiConstants.RPCTYPE_ASYNC).toString());
        }
        if (this.generic) {
            params.put(SalukiConstants.GENERIC_KEY, Boolean.TRUE.toString());
        }
        if (this.grpcStub) {
            params.put(SalukiConstants.GRPC_STUB_KEY, Boolean.TRUE.toString());
        }
        if (StringUtils.isNotBlank(this.group)) {
            params.put(SalukiConstants.GROUP_KEY, this.group);
        } else {
            if (StringUtils.isNotBlank(this.application)) {
                params.put(SalukiConstants.GROUP_KEY, this.application);
            }
        }
        if (StringUtils.isNotBlank(this.version)) {
            params.put(SalukiConstants.VERSION_KEY, version);
        }
        if (this.requestTimeout != 0) {
            params.put(SalukiConstants.RPCTIMEOUT_KEY, Integer.valueOf(requestTimeout).toString());
        }
        String interfaceClassName = interfaceClass != null ? interfaceClass.getName() : this.interfaceName;
        params.put(SalukiConstants.INTERFACECLASS_KEY, interfaceClassName);
        SalukiURL refUrl = new SalukiURL(SalukiConstants.DEFATULT_PROTOCOL, NetUtils.getLocalHost(), 0, interfaceName,
                                         params);
        return refUrl;
    }

    protected void addAnnotationToInterface(Class<?> interfaceClass, List<MethodConfig> methods) {
        if (interfaceClass == null) {
            throw new IllegalStateException("interface not allow null!");
        }
        // if (!interfaceClass.isInterface()) {
        // throw new IllegalStateException("The interface class " + interfaceClass + " is not a interface!");
        // }
        if (methods != null && !methods.isEmpty()) {
            for (MethodConfig methodBean : methods) {
                String methodName = methodBean.getName();
                for (java.lang.reflect.Method method : interfaceClass.getMethods()) {
                    Method method_ = null;
                    if (StringUtils.isNotBlank(methodName) && StringUtils.equals(method.getName(), methodName)) {
                        method_ = method;
                    } else {
                        boolean isNeglectMethod = ReflectUtil.neglectMethod(method);
                        if (!isNeglectMethod) {
                            method_ = method;
                        }
                    }
                    try {
                        Class<?> interfaceClassCopy = ReflectUtil.addHastrategyAnnotation(interfaceClass, method_,
                                                                                          methodBean.getReties());
                        interfaceClass = interfaceClassCopy;
                    } catch (Exception e) {
                        throw new RpcFrameworkException("Can not add com.quancheng.saluki.core.grpc.client.ha.Hastrategy to "
                                                        + interfaceClass.getName(), e);
                    }

                }
            }
        }
    }

    public static void main(String[] args) throws ClassNotFoundException {
        List<MethodConfig> config = Lists.newArrayList();
        MethodConfig methodConfig = new MethodConfig();
        methodConfig.setReties(2);
        config.add(methodConfig);
        Class<?> clzz = Class.forName("com.quancheng.saluki.core.grpc.client.ha.RetryOptions");
        ReferenceConfig referenceConfig = new ReferenceConfig();
        referenceConfig.addAnnotationToInterface(clzz, config);
        try {
            Method[] methods = clzz.getMethods();
            for (Method method : methods) {
                java.lang.annotation.Annotation[] annotations = method.getAnnotations();
                for (java.lang.annotation.Annotation ann : annotations) {
                    System.out.println(method.getName() + "==" + ann);
                }
            }

        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
