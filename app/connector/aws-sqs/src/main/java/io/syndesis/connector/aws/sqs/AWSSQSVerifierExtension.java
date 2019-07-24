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
package io.syndesis.connector.aws.sqs;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.component.aws.sqs.SqsConfiguration;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorHelper;
import org.apache.camel.util.ObjectHelper;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import io.syndesis.connector.support.util.ConnectorOptions;

public class AWSSQSVerifierExtension extends DefaultComponentVerifierExtension {

    protected AWSSQSVerifierExtension(String defaultScheme, CamelContext context) {
        super(defaultScheme, context);
    }


    // *********************************
    // Parameters validation
    // *********************************

    @Override
    protected Result verifyParameters(Map<String, Object> parameters) {

        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.PARAMETERS).error(ResultErrorHelper.requiresOption("accessKey", parameters))
            .error(ResultErrorHelper.requiresOption("secretKey", parameters)).error(ResultErrorHelper.requiresOption("region", parameters));

        // Validate using the catalog

        if (ObjectHelper.isEmpty(ConnectorOptions.extractOption(parameters, "secretKey")) || ObjectHelper.isEmpty(ConnectorOptions.extractOption(parameters, "accessKey"))) {
            builder.error(ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.GENERIC,
                    "You must specify a secretKey and an accessKey").parameterKey("secretKey").parameterKey("accessKey").build());
        }

        return builder.build();
    }

    // *********************************
    // Connectivity validation
    // *********************************

    @Override
    @SuppressWarnings("PMD.AvoidCatchingGenericException")
    protected Result verifyConnectivity(Map<String, Object> parameters) {
        ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY);

        try {
            SqsConfiguration configuration = setProperties(new SqsConfiguration(), parameters);
            AWSCredentials credentials = new BasicAWSCredentials(configuration.getAccessKey(), configuration.getSecretKey());
            AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
            AmazonSQS client = AmazonSQSClientBuilder.standard().withCredentials(credentialsProvider).withRegion(Regions.valueOf(configuration.getRegion())).build();
            client.listQueues();
        } catch (SdkClientException e) {
            ResultErrorBuilder errorBuilder = ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION, e.getMessage())
                .detail("aws_sqs_exception_message", e.getMessage()).detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e);

            builder.error(errorBuilder.build());
        } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") Exception e) {
            builder.error(ResultErrorBuilder.withException(e).build());
        }
        return builder.build();
    }
}
