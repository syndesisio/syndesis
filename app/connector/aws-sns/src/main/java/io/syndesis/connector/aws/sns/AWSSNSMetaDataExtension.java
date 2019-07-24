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
package io.syndesis.connector.aws.sns;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.ListTopicsResult;
import com.amazonaws.services.sns.model.Topic;
import io.syndesis.connector.support.util.ConnectorOptions;

public class AWSSNSMetaDataExtension extends AbstractMetaDataExtension {

	AWSSNSMetaDataExtension(CamelContext context) {
        super(context);
    }

    @Override
    public Optional<MetaData> meta(Map<String, Object> parameters) {
        final String accessKey = ConnectorOptions.extractOption(parameters, "accessKey");
        final String secretKey = ConnectorOptions.extractOption(parameters, "secretKey");
        final String region = ConnectorOptions.extractOption(parameters, "region");
        AmazonSNSClientBuilder clientBuilder;
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        clientBuilder = AmazonSNSClientBuilder.standard().withCredentials(credentialsProvider);
        clientBuilder = clientBuilder.withRegion(Regions.valueOf(region));
        AmazonSNS sqsClient = clientBuilder.build();
        try {
            ListTopicsResult result = sqsClient.listTopics();
            Set<String> setTopic = new HashSet<String>();
            if (result.getTopics() != null) {
                for (Topic entry : result.getTopics()) {
                	setTopic.add(entry.getTopicArn());
                }
            }
            return Optional.of(MetaDataBuilder.on(getCamelContext()).withAttribute(MetaData.CONTENT_TYPE, "text/plain").withAttribute(MetaData.JAVA_TYPE, String.class)
                .withPayload(setTopic).build());
        } catch (Exception e) {
            throw new IllegalStateException("Get information about existing topics with has failed.", e);
        }
    }
}
