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

import java.util.Set;

import javax.validation.BootstrapConfiguration;
import javax.validation.Configuration;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;
import javax.validation.executable.ExecutableType;
import javax.ws.rs.ext.ContextResolver;

import org.hibernate.validator.messageinterpolation.ResourceBundleMessageInterpolator;
import org.hibernate.validator.resourceloading.PlatformResourceBundleLocator;
import org.hibernate.validator.spi.resourceloading.ResourceBundleLocator;
import org.jboss.resteasy.plugins.validation.GeneralValidatorImpl;
import org.jboss.resteasy.spi.validation.GeneralValidator;

class ValidatorContextResolver implements ContextResolver<GeneralValidator> {

    private final ValidatorFactory validatorFactory;

    ValidatorContextResolver(final ValidatorFactory validatorFactory) {
        this.validatorFactory = validatorFactory;
    }

    @Override
    public GeneralValidator getContext(final Class<?> type) {
        final ResourceBundleLocator resourceBundleLocator = new PlatformResourceBundleLocator("messages");
        final MessageInterpolator messageInterpolator = new ResourceBundleMessageInterpolator(resourceBundleLocator);
        final Configuration<?> config = Validation.byDefaultProvider().configure()
            .messageInterpolator(messageInterpolator);
        final BootstrapConfiguration bootstrapConfiguration = config.getBootstrapConfiguration();
        final boolean isExecutableValidationEnabled = bootstrapConfiguration.isExecutableValidationEnabled();
        final Set<ExecutableType> defaultValidatedExecutableTypes = bootstrapConfiguration
            .getDefaultValidatedExecutableTypes();

        return new GeneralValidatorImpl(validatorFactory, isExecutableValidationEnabled,
            defaultValidatedExecutableTypes);
    }
}
