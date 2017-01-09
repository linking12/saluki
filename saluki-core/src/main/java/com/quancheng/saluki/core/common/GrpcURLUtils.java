package com.quancheng.saluki.core.common;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class GrpcURLUtils {

    private static final Pattern KVP_PATTERN = Pattern.compile("([_.a-zA-Z0-9][-_.a-zA-Z0-9]*)[=](.*)");

    public static Map<String, String> parseQueryString(String qs) {
        if (qs == null || qs.length() == 0) return new HashMap<String, String>();
        String[] tmp = qs.split("\\&");
        Map<String, String> map = new HashMap<String, String>(tmp.length);
        for (int i = 0; i < tmp.length; i++) {
            Matcher matcher = KVP_PATTERN.matcher(tmp[i]);
            if (matcher.matches() == false) continue;
            map.put(matcher.group(1), matcher.group(2));
        }
        return map;
    }

    public static boolean isMatch(GrpcURL subscribedUrl, GrpcURL providerUrl) {
        String subscribedInterface = subscribedUrl.getServiceInterface();
        String providerInterface = providerUrl.getServiceInterface();
        if (StringUtils.equals(subscribedInterface, providerInterface)) {
            return false;
        }
        if (!providerUrl.getParameter(Constants.ENABLED_KEY, true)) {
            return false;
        }
        String subscribedGroup = subscribedUrl.getGroup();
        String subscribedVersion = subscribedUrl.getVersion();
        String providerGroup = providerUrl.getGroup();
        String providerVersion = providerUrl.getVersion();
        return StringUtils.equals(subscribedGroup, providerGroup)
               && StringUtils.equals(subscribedVersion, providerVersion);
    }

    public static boolean isMatchGlobPattern(String pattern, String value, GrpcURL param) {
        if (param != null && pattern.startsWith("$")) {
            pattern = param.getRawParameter(pattern.substring(1));
        }
        return isMatchGlobPattern(pattern, value);
    }

    public static boolean isMatchGlobPattern(String pattern, String value) {
        if ("*".equals(pattern)) return true;
        if ((pattern == null || pattern.length() == 0) && (value == null || value.length() == 0)) return true;
        if ((pattern == null || pattern.length() == 0) || (value == null || value.length() == 0)) return false;

        int i = pattern.lastIndexOf('*');
        // 没有找到星号
        if (i == -1) {
            return value.equals(pattern);
        }
        // 星号在末尾
        else if (i == pattern.length() - 1) {
            return value.startsWith(pattern.substring(0, i));
        }
        // 星号的开头
        else if (i == 0) {
            return value.endsWith(pattern.substring(i + 1));
        }
        // 星号的字符串的中间
        else {
            String prefix = pattern.substring(0, i);
            String suffix = pattern.substring(i + 1);
            return value.startsWith(prefix) && value.endsWith(suffix);
        }
    }

    private GrpcURLUtils(){

    }

}
