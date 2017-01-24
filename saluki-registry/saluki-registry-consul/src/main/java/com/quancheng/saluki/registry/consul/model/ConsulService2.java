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
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        ConsulService2 other = (ConsulService2) obj;
        if (newService == null) {
            if (other.newService != null) {
                return false;
            }
        } else if (other.newService != null) {
            if (!newService.getId().equals(other.newService.getId())) {
                return false;
            }
        }
        return true;
    }

}
