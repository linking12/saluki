package com.quancheng.saluki.gateway.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationTrustResolverImpl;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.expression.OAuth2MethodSecurityExpressionHandler;

import com.quancheng.saluki.gateway.oauth2.security.CustomLogoutSuccessHandler;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

  @Autowired
  private CustomLogoutSuccessHandler customLogoutSuccessHandler;

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http//
        .exceptionHandling()//
        .accessDeniedPage("/login.html?authorization_error=true")//
        .and()//
        .logout()//
        .logoutSuccessHandler(customLogoutSuccessHandler)//
        .permitAll()//
        .and()//
        .formLogin()//
        .loginPage("/login.html")//
        .permitAll();//
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public AuditorAware<String> auditorAwareBean() {
    return () -> {
      Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
      if (authentication == null
          || new AuthenticationTrustResolverImpl().isAnonymous(authentication)) {
        return "@SYSTEM";
      }

      Object principal = authentication.getPrincipal();
      if (principal instanceof String) {
        return (String) principal;
      } else if (principal instanceof UserDetails) {
        return ((UserDetails) principal).getUsername();
      } else {
        return String.valueOf(principal);
      }
    };
  }


  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @EnableGlobalMethodSecurity(prePostEnabled = true, jsr250Enabled = true)
  protected static class GlobalSecurityConfiguration extends GlobalMethodSecurityConfiguration {


    @Override
    protected MethodSecurityExpressionHandler createExpressionHandler() {
      return new OAuth2MethodSecurityExpressionHandler();
    }

  }
}
