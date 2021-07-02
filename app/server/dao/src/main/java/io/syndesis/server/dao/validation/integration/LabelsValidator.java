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

import java.util.Map;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.validation.integration.ValidLabels;
import io.syndesis.common.util.Labels;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

public class LabelsValidator implements ConstraintValidator<ValidLabels, Integration> {

    @Override
    public void initialize(final ValidLabels validLabels) {
        // The annotation has no useful values
    }

    @Override
    public boolean isValid(final Integration integration, final ConstraintValidatorContext context) {
        boolean valid = true;
        for (Map.Entry<String, String> entry: integration.getLabels().entrySet()){
            if (!Labels.isValidKey(entry.getKey()) || !Labels.isValid(entry.getValue())) {
                valid = false;

                context.disableDefaultConstraintViolation();
                context.unwrap(HibernateConstraintValidatorContext.class)
                    .addExpressionVariable("key", entry.getKey())
                    .addExpressionVariable("value", entry.getValue())
                    .buildConstraintViolationWithTemplate(context.getDefaultConstraintMessageTemplate())
                    .addPropertyNode("labels")
                    .addConstraintViolation();

                break;
            }
        }

        return valid;
    }

}
