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
package io.syndesis.server.dao.validation;

import java.util.Arrays;
import java.util.HashSet;

import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder;
import javax.validation.ConstraintValidatorContext.ConstraintViolationBuilder.NodeBuilderCustomizableContext;

import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.validation.UniqueProperty;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(Parameterized.class)
public class UniquePropertyValidatorTest {

    @Parameter(0)
    public WithId<?> connection;

    @Parameter(1)
    public boolean validity;

    private final UniquePropertyValidator validator = new UniquePropertyValidator();

    public UniquePropertyValidatorTest() {
        validator.initialize(Connection.class.getAnnotation(UniqueProperty.class));
    }

    @Before
    public void setupMocks() {
        validator.dataManager = mock(DataManager.class);

        when(validator.dataManager.fetchIdsByPropertyValue(Connection.class, "name", "Existing"))
            .thenReturn(new HashSet<>(Arrays.asList("same")));
        when(validator.dataManager.fetchIdsByPropertyValue(Integration.class, "name", "Existing"))
            .thenReturn(new HashSet<>(Arrays.asList("deleted")));
        when(validator.dataManager.fetch(Integration.class, "deleted"))
            .thenReturn(new Integration.Builder().name("Existing").id("deleted").isDeleted(true).build());
    }

    @Test
    public void shouldAscertainPropertyUniqueness() {
        final HibernateConstraintValidatorContext context = mock(HibernateConstraintValidatorContext.class);
        when(context.unwrap(HibernateConstraintValidatorContext.class)).thenReturn(context);
        when(context.addExpressionVariable(eq("nonUnique"), anyString())).thenReturn(context);
        when(context.getDefaultConstraintMessageTemplate()).thenReturn("template");
        final ConstraintViolationBuilder builder = mock(ConstraintViolationBuilder.class);
        when(context.buildConstraintViolationWithTemplate("template")).thenReturn(builder);
        when(builder.addPropertyNode(anyString())).thenReturn(mock(NodeBuilderCustomizableContext.class));

        assertThat(validator.isValid(connection, context)).isEqualTo(validity);
    }

    @Parameters
    public static Iterable<Object[]> parameters() {

        final Connection existingNameNoId = new Connection.Builder().name("Existing").build();

        final Connection existingNameWithSameId = new Connection.Builder().name("Existing").id("same").build();

        final Connection existingNameWithDifferentId = new Connection.Builder().name("Existing").id("different")
            .build();

        final Connection uniqueNameNoId = new Connection.Builder().name("Unique").build();

        final Integration existingButDeleted = new Integration.Builder().name("Existing").id("different").build();

        return Arrays.asList(//
            new Object[] {existingNameNoId, false}, //
            new Object[] {existingNameWithSameId, true}, //
            new Object[] {existingNameWithDifferentId, false}, //
            new Object[] {uniqueNameNoId, true}, //
            new Object[] {existingButDeleted, true}//
        );
    }
}
