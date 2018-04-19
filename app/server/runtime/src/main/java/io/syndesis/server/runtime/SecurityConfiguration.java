/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.server.runtime;

import javax.servlet.http.HttpServletRequest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationProvider;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesUserDetailsService;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails;
import org.springframework.security.web.authentication.preauth.RequestHeaderAuthenticationFilter;

@Profile("!development")
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String[] COMMON_NON_SECURED_PATHS = {
        "/api/v1/swagger.*",
        "/api/v1/index.html",
        "/api/v1/internal/swagger.*",
        "/api/v1/internal/index.html",
        "/api/v1/version",
        "/health"
    };

    @Override
    protected void configure(AuthenticationManagerBuilder authenticationManagerBuilder) {
        authenticationManagerBuilder.authenticationProvider(authenticationProvider());
    }

    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    protected void configure(HttpSecurity http) throws Exception {
        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .addFilter(requestHeaderAuthenticationFilter())
            .addFilter(new AnonymousAuthenticationFilter("anonymous"))
            .authorizeRequests()
            .antMatchers(HttpMethod.OPTIONS).permitAll()
            .antMatchers(COMMON_NON_SECURED_PATHS).permitAll()
            .antMatchers(HttpMethod.GET, "/api/v1/credentials/callback").permitAll()
            .antMatchers("/api/v1/**").hasRole("AUTHENTICATED")
            .anyRequest().permitAll();

        http.csrf()
            .ignoringAntMatchers(COMMON_NON_SECURED_PATHS)
            .ignoringAntMatchers("/api/v1/credentials/callback")
            .ignoringAntMatchers("/api/v1/atlas/**")
            .csrfTokenRepository(new SyndesisCsrfRepository());
    }

    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    private RequestHeaderAuthenticationFilter requestHeaderAuthenticationFilter() throws Exception {
        RequestHeaderAuthenticationFilter f = new RequestHeaderAuthenticationFilter();
        f.setPrincipalRequestHeader("X-Forwarded-User");
        f.setCredentialsRequestHeader("X-Forwarded-Access-Token");
        f.setAuthenticationManager(authenticationManager());
        f.setAuthenticationDetailsSource(
            (AuthenticationDetailsSource<HttpServletRequest, PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails>)
                (request) ->new PreAuthenticatedGrantedAuthoritiesWebAuthenticationDetails(
                    request,
                    AuthorityUtils.createAuthorityList("ROLE_AUTHENTICATED")
                )
        );
        f.setAuthenticationFailureHandler(new SimpleUrlAuthenticationFailureHandler());
        f.setExceptionIfHeaderMissing(false);
        return f;
    }

    private AuthenticationProvider authenticationProvider() {
        PreAuthenticatedAuthenticationProvider authProvider = new PreAuthenticatedAuthenticationProvider();
        authProvider.setPreAuthenticatedUserDetailsService(new PreAuthenticatedGrantedAuthoritiesUserDetailsService());
        return authProvider;
    }

    @Bean()
    @Override
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }
}
