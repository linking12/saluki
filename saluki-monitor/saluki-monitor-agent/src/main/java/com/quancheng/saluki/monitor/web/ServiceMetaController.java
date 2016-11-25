package com.quancheng.saluki.monitor.web;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.quancheng.saluki.core.utils.ReflectUtil;
import com.taobao.jaket.Jaket;
import com.taobao.jaket.model.MethodDefinition;
import com.taobao.jaket.model.ServiceDefinition;
import com.taobao.jaket.model.TypeDefinition;

@RestController
@RequestMapping("/serviceMeasure")
public class ServiceMetaController {

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

}
