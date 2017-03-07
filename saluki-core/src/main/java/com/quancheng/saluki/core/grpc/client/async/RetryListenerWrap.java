/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.client.async;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author shimingliu 2017年2月16日 下午5:17:08
 * @version RetryListenerWrap.java, v 0.0.1 2017年2月16日 下午5:17:08 shimingliu
 */
public class RetryListenerWrap implements Runnable {

    private final static Logger log = LoggerFactory.getLogger(RetryListenerWrap.class);

    private Runnable            listener;

    public RetryListenerWrap(Runnable listener){
        this.listener = listener;
    }

    @Override
    public void run() {
        try {
            listener.run();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
        }
    }

}
