package com.quancheng.saluki.monitor.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.gson.Gson;
import com.quancheng.boot.saluki.starter.SalukiReference;
import com.quancheng.saluki.core.grpc.service.GenericService;
import com.quancheng.saluki.core.utils.ReflectUtil;
import com.taobao.jaket.Jaket;
import com.taobao.jaket.model.MethodDefinition;
import com.taobao.jaket.model.ServiceDefinition;
import com.taobao.jaket.model.TypeDefinition;

@RestController
@RequestMapping("/serviceMeasure")
public class ServiceMetaController {

    private final Gson     gson = new Gson();

    @SalukiReference(service = "com.quancheng.saluki.core.grpc.service.GenericService", group = "Default", version = "1.0.0")
    private GenericService genricService;

    @RequestMapping(value = "/getAllMethod", method = RequestMethod.GET)
    public List<MethodDefinition> getAllMethod(@RequestParam(value = "service", required = true) String service) throws ClassNotFoundException {
        try {
            Class<?> clazz = ReflectUtil.name2class(service);
            ServiceDefinition sd = Jaket.build(clazz);
            return sd.getMethods();
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    @RequestMapping(value = "/getMethod", method = RequestMethod.GET)
    public MethodDefinition getMethod(@RequestParam(value = "service", required = true) String service,
                                      @RequestParam(value = "method", required = true) String method) throws ClassNotFoundException {
        try {
            Class<?> clazz = ReflectUtil.name2class(service);
            ServiceDefinition serviceMeta = Jaket.build(clazz);
            List<MethodDefinition> methodMetas = serviceMeta.getMethods();
            MethodDefinition targetMethodMeta = null;
            for (MethodDefinition methodMeta : methodMetas) {
                if (methodMeta.getName().equals(method)) {
                    targetMethodMeta = methodMeta;
                    break;
                }
            }
            String[] requestTypes = targetMethodMeta.getParameterTypes();
            List<TypeDefinition> parameters = new ArrayList<TypeDefinition>();
            targetMethodMeta.setParameters(parameters);
            for (String requestType : requestTypes) {
                for (TypeDefinition parameterMeta : serviceMeta.getTypes()) {
                    if (parameterMeta.getType().equals(requestType)) {
                        parameters.add(parameterMeta);
                    }
                }
            }
            return targetMethodMeta;
        } catch (ClassNotFoundException e) {
            throw e;
        }
    }

    @RequestMapping(value = "/testService", method = RequestMethod.POST)
    public Object testService(@RequestParam(value = "group", required = true) String group,
                              @RequestParam(value = "version", required = true) String version,
                              @RequestParam(value = "service", required = true) String service,
                              @RequestParam(value = "method", required = true) String method,
                              @RequestParam(value = "parameterType", required = true) String parameterType,
                              @RequestParam(value = "returnType", required = true) String returnType,
                              @RequestParam(value = "parameter", required = true) String parameter) throws ClassNotFoundException {
        try {
            Class<?> requestClass = ReflectUtil.name2class(parameterType);
            Object request = gson.fromJson(parameter, requestClass);
            String[] paramTypes = new String[] { parameterType, returnType };
            Object[] args = new Object[] { request };
            Object reply = genricService.$invoke(service, group, version, method, paramTypes, args);
            return reply;
        } catch (ClassNotFoundException e) {
            throw e;
        }

    }

}
