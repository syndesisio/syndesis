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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.GetQueueAttributesRequest;
import com.amazonaws.services.sqs.model.GetQueueAttributesResult;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.QueueAttributeName;
import io.syndesis.connector.support.util.ConnectorOptions;

public class AWSSQSMetaDataExtension extends AbstractMetaDataExtension {

    AWSSQSMetaDataExtension(CamelContext context) {
        super(context);
    }

    @Override
    public Optional<MetaData> meta(Map<String, Object> parameters) {
        final String accessKey = ConnectorOptions.extractOption(parameters, "accessKey");
        final String secretKey = ConnectorOptions.extractOption(parameters, "secretKey");
        final String region = ConnectorOptions.extractOption(parameters, "region");
        AmazonSQSClientBuilder clientBuilder;
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
        clientBuilder = AmazonSQSClientBuilder.standard().withCredentials(credentialsProvider);
        clientBuilder = clientBuilder.withRegion(Regions.valueOf(region));
        AmazonSQS sqsClient = clientBuilder.build();
        List<String> attributeNames = new ArrayList<String>();
        attributeNames.add("All");
        try {
            ListQueuesResult result = sqsClient.listQueues();
            Set<String> setQueue = new HashSet<String>();
            if (result.getQueueUrls() != null) {
                for (String entry : result.getQueueUrls()) {
                    GetQueueAttributesRequest req = new GetQueueAttributesRequest();
                    req.setQueueUrl(entry);
                    req.setAttributeNames(attributeNames);
                    GetQueueAttributesResult c = sqsClient.getQueueAttributes(req);
                    setQueue.add(c.getAttributes().get(QueueAttributeName.QueueArn.name()));
                }
            }
            return Optional.of(MetaDataBuilder.on(getCamelContext()).withAttribute(MetaData.CONTENT_TYPE, "text/plain").withAttribute(MetaData.JAVA_TYPE, String.class)
                .withPayload(setQueue).build());
        } catch (Exception e) {
            throw new IllegalStateException("Get information about existing queues with has failed.", e);
        }
    }
}
