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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.web.authentication.AbstractAuthenticationTargetUrlRequestHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import com.quancheng.saluki.gateway.oauth2.service.DatabaseTokenStoreService;

/**
 * @author liushiming
 * @version CustomLogoutSuccessHandler.java, v 0.0.1 2017年8月17日 下午6:52:21 liushiming
 * @since JDK 1.8
 */
@Component
public class CustomLogoutSuccessHandler extends AbstractAuthenticationTargetUrlRequestHandler
    implements LogoutSuccessHandler {
  private static final String BEARER_AUTHENTICATION = "Bearer ";
  private static final String HEADER_AUTHORIZATION = "authorization";

  @Autowired
  private DatabaseTokenStoreService tokenStoreService;

  @Override
  public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    String token = request.getHeader(HEADER_AUTHORIZATION);

    if (token != null && token.startsWith(BEARER_AUTHENTICATION)) {

      OAuth2AccessToken oAuth2AccessToken = tokenStoreService.readAccessToken(token.split(" ")[0]);

      if (oAuth2AccessToken != null) {
        tokenStoreService.removeAccessToken(oAuth2AccessToken);
      }

    }

    response.setStatus(HttpServletResponse.SC_OK);

  }
}
