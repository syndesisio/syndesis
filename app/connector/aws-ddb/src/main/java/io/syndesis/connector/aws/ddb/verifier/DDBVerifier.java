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
package io.syndesis.connector.aws.ddb.verifier;

import java.util.Map;
import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.model.ListTablesResult;
import com.amazonaws.util.StringUtils;
import io.syndesis.connector.support.verifier.api.ComponentVerifier;
import org.apache.camel.CamelContext;
import org.apache.camel.component.aws.ddb.DdbComponentVerifierExtension;
import org.apache.camel.component.aws.ddb.DdbConfiguration;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;

/**
 * Factory to create the verifiers for the connection.
 *
 * @author delawen
 */
public class DDBVerifier extends ComponentVerifier {
    public DDBVerifier() {
        super("aws-ddb", DdbComponentVerifierExtension.class);
    }

    @Override
    protected DdbComponentVerifierExtension resolveComponentVerifierExtension(CamelContext context, String scheme) {
        DdbComponentVerifierExtension ext = new DdbComponentVerifierExtension(scheme) {

            @Override
            //Extending here to verify table name
            //Following versions of camel don't require this, but current version does
            protected Result verifyConnectivity(Map<String, Object> parameters) {
                ResultBuilder builder = ResultBuilder.withStatusAndScope(Result.Status.OK, Scope.CONNECTIVITY);

                try {
                    DdbConfiguration configuration = setProperties(new DdbConfiguration(), parameters);
                    AWSCredentials credentials = new BasicAWSCredentials(configuration.getAccessKey(),
                        configuration.getSecretKey());
                    AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(credentials);
                    AmazonDynamoDB client =
                        AmazonDynamoDBClientBuilder.standard().withCredentials(credentialsProvider).withRegion(Regions.valueOf(configuration.getRegion())).build();
                    ListTablesResult tables = client.listTables();
                    String table = configuration.getTableName();
                    boolean tableExists = !StringUtils.isNullOrEmpty(table) &&
                        tables.getTableNames().parallelStream().filter(t -> table.equalsIgnoreCase(t)).count() > 0;

                    if (!tableExists) {
                        ResultErrorBuilder errorBuilder =
                            ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.ILLEGAL_PARAMETER,
                                "Table does not exist. Check table name and region.")
                                .detail("aws_ddb_exception_message", "Table does not exist. Check table name and " +
                                    "region.");
                        builder.error(errorBuilder.build());
                    }
                } catch (SdkClientException e) {
                    ResultErrorBuilder errorBuilder =
                        ResultErrorBuilder.withCodeAndDescription(VerificationError.StandardCode.AUTHENTICATION,
                            e.getMessage())
                            .detail("aws_ddb_exception_message", e.getMessage()).detail(VerificationError.ExceptionAttribute.EXCEPTION_CLASS, e.getClass().getName())
                            .detail(VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE, e);

                    builder.error(errorBuilder.build());
                } catch (Exception e) {
                    builder.error(ResultErrorBuilder.withException(e).build());
                }
                return builder.build();
            }
        };
        ext.setCamelContext(context);
        return ext;
    }
}
