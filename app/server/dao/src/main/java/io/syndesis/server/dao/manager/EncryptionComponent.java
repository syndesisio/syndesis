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
package io.syndesis.server.dao.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import io.syndesis.common.model.connection.ConfigurationProperty;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.Step;

import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.stereotype.Component;

/**
 * Handy methods used to apply encryption to configured secrets.
 *
 */
@Component
public class EncryptionComponent {

    public static final String ENCRYPTED_PREFIX = "\u00BBENC:";

    private final TextEncryptor textEncryptor;

    public EncryptionComponent(TextEncryptor textEncryptor) {
        this.textEncryptor = textEncryptor;
    }

    public static String stripPrefix(String value, String prefix) {
        return value != null && value.startsWith(prefix)?value.substring(prefix.length()):value;
    }

    public String encrypt(final String value) {
        String result = value;
        if( !value.startsWith(ENCRYPTED_PREFIX) ) {
            result = ENCRYPTED_PREFIX+textEncryptor.encrypt(value);
        }
        return result;
    }

    public Map<String, String> encryptPropertyValues(Map<String, String> values, Map<String, ConfigurationProperty> properties) {
        final Map<String, String> result = new HashMap<>(values);
        if( textEncryptor!=null ) {
            // Let encrypt all the secrets
            for (Map.Entry<String, String> entry : values.entrySet()) {
                ConfigurationProperty property = properties.get(entry.getKey());
                if(property==null || !property.secret()) {
                    continue;
                }
                result.put(entry.getKey(), encrypt(entry.getValue()));
            }
        }
        return result;
    }

    public Integration encrypt(Integration integration) {
        return new Integration.Builder()
            .createFrom(integration)
            .flows(integration.getFlows()
                .stream()
                .map(f -> new Flow.Builder()
                    .createFrom(f)
                    .steps(encrypt(f.getSteps()))
                    .build())
                .collect(Collectors.toList()))
            .build();
    }

    public IntegrationDeployment encrypt(IntegrationDeployment integrationDeployment) {
        return new IntegrationDeployment.Builder()
            .createFrom(integrationDeployment)
            .spec(encrypt(integrationDeployment.getSpec()))
            .build();
    }

    public List<? extends Step> encrypt(List<? extends Step> steps) {
        return steps.stream().map(step -> {
            if(step.getAction().isPresent() ) {

                Map<String, String> configuredProperties = encryptPropertyValues(step.getConfiguredProperties(), step.getAction().get().getProperties());
                return new Step.Builder()
                    .createFrom(step)
                    .configuredProperties(configuredProperties)
                    .build();
            } else {
                return step;
            }
        }).collect(Collectors.toList());
    }

    public String decrypt(final String value) {
        // value might not be encrypted...
        if( value == null ) {
            return null;
        }
        String result = value;
        if( result.startsWith(ENCRYPTED_PREFIX) ) {
            try {
                result = textEncryptor.decrypt(stripPrefix(result, ENCRYPTED_PREFIX));
            } catch (@SuppressWarnings("PMD.AvoidCatchingGenericException") RuntimeException e) {
                // We could fail to decrypt the value..
                result = null;
            }
        }
        return result;
    }

    public Map<String, String> decrypt(Map<String, String> props) {
        if( props == null ) {
            return null;
        }
        HashMap<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> entry : props.entrySet()) {
            result.put(entry.getKey(), decrypt(entry.getValue()));
        }
        return result;
    }
}
