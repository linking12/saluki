package com.quancheng.saluki.gateway.oauth2.limiter;

import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Component
public class RateLimiter {

    @Value("${access.limit.intervalInMills}")
    private long      intervalInMills;

    @Value("${access.limit.limit}")
    private long      limit;

    @Autowired
    private JedisPool jedisPool;

    private double    intervalPerPermit;

    @PostConstruct
    public void init() {
        intervalPerPermit = intervalInMills * 1.0 / limit;
    }

    public synchronized boolean access(String userId) {
        String key = genKey(userId);
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
                    System.out.println("grantedTokens=" + grantedTokens);
                    currentTokensRemaining = Math.min(grantedTokens + tokenBucket.getTokensRemaining(), limit);
                }
                assert currentTokensRemaining >= 0;
                if (currentTokensRemaining == 0) {
                    tokenBucket.setTokensRemaining(currentTokensRemaining);
                    jedis.hmset(key, tokenBucket.toHash());
                    System.out.println("tokenBucket=" + tokenBucket);
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

    private String genKey(String userId) {
        return "rate:limiter:" + intervalInMills + ":" + limit + ":" + userId;
    }

    public static void main(String[] args) throws InterruptedException {
        RateLimiter rateLimiter = new RateLimiter();
        for (int i = 0; i < 3; i++) {
            boolean root = rateLimiter.access("root");
            System.out.println(root);
        }
        boolean root = rateLimiter.access("root");
        System.out.println(root);
        Thread.sleep(4000);
        root = rateLimiter.access("root");
        System.out.println(root);

    }
}
