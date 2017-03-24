/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.gateway.filters.pre;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.quancheng.saluki.gateway.oauth2.security.Authorities;
import com.quancheng.saluki.gateway.oauth2.security.UserDetailsService;
import com.quancheng.saluki.gateway.oauth2.support.Authority;
import com.quancheng.saluki.gateway.oauth2.support.User;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author shimingliu 2017年3月23日 上午10:43:08
 * @version Oauth2Filter.java, v 0.0.1 2017年3月23日 上午10:43:08 shimingliu
 */
public class Oauth2LimitAccessFilter extends ZuulFilter {

    private final JedisPool          jedisPool;

    private final UserDetailsService userDetailsService;

    public Oauth2LimitAccessFilter(UserDetailsService userDetailsService, JedisPool jedisPool){
        super();
        this.userDetailsService = userDetailsService;
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
        User user = userDetailsService.loadUsernameByToken(accessToken);
        Set<Authority> userAuthorities = user.getAuthorities();
        Boolean isAdmin = userAuthorities.contains(Authorities.ROLE_ADMIN.name());
        Boolean isAnyOne = userAuthorities.contains(Authorities.ROLE_ANONYMOUS.name());
        if (!isAdmin && !isAnyOne) {
            String userName = user.getUsername();
            Long intervalInMills = user.getIntervalInMills();
            Long limit = user.getLimit();
            if (intervalInMills != null && intervalInMills != 0l && limit != null && limit != 0l) {
                if (!access(userName, intervalInMills, limit)) {
                    ctx.setSendZuulResponse(false);
                    ctx.setResponseStatusCode(401);
                    ctx.setResponseBody("The times of usage is limited");
                }
            }
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
