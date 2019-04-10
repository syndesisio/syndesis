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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorFactory;
import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.internal.cfg.context.DefaultConstraintMapping;
import org.hibernate.validator.internal.constraintvalidators.bv.NotNullValidator;
import org.junit.Test;
import org.mockito.Mock;
import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.action.Action.Pattern;
import io.syndesis.common.model.action.ConnectorAction;
import io.syndesis.common.model.connection.Connection;
import io.syndesis.common.model.connection.Connector;
import io.syndesis.common.model.integration.Flow;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.common.model.integration.IntegrationDeployment;
import io.syndesis.common.model.integration.Step;
import io.syndesis.common.model.validation.AllValidations;
import io.syndesis.common.model.validation.extension.NoDuplicateExtension;
import io.syndesis.common.model.validation.integration.IntegrationWithDomain;
import io.syndesis.common.model.validation.integration.NoDuplicateIntegration;
import io.syndesis.common.util.StringConstants;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.validation.extension.NoDuplicateExtensionValidator;
import io.syndesis.server.dao.validation.integration.NoDuplicateIntegrationValidator;

public class NoDuplicateValidatorTest implements StringConstants {

    private static final String SQL_CONNECTOR_ACTION_ID = "sql-connector";

    private static final String CONNECTION_ID = "5";

    private static final String CONNECTOR_ID = "sql";

    private static final String SQL_CONNECTOR_NAME = "SQL Connector";

    private static final long INTEGRATION_CREATED_AT = 1533024000000L;  // 31/07/2018 09:00:00

    private static final long INTEGRATION_UPDATED_AT = 1533024600000L; // 31/07/2018 09:10:00

    @Mock
    private final DataManager dataManager = mock(DataManager.class);

    private Validator validator;

    private class InjectionConstraintValidatorFactory implements ConstraintValidatorFactory {

        private NoDuplicateIntegrationValidator noDupIntValidator = new NoDuplicateIntegrationValidator();

        private NoDuplicateExtensionValidator noDupExtValidator = new NoDuplicateExtensionValidator();

        public InjectionConstraintValidatorFactory(DataManager dataManager) {
            setDataManager(noDupIntValidator, dataManager);
            setDataManager(noDupExtValidator, dataManager);
        }

        private void setDataManager(Object target, DataManager dataManager) {
            try {
                Field privateField = target.getClass().getDeclaredField("dataManager");
                privateField.setAccessible(true);
                privateField.set(target, dataManager);
            } catch(Exception e){
                throw new RuntimeException(e);
            }
        }

        @SuppressWarnings( "unchecked" )
        @Override
        public <T extends ConstraintValidator<?, ?>> T getInstance(Class<T> key) {
            if (key == NotNullValidator.class) {
                return (T) new NotNullValidator();
            } else if (key == NoDuplicateIntegrationValidator.class) {
                return (T) noDupIntValidator;
            } else if (key == NoDuplicateExtensionValidator.class) {
                return (T) noDupExtValidator;
            }

            throw new UnsupportedOperationException();
        }

        @Override
        public void releaseInstance(ConstraintValidator<?, ?> instance) {
            // Nothing to do
        }
    }

    private InjectionConstraintValidatorFactory constraintValidatorFactory = new InjectionConstraintValidatorFactory(dataManager);

    public NoDuplicateValidatorTest() {

        final DefaultConstraintMapping mapping = new DefaultConstraintMapping();
        mapping.constraintDefinition(NoDuplicateExtension.class).validatedBy(NoDuplicateExtensionValidator.class);
        mapping.constraintDefinition(NoDuplicateIntegration.class).validatedBy(NoDuplicateIntegrationValidator.class);

        final ValidatorFactory validatorFactory = Validation.byProvider(HibernateValidator.class).configure()
            .constraintValidatorFactory(constraintValidatorFactory)
            .addMapping(mapping).buildValidatorFactory();

        validator = validatorFactory.getValidator();

    }

    private Connector newSqlConnector() {
        ConnectorAction action1 = new ConnectorAction.Builder()
            .id(SQL_CONNECTOR_ACTION_ID)
            .actionType("connector")
            .description("Invoke SQL to obtain ...")
            .name("Invoke SQL")
            .addTag("dynamic")
            .pattern(Pattern.To)
            .build();

        return new Connector.Builder()
           .id(CONNECTOR_ID)
           .name(SQL_CONNECTOR_NAME)
           .addAction(action1)
           .build();
    }

    private Connection newSqlConnection(Connector connector) {
        assertNotNull(connector);

        Map<String, String> configuredProperties = new HashMap<>();
        configuredProperties.put("password", "password");
        configuredProperties.put("user", "developer");
        configuredProperties.put("schema", "sampledb");
        configuredProperties.put("url",  "jdbc:postgresql://syndesis-db:5432/sampledb");

        return new Connection.Builder()
            .id(CONNECTION_ID)
            .addTag("dynamic")
            .configuredProperties(configuredProperties)
            .connector(connector)
            .connectorId("sql")
            .description("Connection to Sampledb")
            .icon("fa-database")
            .name("PostgresDB")
            .build();
    }

    private Step newSqlStep(Connection connection) {
        ConnectorAction action = new ConnectorAction.Builder()
            .actionType("connector")
            .id(SQL_CONNECTOR_ACTION_ID)
            .name("Invoke SQL")
            .pattern(Pattern.To)
            .addTag("dynamic")
            .build();

        return new Step.Builder()
            .connection(connection)
            .id("SomeLongId")
            .action(action)
            .build();
    }

    private Integration newSqlIntegration(String id, Connection connection) {
        return new Integration.Builder()
            .id(id)
            .name(id)
            .addFlow(new Flow.Builder().id(id + ":flow").addStep(newSqlStep(connection)).build())
            .createdAt(INTEGRATION_CREATED_AT)
            .updatedAt(INTEGRATION_UPDATED_AT)
            .build();
    }

    /**
     * Tests an integration can be validated either using a domain or without.
     */
    @Test
    public void shouldValidateSameWayWithOrWithoutDomain() {
        String idPrefix = "MyTestIntegration-x123456";
        Connector sqlConnector = newSqlConnector();
        Connection sqlConnection = newSqlConnection(sqlConnector);

        List<Integration> integrations = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            String id = idPrefix + HYPHEN + i;
            integrations.add(newSqlIntegration(id, sqlConnection));
        }

        Integration theIntegration = new Integration.Builder()
            .createFrom(integrations.get(0))
            .description("I am a copy")
            .id(integrations.get(0).getId().get() + "new")
            .build();

        when(dataManager.fetchAll(Integration.class))
            .thenReturn(new ListResult.Builder<Integration>().addAllItems(integrations).build());

        List<IntegrationDeployment> emptyList = Collections.emptyList();
        when(dataManager.fetchAll(IntegrationDeployment.class))
            .thenReturn(new ListResult.Builder<IntegrationDeployment>().addAllItems(emptyList).build());

        Set<ConstraintViolation<Integration>> violations = validator.validate(theIntegration, AllValidations.class);
        assertEquals(1, violations.size());
        ConstraintViolation<Integration> violation = violations.iterator().next();

        IntegrationWithDomain target = new IntegrationWithDomain(theIntegration, integrations);
        Set<ConstraintViolation<IntegrationWithDomain>> domainViolations = validator.validate(target, AllValidations.class);
        assertEquals(1, domainViolations.size());
        ConstraintViolation<IntegrationWithDomain> domainViolation = domainViolations.iterator().next();

        assertEquals(violation.getMessage(), domainViolation.getMessage());
        assertEquals(violation.getPropertyPath(), domainViolation.getPropertyPath());
    }
}
