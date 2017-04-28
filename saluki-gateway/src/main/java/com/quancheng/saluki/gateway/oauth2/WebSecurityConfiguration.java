package com.quancheng.saluki.gateway.oauth2;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationTrustResolver;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@EnableJpaAuditing
@EnableWebSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http//
            .exceptionHandling()//
            .accessDeniedPage("/login.html?authorization_error=true")//
            .and()//
            .logout()//
            .permitAll()//
            .and()//
            .formLogin()//
            .loginPage("/login.html")//
            .permitAll()//
            .and()//
            .authorizeRequests()//
            .anyRequest().authenticated();//
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Autowired
    public AuditorAware<String> auditorAwareBean(AuthenticationTrustResolver authenticationTrustResolver) {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || authenticationTrustResolver.isAnonymous(authentication)) {
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
}
