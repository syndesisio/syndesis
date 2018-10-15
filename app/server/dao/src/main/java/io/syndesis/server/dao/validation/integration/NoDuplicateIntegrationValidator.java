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
package io.syndesis.server.dao.validation.integration;

import java.util.Set;
import java.util.stream.Collectors;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import io.syndesis.common.model.WithName;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.validation.integration.IntegrationWithDomain;
import io.syndesis.common.model.validation.integration.NoDuplicateIntegration;
import io.syndesis.server.dao.manager.DataManager;

public class NoDuplicateIntegrationValidator implements ConstraintValidator<NoDuplicateIntegration, Integration> {

    @Autowired
    private DataManager dataManager;

    @Override
    public void initialize(final NoDuplicateIntegration validIntegration) {
        // The annotation has no useful values
    }

    private boolean searchName(final String name, final Set<String> names, final ConstraintValidatorContext context) {
        final boolean exists = names.contains(name);
        if (exists) {
            context.disableDefaultConstraintViolation();
            context.unwrap(HibernateConstraintValidatorContext.class).addExpressionVariable("nonUnique", name)
                    .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("name").addConstraintViolation();
        }

        return !exists;
    }

    private boolean isValid(final IntegrationWithDomain value, final ConstraintValidatorContext context) {
        Integration target = value.getTarget();
        final String name = target.getName();
        if (name == null) {
            return true;
        }

        // name should be unique among all other draft and deployed integrations
        final Set<String> names = value.getDomain().stream()
                .filter(i -> !i.isDeleted() && !i.getId().equals(target.getId()))
                .map(WithName::getName)
                .collect(Collectors.toSet());

        return searchName(name, names, context);
    }

    @Override
    public boolean isValid(final Integration value, final ConstraintValidatorContext context) {
        if (value instanceof IntegrationWithDomain) {
            return isValid((IntegrationWithDomain) value, context);
        }

        final String name = value.getName();
        if (name == null) {
            return true;
        }

        // name should be unique among all other draft and deployed integrations
        final Set<String> names = dataManager.fetchAll(Integration.class).getItems().stream()
                        .filter(i -> !i.isDeleted() && !i.getId().equals(value.getId()))
                         .map(WithName::getName)
                         .collect(Collectors.toSet());
                names.addAll(dataManager.fetchAll(IntegrationDeployment.class).getItems().stream()
                        .filter(d -> !d.getSpec().isDeleted() && !d.getSpec().getId().equals(value.getId()))
                        .map(d -> d.getSpec().getName())
                        .collect(Collectors.toSet()));

       return searchName(name, names, context);
    }
}
