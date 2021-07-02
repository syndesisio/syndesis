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

import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import io.syndesis.common.model.validation.AllValidations;
import io.syndesis.common.model.validation.UniquenessRequired;
import io.syndesis.common.model.validation.integration.NoDuplicateIntegration;
import io.syndesis.common.model.validation.integration.ValidLabels;
import io.syndesis.common.util.Labels;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.junit.jupiter.api.Test;

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

    public static class MockLabelValidator implements ConstraintValidator<ValidLabels, Integration> {

        @Override
        public void initialize(final ValidLabels constraintAnnotation) {
            // nop
        }

        @Override
        public boolean isValid(final Integration integration, final ConstraintValidatorContext context) {
            boolean valid = true;
            for (Map.Entry<String, String> entry: integration.getLabels().entrySet()){
                if (!Labels.isValidKey(entry.getKey()) || !Labels.isValid(entry.getValue())) {
                    valid = false;
                    break;
                }
            }

            return valid;

        }
    }

    public IntegrationTest() {
        final DefaultConstraintMapping mapping = new DefaultConstraintMapping();
        mapping.constraintDefinition(NoDuplicateIntegration.class).validatedBy(UniqnenessValidator.class);
        mapping.constraintDefinition(ValidLabels.class).validatedBy(MockLabelValidator.class);
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

    @Test
    public void shouldValidateLabels() {
        UniqnenessValidator.VALID.set(true);
        final Set<ConstraintViolation<Integration>> violations = validator.validate(new Integration.Builder().name("test1").putLabel("hello", "world").build(),
            AllValidations.class);

        assertThat(violations).isEmpty();
    }

    @Test
    public void shouldNotifyInvalidKeyLabel() {
        UniqnenessValidator.VALID.set(true);
        final Set<ConstraintViolation<Integration>> violations = validator.validate(new Integration.Builder().name("test2").putLabel("syndesis.io/fail", "fail").build(),
            AllValidations.class);
        assertThat(violations).hasSize(1);
    }

    @Test
    public void shouldNotifyInvalidValueLabel() {
        UniqnenessValidator.VALID.set(true);
        final Set<ConstraintViolation<Integration>> violations = validator.validate(new Integration.Builder().name("test3").putLabel("fail", "12345678901234567890123456789012345678901234567890" +
                "12345678901234567890123456789012345678901234567890").build(),
            AllValidations.class);
        assertThat(violations).hasSize(1);
    }
}
