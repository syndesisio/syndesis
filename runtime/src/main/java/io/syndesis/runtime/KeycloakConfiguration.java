/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.runtime;

import java.util.List;

import javax.servlet.http.HttpSessionEvent;

import org.keycloak.adapters.springsecurity.KeycloakSecurityComponents;
import org.keycloak.adapters.springsecurity.config.KeycloakWebSecurityConfigurerAdapter;
import org.keycloak.adapters.springsecurity.filter.KeycloakAuthenticationProcessingFilter;
import org.keycloak.adapters.springsecurity.filter.KeycloakPreAuthActionsFilter;
import org.keycloak.adapters.springsecurity.management.HttpSessionManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.security.web.authentication.preauth.x509.X509AuthenticationFilter;
import org.springframework.security.web.authentication.session.NullAuthenticatedSessionStrategy;
import org.springframework.security.web.authentication.session.SessionAuthenticationStrategy;

@Configuration
@EnableWebSecurity
@ComponentScan(basePackageClasses = KeycloakSecurityComponents.class)
@ConditionalOnProperty("keycloak.enabled")
public class KeycloakConfiguration extends KeycloakWebSecurityConfigurerAdapter {

    // the default HttpSessionManager tries to access HttpSession, when used with Infinispan spring-session support
    // the session is `null`, see AbstractApplicationPublisherBridge::emitSessionCreatedEvent
    static class NopHttpSessionManager extends HttpSessionManager {

        public static final HttpSessionManager INSTANCE = new NopHttpSessionManager();

        @Override
        public void logoutAll() {
            // nop
        }

        @Override
        public void logoutHttpSessions(final List<String> ids) {
            // nop
        }

        @Override
        public void sessionCreated(final HttpSessionEvent event) {
            // nop
        }

        @Override
        public void sessionDestroyed(final HttpSessionEvent event) {
            // nop
        }
    }

    @Autowired
    public void configureGlobal(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(keycloakAuthenticationProvider());
    }

    @Bean
    @Override
    protected SessionAuthenticationStrategy sessionAuthenticationStrategy() {
        return new NullAuthenticatedSessionStrategy();
    }

    @Bean
    public FilterRegistrationBean keycloakAuthenticationProcessingFilterRegistrationBean(KeycloakAuthenticationProcessingFilter filter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Bean
    public FilterRegistrationBean keycloakPreAuthActionsFilterRegistrationBean(KeycloakPreAuthActionsFilter filter) {
        FilterRegistrationBean registrationBean = new FilterRegistrationBean(filter);
        registrationBean.setEnabled(false);
        return registrationBean;
    }

    @Override
    protected HttpSessionManager httpSessionManager() {
        return NopHttpSessionManager.INSTANCE;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .sessionAuthenticationStrategy(sessionAuthenticationStrategy()).and()
            .addFilterBefore(keycloakPreAuthActionsFilter(), LogoutFilter.class)
            .addFilterBefore(keycloakAuthenticationProcessingFilter(), X509AuthenticationFilter.class)
            .exceptionHandling().authenticationEntryPoint(authenticationEntryPoint()).and().authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS).permitAll()
            .antMatchers("/api/v1/swagger.*").permitAll()
            .antMatchers(HttpMethod.GET, "/api/v1/credentials/callback").permitAll()
            .antMatchers("/api/v1/**").authenticated()
            .anyRequest().permitAll();

        http.csrf().disable();
    }

}
