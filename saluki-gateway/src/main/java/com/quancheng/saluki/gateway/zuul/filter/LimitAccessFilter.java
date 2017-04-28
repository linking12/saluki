/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.zuul.filter;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.tuple.Triple;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.quancheng.saluki.gateway.oauth2.service.DatabaseUserDetailService;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author shimingliu 2017年3月23日 上午10:43:08
 * @version Oauth2Filter.java, v 0.0.1 2017年3月23日 上午10:43:08 shimingliu
 */
public class LimitAccessFilter extends ZuulFilter {

    private final JedisPool                 jedisPool;

    private final DatabaseUserDetailService databaseUserDetailService;

    public LimitAccessFilter(DatabaseUserDetailService databaseUserDetailService, JedisPool jedisPool){
        super();
        this.databaseUserDetailService = databaseUserDetailService;
        this.jedisPool = jedisPool;
    }

    @Override
    public String filterType() {
        return "pre";
    }

    @Override
    public int filterOrder() {
        return 0;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String auth = request.getHeader("Authorization");
        String accessToken = auth.split(" ")[1];
        ctx.set("LimitAccess", Boolean.TRUE);
        try {
            Triple<Long, String, Long> clientTriple = databaseUserDetailService.loadClientByToken(accessToken);
            String user = clientTriple.getMiddle();
            Long intervalInMills = clientTriple.getLeft();
            Long limits = clientTriple.getRight();
            if (intervalInMills != null && intervalInMills != 0l && limits != null && limits != 0l) {
                if (!access(user, intervalInMills, limits)) {
                    ctx.set("LimitAccess", Boolean.FALSE);
                    ctx.setSendZuulResponse(false);
                    ctx.setResponseStatusCode(HttpServletResponse.SC_BAD_REQUEST);
                    ctx.setResponseBody("The times of usage is limited");
                }
            }
        } catch (Throwable e) {
        }
        return null;
    }

    public synchronized boolean access(String userId, long intervalInMills, long limit) {
        String key = genKey(userId, intervalInMills, limit);
        double intervalPerPermit = intervalInMills * 1.0 / limit;
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> counter = jedis.hgetAll(key);
            if (counter.size() == 0) {
                TokenBucket tokenBucket = new TokenBucket(System.currentTimeMillis(), limit - 1);
                jedis.hmset(key, tokenBucket.toHash());
                return true;
            } else {
                TokenBucket tokenBucket = TokenBucket.fromHash(counter);
                long lastRefillTime = tokenBucket.getLastRefillTime();
                long refillTime = System.currentTimeMillis();
                long intervalSinceLast = refillTime - lastRefillTime;
                long currentTokensRemaining;
                if (intervalSinceLast > intervalInMills) {
                    currentTokensRemaining = limit;
                } else {
                    long grantedTokens = (long) (intervalSinceLast / intervalPerPermit);
                    currentTokensRemaining = Math.min(grantedTokens + tokenBucket.getTokensRemaining(), limit);
                }
                assert currentTokensRemaining >= 0;
                if (currentTokensRemaining == 0) {
                    tokenBucket.setTokensRemaining(currentTokensRemaining);
                    jedis.hmset(key, tokenBucket.toHash());
                    return false;
                } else {
                    tokenBucket.setLastRefillTime(refillTime);
                    tokenBucket.setTokensRemaining(currentTokensRemaining - 1);
                    jedis.hmset(key, tokenBucket.toHash());
                    return true;
                }
            }
        }
    }

    private String genKey(String userId, long intervalInMills, long limit) {
        return "rate:limiter:" + intervalInMills + ":" + limit + ":" + userId;
    }

    private static class TokenBucket {

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

}
