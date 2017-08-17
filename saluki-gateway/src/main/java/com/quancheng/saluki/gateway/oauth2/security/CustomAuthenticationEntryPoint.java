/*
 * Copyright 2014-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.quancheng.saluki.gateway.oauth2.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 * @author liushiming
 * @version CustomAuthenticationEntryPoint.java, v 0.0.1 2017年8月17日 下午6:51:16 liushiming
 * @since JDK 1.8
 */
@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {
  private final Logger log = LoggerFactory.getLogger(CustomAuthenticationEntryPoint.class);

  public void commence(HttpServletRequest request, HttpServletResponse response,
      AuthenticationException ae) throws IOException, ServletException {

    log.info("Pre-authenticated entry point called. Rejecting access");
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Access Denied");

  }
}
