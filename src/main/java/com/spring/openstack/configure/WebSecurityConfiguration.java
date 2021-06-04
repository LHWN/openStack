package com.spring.openstack.configure;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableGlobalMethodSecurity
public class WebSecurityConfiguration extends WebSecurityConfigurerAdapter {
    // webignore 통해 인증하지 않고도 접근 가능하도록 무시

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .antMatchers("/i18n/**")
                .antMatchers("/static/**")
                .antMatchers("/css/**")
                .antMatchers("/js/**")
                .antMatchers("/images/**");
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.headers().frameOptions().sameOrigin()
                .and().formLogin().loginPage("/login")
                .and().logout().logoutUrl("/logout")
                .and().authorizeRequests().antMatchers("/login", "/").permitAll()
                .and().authorizeRequests().anyRequest().authenticated();
        http.csrf().disable();
    }

    // login 경로로 접근하면 로그인 페이지 나오게
    // /loing 과 / root 경로는 모두 접근 가능하게
    // 그외의 모든 request 는 인증해야함
}
