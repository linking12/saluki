package com.ypp.thrall.core.common;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

public class ThrallURLUtils {

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

    public static boolean isMatch(ThrallURL subscribedUrl, ThrallURL providerUrl) {
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

    private ThrallURLUtils(){

    }

}
