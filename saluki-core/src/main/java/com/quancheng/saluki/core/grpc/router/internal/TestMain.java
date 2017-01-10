/*
 * Copyright (c) 2017, Quancheng-ec.com All right reserved. This software is the
 * confidential and proprietary information of Quancheng-ec.com ("Confidential
 * Information"). You shall not disclose such Confidential Information and shall
 * use it only in accordance with the terms of the license agreement you entered
 * into with Quancheng-ec.com.
 */
package com.quancheng.saluki.core.grpc.router.internal;

import java.text.ParseException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.quancheng.saluki.core.common.GrpcURL;
import com.quancheng.saluki.core.common.GrpcURLUtils;

/**
 * @author shimingliu 2017年1月9日 下午2:39:20
 * @version TestMain.java, v 0.0.1 2017年1月9日 下午2:39:20 shimingliu
 */
public class TestMain {

    private static final Logger           log           = LoggerFactory.getLogger(ConditionRouter.class);

    private static final Pattern          ROUTE_PATTERN = Pattern.compile("([&!=,]*)\\s*([^&!=,\\s]+)");

    private static Map<String, MatchPair> whenCondition;

    private static Map<String, MatchPair> thenCondition;

    public static void main(String[] args) {
        String rule = "host = 10.110.0.16 => host = 10.110.0.16";
        parseRule(rule);

        GrpcURL refUrl = GrpcURL.valueOf("grpc://10.110.0.16:8081/com.quancheng.examples.service.HelloService?application=example-client&group=example&grpcstub=true&httpport=8081&interfaceClass=com.quancheng.examples.service.HelloServiceGrpc$HelloServiceFutureStub&methods=&monitorinterval=1&version=1.0.0");
        GrpcURL providerUrl = GrpcURL.valueOf("grpc://10.110.0.16:12201/com.quancheng.examples.service.HelloService?application=example-server&group=example&httpport=8080&version=1.0.0");

        boolean matchWhen = matchWhen(refUrl);
        boolean matchThen = matchThen(refUrl, providerUrl);
        System.out.println(matchWhen);
        System.out.println(matchThen);
    }

    public static boolean matchWhen(GrpcURL refUrl) {
        return matchCondition(whenCondition, refUrl, null);
    }

    public static boolean matchThen(GrpcURL providerUrl, GrpcURL param) {
        return thenCondition != null && matchCondition(thenCondition, providerUrl, param);
    }

    private static void parseRule(String rule) {
        int i = rule.indexOf("=>");
        String whenRule = i < 0 ? null : rule.substring(0, i).trim();
        String thenRule = i < 0 ? rule.trim() : rule.substring(i + 2).trim();
        try {
            whenCondition = doParseRule(whenRule);
            thenCondition = doParseRule(thenRule);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    private static boolean matchCondition(Map<String, MatchPair> condition, GrpcURL url, GrpcURL param) {
        Map<String, String> sample = url.toMap();
        for (Map.Entry<String, String> entry : sample.entrySet()) {
            String key = entry.getKey();
            MatchPair pair = condition.get(key);
            if (pair != null && !pair.isMatch(entry.getValue(), param)) {
                return false;
            }
        }
        return true;
    }

    private static Map<String, MatchPair> doParseRule(String rule) throws ParseException {
        Map<String, MatchPair> condition = new HashMap<String, MatchPair>();
        if (StringUtils.isBlank(rule)) {
            return condition;
        }
        // 匹配或不匹配Key-Value对
        MatchPair pair = null;
        // 多个Value值
        Set<String> values = null;
        final Matcher matcher = ROUTE_PATTERN.matcher(rule);
        while (matcher.find()) { // 逐个匹配
            String separator = matcher.group(1);
            String content = matcher.group(2);
            // 表达式开始
            if (separator == null || separator.length() == 0) {
                pair = new MatchPair();
                condition.put(content, pair);
            }
            // KV开始
            else if ("&".equals(separator)) {
                if (condition.get(content) == null) {
                    pair = new MatchPair();
                    condition.put(content, pair);
                } else {
                    condition.put(content, pair);
                }
            }
            // KV的Value部分开始
            else if ("=".equals(separator)) {
                if (pair == null)
                    throw new ParseException("Illegal route rule \"" + rule + "\", The error char '" + separator
                                             + "' at index " + matcher.start() + " before \"" + content + "\".",
                                             matcher.start());

                values = pair.matches;
                values.add(content);
            }
            // KV的Value部分开始
            else if ("!=".equals(separator)) {
                if (pair == null)
                    throw new ParseException("Illegal route rule \"" + rule + "\", The error char '" + separator
                                             + "' at index " + matcher.start() + " before \"" + content + "\".",
                                             matcher.start());

                values = pair.mismatches;
                values.add(content);
            }
            // KV的Value部分的多个条目
            else if (",".equals(separator)) { // 如果为逗号表示
                if (values == null || values.size() == 0)
                    throw new ParseException("Illegal route rule \"" + rule + "\", The error char '" + separator
                                             + "' at index " + matcher.start() + " before \"" + content + "\".",
                                             matcher.start());
                values.add(content);
            } else {
                throw new ParseException("Illegal route rule \"" + rule + "\", The error char '" + separator
                                         + "' at index " + matcher.start() + " before \"" + content + "\".",
                                         matcher.start());
            }
        }
        return condition;
    }

    private static final class MatchPair {

        final Set<String> matches    = new HashSet<String>();
        final Set<String> mismatches = new HashSet<String>();

        public boolean isMatch(String value, GrpcURL param) {
            for (String match : matches) {
                if (!GrpcURLUtils.isMatchGlobPattern(match, value, param)) {
                    return false;
                }
            }
            for (String mismatch : mismatches) {
                if (GrpcURLUtils.isMatchGlobPattern(mismatch, value, param)) {
                    return false;
                }
            }
            return true;
        }
    }

}
