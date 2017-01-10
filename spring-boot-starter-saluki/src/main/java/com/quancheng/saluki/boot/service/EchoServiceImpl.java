/*
 * Copyright (c) 2016, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.boot.service;

import com.quancheng.saluki.boot.SalukiService;
import com.quancheng.saluki.service.EchoService;
import com.quancheng.saluki.service.serviceparam.EchoReply;
import com.quancheng.saluki.service.serviceparam.EchoRequest;

/**
 * @author shimingliu 2016年12月29日 下午2:35:00
 * @version EchoServiceImpl.java, v 0.0.1 2016年12月29日 下午2:35:00 shimingliu
 */
@SalukiService
public class EchoServiceImpl implements EchoService {

    @Override
    public EchoReply echo(EchoRequest echorequest) {
        EchoReply reply = new EchoReply();
        reply.setMessage("Hello World");
        return reply;
    }

}
