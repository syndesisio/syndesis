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
package io.syndesis.common.model.integration;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import io.syndesis.common.model.validation.AllValidations;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.junit.Test;

import io.syndesis.common.model.validation.UniquenessRequired;
import io.syndesis.common.model.validation.integration.NoDuplicateIntegration;

import static org.assertj.core.api.Assertions.assertThat;

public class IntegrationTest {

    private final Validator validator;

    public static class UniqnenessValidator implements ConstraintValidator<NoDuplicateIntegration, Object> {

        private static final AtomicBoolean VALID = new AtomicBoolean(true);

        @Override
        public void initialize(final NoDuplicateIntegration constraintAnnotation) {
            // nop
        }

        @Override
        public boolean isValid(final Object value, final ConstraintValidatorContext context) {
            return VALID.get();
        }
    }

    public IntegrationTest() {
        final DefaultConstraintMapping mapping = new DefaultConstraintMapping();
        mapping.constraintDefinition(NoDuplicateIntegration.class).validatedBy(UniqnenessValidator.class);
        final ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class).configure()
            .addMapping(mapping).buildValidatorFactory();

        validator = validatorFactory.getValidator();
    }

    @Test
    public void shouldNotifyOfUniquenessConstraintViolation() {
        UniqnenessValidator.VALID.set(false);

        final Set<ConstraintViolation<Integration>> violations = validator.validate(new Integration.Builder().build(),
            UniquenessRequired.class);

        assertThat(violations).hasSize(1);
    }

    @Test
    public void shouldValidateForNameUniqueness() {
        UniqnenessValidator.VALID.set(true);

        final Set<ConstraintViolation<Integration>> violations = validator.validate(new Integration.Builder().build(),
            UniquenessRequired.class);

        assertThat(violations).isEmpty();
    }

    @Test
    public void shouldNotifyOfNullNameViolation() {
        final Set<ConstraintViolation<Integration>> violations = validator.validate(new Integration.Builder().build(),
            AllValidations.class);

        assertThat(violations).hasSize(1);
    }

}
