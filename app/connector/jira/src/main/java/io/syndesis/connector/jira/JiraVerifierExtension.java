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
package io.syndesis.connector.jira;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.RestClientException;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.camel.component.jira.JiraConfiguration;
import org.apache.camel.component.jira.oauth.JiraOAuthAuthenticationHandler;
import org.apache.camel.component.jira.oauth.OAuthAsynchronousJiraRestClientFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.component.jira.JiraConstants.PRIVATE_KEY;

public class JiraVerifierExtension extends DefaultComponentVerifierExtension {

    private static final Logger LOG = LoggerFactory.getLogger(JiraVerifierExtension.class);

    JiraVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS)
            .error(ResultErrorHelper.requiresOption("jiraUrl", parameters));
        return builder.build();
    }

    @Override
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY);

        JiraRestClient client = null;
        try {
            String privateKey = ConnectorOptions.extractOption(parameters, PRIVATE_KEY);
            boolean isRawEnvloped = privateKey != null && privateKey.length() > 4 && "RAW(".equals(privateKey.substring(0, 4));
            if (isRawEnvloped) {
                // remove the RAW envelope
                privateKey = privateKey.substring(4, privateKey.length() - 1);
            }
            parameters.put(PRIVATE_KEY, privateKey);
            JiraConfiguration conf = setProperties(new JiraConfiguration(), parameters);
            OAuthAsynchronousJiraRestClientFactory factory = new OAuthAsynchronousJiraRestClientFactory();

            final URI jiraServerUri = URI.create(conf.getJiraUrl());
            boolean useUserPasswd = StringUtils.isNotBlank(conf.getUsername()) && StringUtils.isNotBlank(conf.getPassword());
            boolean useOAuth = StringUtils.isNotBlank(conf.getAccessToken()) && StringUtils.isNotBlank(conf.getVerificationCode())
                && StringUtils.isNotBlank(conf.getConsumerKey()) && StringUtils.isNotBlank(conf.getPrivateKey());
            if (useUserPasswd) {
                client = factory.createWithBasicHttpAuthentication(jiraServerUri, conf.getUsername(), conf.getPassword());
            } else if (useOAuth){
                JiraOAuthAuthenticationHandler oAuthHandler = new JiraOAuthAuthenticationHandler(conf.getConsumerKey(), conf.getVerificationCode(),
                    conf.getPrivateKey(), conf.getAccessToken(), conf.getJiraUrl());
                client = factory.create(jiraServerUri, oAuthHandler);
            }
            if (useOAuth || useUserPasswd) {
                // test the connection to the jira server
                ServerInfo serverInfo = client.getMetadataClient().getServerInfo().claim();
                LOG.info("Verify connectivity to jira server OK: {}, {}, {}", serverInfo.getServerTitle(), serverInfo.getVersion(), serverInfo.getBaseUri());
            } else {
                ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION,
                    "There are missing parameters. Set either the username/password or the OAuth parameters.");
                builder.error(errorBuilder.build());
            }

        } catch (RestClientException e) {
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, e.getMessage())
                .detail("jira_exception_message", e.getMessage())
                .detail("jira_status_code", e.getStatusCode())
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e);

            builder.error(errorBuilder.build());
        } catch (Exception e) {
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, e.getMessage())
                .detail("jira_exception_message", e.getMessage())
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e);

            builder.error(errorBuilder.build());
        } finally {
            if (client != null) {
                try {
                    client.close();
                } catch (IOException e) {
                    LOG.warn("Error trying to close the JiraClient connection.", e);
                }
            }
        }
        return builder.build();
    }

}
