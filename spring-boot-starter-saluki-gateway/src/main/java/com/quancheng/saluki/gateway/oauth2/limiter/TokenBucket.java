/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.oauth2.limiter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author shimingliu 2017年3月23日 上午11:49:04
 * @version TokenBucket.java, v 0.0.1 2017年3月23日 上午11:49:04 shimingliu
 */
public class TokenBucket {

    private long lastRefillTime;
    private long tokensRemaining;

    public TokenBucket(long lastRefillTime, long tokensRemaining){
        this.lastRefillTime = lastRefillTime;
        this.tokensRemaining = tokensRemaining;
    }

    public long getTokensRemaining() {
        return this.tokensRemaining;
    }

    public void setTokensRemaining(long tokensRemaining) {
        this.tokensRemaining = tokensRemaining;
    }

    public long getLastRefillTime() {
        return this.lastRefillTime;
    }

    public void setLastRefillTime(long lastRefillTime) {
        this.lastRefillTime = lastRefillTime;
    }

    public Map<String, String> toHash() {
        Map<String, String> hash = new HashMap<>();
        hash.put("lastRefillTime", String.valueOf(lastRefillTime));
        hash.put("tokensRemaining", String.valueOf(tokensRemaining));
        return hash;
    }

    public static TokenBucket fromHash(Map<String, String> hash) {
        long lastRefillTime = Long.parseLong(hash.get("lastRefillTime"));
        int tokensRemaining = Integer.parseInt(hash.get("tokensRemaining"));
        return new TokenBucket(lastRefillTime, tokensRemaining);
    }

}
