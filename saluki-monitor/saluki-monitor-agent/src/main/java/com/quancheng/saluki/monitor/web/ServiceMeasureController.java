package com.quancheng.saluki.monitor.web;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.boot.saluki.starter.SalukiService;
import com.quancheng.boot.saluki.starter.autoconfigure.SalukiProperties;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.core.utils.ReflectUtil;
import com.quancheng.saluki.monitor.invoke.GenericInvokeMetadata;
import com.quancheng.saluki.monitor.invoke.GenericInvokeUtils;
import com.quancheng.saluki.monitor.invoke.MetadataType;
import com.taobao.jaket.Jaket;
import com.taobao.jaket.model.MethodDefinition;
import com.taobao.jaket.model.ServiceDefinition;

@RestController
@RequestMapping("serviceMeasure")
public class ServiceMeasureController {

    private final Gson                 gson = new Gson();

    @Autowired
    private SalukiProperties           salukiProperties;

    @Autowired
    private AbstractApplicationContext applicationContext;

    @SalukiReference(service = "com.quancheng.saluki.core.grpc.service.GenericService", group = "Default", version = "1.0.0")
    private GenericService             genricService;

    @RequestMapping(value = "getAllMethod", method = RequestMethod.GET)
    public List<MethodDefinition> getAllMethod(@RequestParam(value = "service", required = true) String service) throws ClassNotFoundException {
        try {
            Class<?> clazz = ReflectUtil.name2class(service);
            ServiceDefinition sd = Jaket.build(clazz);
            return sd.getMethods();
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    @RequestMapping(value = "getMethod", method = RequestMethod.GET)
    public GenericInvokeMetadata getMethod(@RequestParam(value = "service", required = true) String service,
                                           @RequestParam(value = "method", required = true) String method) throws ClassNotFoundException {
        try {
            Class<?> clazz = ReflectUtil.name2class(service);
            ServiceDefinition serviceMeta = Jaket.build(clazz);
            if (!method.contains("~")) {
                for (MethodDefinition methodDef : serviceMeta.getMethods()) {
                    if (methodDef.getName().equals(method)) {
                        method = method + "~" + methodDef.getParameterTypes()[0];
                        break;
                    }
                }
            }
            GenericInvokeMetadata meta = GenericInvokeUtils.getGenericInvokeMetadata(serviceMeta, method,
                                                                                     MetadataType.DEFAULT_VALUE);
            return meta;
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    @RequestMapping(value = "testService", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
    public Object testService(@RequestBody ServiceMeasureModel model) throws ClassNotFoundException {
        try {
            Class<?> requestClass = ReflectUtil.name2class(model.getParameterType());
            Object request = gson.fromJson(model.getParameter(), requestClass);
            String[] paramTypes = new String[] { model.getParameterType(), model.getReturnType() };
            Object[] args = new Object[] { request };
            Object reply = genricService.$invoke(model.getService(), findGroup(model.getService()),
                                                 findVersion(model.getService()), model.getMethod(), paramTypes, args);
            return reply;
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    private String findGroup(String interfaceClassName) throws ClassNotFoundException {
        if (salukiProperties.getServiceGroup() != null) {
            return salukiProperties.getServiceGroup();
        } else {
            Class<?> interfaceClass = ReflectUtil.name2class(interfaceClassName);
            SalukiService annotation = getSalukiAnnotation(interfaceClass);
            return annotation.group();
        }
    }

    private String findVersion(String interfaceClassName) throws ClassNotFoundException {
        if (salukiProperties.getServcieVersion() != null) {
            return salukiProperties.getServcieVersion();
        } else {
            Class<?> interfaceClass = ReflectUtil.name2class(interfaceClassName);
            SalukiService annotation = getSalukiAnnotation(interfaceClass);
            return annotation.version();
        }
    }

    private SalukiService getSalukiAnnotation(Class<?> beanType) {
        Map<String, ?> beanMap = applicationContext.getBeansOfType(beanType);
        for (Map.Entry<String, ?> entry : beanMap.entrySet()) {
            Object obj = entry.getValue();
            SalukiService salukiAnnotation = obj.getClass().getAnnotation(SalukiService.class);
            return salukiAnnotation;
        }
        throw new IllegalArgumentException("There no bean in spring container,pls check again ");
    }
}
