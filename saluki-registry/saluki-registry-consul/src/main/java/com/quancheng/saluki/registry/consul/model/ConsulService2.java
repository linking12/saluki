/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.registry.consul.model;

import com.ecwid.consul.v1.agent.model.NewService;

/**
 * @author shimingliu 2017年1月24日 上午10:56:25
 * @version ConsulService2.java, v 0.0.1 2017年1月24日 上午10:56:25 shimingliu
 */
public class ConsulService2 {

    private ConsulService service;

    private NewService    newService;

    public ConsulService2(ConsulService service, NewService newService){
        super();
        this.service = service;
        this.newService = newService;
    }

    public ConsulService getService() {
        return service;
    }

    public void setService(ConsulService service) {
        this.service = service;
    }

    public NewService getNewService() {
        return newService;
    }

    public void setNewService(NewService newService) {
        this.newService = newService;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((newService == null) ? 0 : newService.hashCode());
        result = prime * result + ((service == null) ? 0 : service.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ConsulService2 other = (ConsulService2) obj;
        if (newService == null) {
            if (other.newService != null) return false;
        } else if (!newService.equals(other.newService)) return false;
        if (service == null) {
            if (other.service != null) return false;
        } else if (!service.equals(other.service)) return false;
        return true;
    }

    @Override
    public String toString() {
        return "ConsulService2 [service=" + service + ", newService=" + newService + "]";
    }

}
