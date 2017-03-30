///*
// * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
// * confidential and proprietary information of Quancheng-ec.com ("Confidential
// * Information"). You shall not disclose such Confidential Information and shall
// * use it only in accordance with the terms of the license agreement you entered
// * into with Quancheng-ec.com.
// */
//package com.quancheng.saluki.gateway.zuul.filter;
//
//import java.util.Collection;
//import java.util.HashMap;
//import java.util.Map;
//
//import javax.servlet.http.HttpServletRequest;
//
//import org.springframework.security.core.GrantedAuthority;
//
//import com.netflix.zuul.ZuulFilter;
//import com.netflix.zuul.context.RequestContext;
//import com.quancheng.saluki.gateway.oauth2.security.Authorities;
//import com.quancheng.saluki.gateway.oauth2.security.UserDetailsService;
//
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisPool;
//
///**
// * @author shimingliu 2017年3月23日 上午10:43:08
// * @version Oauth2Filter.java, v 0.0.1 2017年3月23日 上午10:43:08 shimingliu
// */
//public class Oauth2LimitAccessFilter extends ZuulFilter {
//
//    private final JedisPool          jedisPool;
//
//    private final UserDetailsService userDetailsService;
//
//    public Oauth2LimitAccessFilter(UserDetailsService userDetailsService, JedisPool jedisPool){
//        super();
//        this.userDetailsService = userDetailsService;
//        this.jedisPool = jedisPool;
//    }
//
//    @Override
//    public String filterType() {
//        return "pre";
//    }
//
//    @Override
//    public int filterOrder() {
//        return 0;
//    }
//
//    @Override
//    public boolean shouldFilter() {
//        return true;
//    }
//
//    @Override
//    public Object run() {
//        RequestContext ctx = RequestContext.getCurrentContext();
//        HttpServletRequest request = ctx.getRequest();
//        String auth = request.getHeader("Authorization");
//        String accessToken = auth.split(" ")[1];
//        Map<String, Object> user = userDetailsService.loadUsernameByToken(accessToken);
//        org.springframework.security.core.userdetails.User realUser = (org.springframework.security.core.userdetails.User) user.get("user");
//        Collection<GrantedAuthority> userAuthorities = realUser.getAuthorities();
//        Boolean isAdmin = isRole(userAuthorities, Authorities.ROLE_ADMIN);
//        Boolean isAnyOne = isRole(userAuthorities, Authorities.ROLE_ANONYMOUS);
//        if (!isAdmin && !isAnyOne) {
//            String userName = realUser.getUsername();
//            Long intervalInMills = (Long) user.get("intervalInMills");
//            Long limit = (Long) user.get("limit");
//            if (intervalInMills != null && intervalInMills != 0l && limit != null && limit != 0l) {
//                if (!access(userName, intervalInMills, limit)) {
//                    ctx.setSendZuulResponse(false);
//                    ctx.setResponseStatusCode(401);
//                    ctx.setResponseBody("The times of usage is limited");
//                }
//            }
//        }
//        return null;
//    }
//
//    private boolean isRole(Collection<GrantedAuthority> userAuthorities, Authorities authorities) {
//        for (GrantedAuthority authority : userAuthorities) {
//            if (authority.getAuthority().equals(authorities.name())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public synchronized boolean access(String userId, long intervalInMills, long limit) {
//        String key = genKey(userId, intervalInMills, limit);
//        double intervalPerPermit = intervalInMills * 1.0 / limit;
//        try (Jedis jedis = jedisPool.getResource()) {
//            Map<String, String> counter = jedis.hgetAll(key);
//            if (counter.size() == 0) {
//                TokenBucket tokenBucket = new TokenBucket(System.currentTimeMillis(), limit - 1);
//                jedis.hmset(key, tokenBucket.toHash());
//                return true;
//            } else {
//                TokenBucket tokenBucket = TokenBucket.fromHash(counter);
//                long lastRefillTime = tokenBucket.getLastRefillTime();
//                long refillTime = System.currentTimeMillis();
//                long intervalSinceLast = refillTime - lastRefillTime;
//                long currentTokensRemaining;
//                if (intervalSinceLast > intervalInMills) {
//                    currentTokensRemaining = limit;
//                } else {
//                    long grantedTokens = (long) (intervalSinceLast / intervalPerPermit);
//                    currentTokensRemaining = Math.min(grantedTokens + tokenBucket.getTokensRemaining(), limit);
//                }
//                assert currentTokensRemaining >= 0;
//                if (currentTokensRemaining == 0) {
//                    tokenBucket.setTokensRemaining(currentTokensRemaining);
//                    jedis.hmset(key, tokenBucket.toHash());
//                    return false;
//                } else {
//                    tokenBucket.setLastRefillTime(refillTime);
//                    tokenBucket.setTokensRemaining(currentTokensRemaining - 1);
//                    jedis.hmset(key, tokenBucket.toHash());
//                    return true;
//                }
//            }
//        }
//    }
//
//    private String genKey(String userId, long intervalInMills, long limit) {
//        return "rate:limiter:" + intervalInMills + ":" + limit + ":" + userId;
//    }
//
//    private static class TokenBucket {
//
//        private long lastRefillTime;
//        private long tokensRemaining;
//
//        public TokenBucket(long lastRefillTime, long tokensRemaining){
//            this.lastRefillTime = lastRefillTime;
//            this.tokensRemaining = tokensRemaining;
//        }
//
//        public long getTokensRemaining() {
//            return this.tokensRemaining;
//        }
//
//        public void setTokensRemaining(long tokensRemaining) {
//            this.tokensRemaining = tokensRemaining;
//        }
//
//        public long getLastRefillTime() {
//            return this.lastRefillTime;
//        }
//
//        public void setLastRefillTime(long lastRefillTime) {
//            this.lastRefillTime = lastRefillTime;
//        }
//
//        public Map<String, String> toHash() {
//            Map<String, String> hash = new HashMap<>();
//            hash.put("lastRefillTime", String.valueOf(lastRefillTime));
//            hash.put("tokensRemaining", String.valueOf(tokensRemaining));
//            return hash;
//        }
//
//        public static TokenBucket fromHash(Map<String, String> hash) {
//            long lastRefillTime = Long.parseLong(hash.get("lastRefillTime"));
//            int tokensRemaining = Integer.parseInt(hash.get("tokensRemaining"));
//            return new TokenBucket(lastRefillTime, tokensRemaining);
//        }
//    }
//
//}
