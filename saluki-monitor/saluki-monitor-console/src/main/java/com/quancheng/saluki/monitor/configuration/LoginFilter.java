package com.quancheng.saluki.monitor.configuration;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("LoginFilter")
public class LoginFilter implements Filter {

    private static final Logger log       = LoggerFactory.getLogger(LoginFilter.class);

    @Value("${ucenter.server.host}")
    private String              url       = "";
    @Value("${ucenter.client.appName}")
    private String              name      = "";
    @Value("${ucenter.client.whiteList}")
    private String              whiteList = "";

    @Override
    public void init(FilterConfig config) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
                                                                                              ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        Cookie[] cookies = req.getCookies();
        String Uid = null;
        if (cookies != null) {
            for (Cookie ck : cookies) {
                if ("userId".equals(ck.getName())) {
                    Uid = ck.getValue();
                }
            }
        }
        String[] whiteListArr = whiteList.split(",");
        List<String> result = Arrays.asList(whiteListArr);
        int count = 0;
        for (String item : result) {
            Pattern r = Pattern.compile(item);
            Matcher m = r.matcher(req.getRequestURI());
            while (m.find()) {
                count++;
            }
        }
        if (count == 0) {
            if (Uid != null) {
                log.warn("登录成功");
            } else {
                log.warn("跳转登录");
                String sourceUrl = req.getRequestURL().toString();
                if (!StringUtils.isEmpty(req.getQueryString())) {
                    sourceUrl = sourceUrl + "?" + req.getQueryString();
                }
                log.warn("sourceUrl:" + sourceUrl);
                String urlCopy = "";
                urlCopy = url + "?target=" + URLEncoder.encode(sourceUrl, "UTF-8") + "&appName=" + name;
                log.debug("urlCopy:" + urlCopy);
                res.sendRedirect(urlCopy);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}
