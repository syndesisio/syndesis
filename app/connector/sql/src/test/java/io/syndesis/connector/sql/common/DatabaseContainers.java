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
package io.syndesis.connector.sql.common;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class DatabaseContainers implements TestTemplateInvocationContextProvider {

    private static final Namespace NAMESPACE = Namespace.create(DatabaseContainers.class);

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Database {
        /**
         * Container image name, from which the appropriate test container is
         * deduced.
         */
        String[] value();
    }

    private static final class ContainerInvocationContext implements TestTemplateInvocationContext {
        private final String imageName;

        private ContainerInvocationContext(final String imageName) {
            this.imageName = imageName;
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
            final BeforeEachCallback beforeEachCallback = (BeforeEachCallback) context -> container(context, imageName).start();
            final AfterAllCallback afterAllCallback = (AfterAllCallback) context -> container(context, imageName).stop();
            final ParameterResolver parameterResolver = new ContainerParameterResolver(imageName);

            return Arrays.asList(beforeEachCallback, afterAllCallback, parameterResolver);
        }

        @Override
        public String getDisplayName(final int invocationIndex) {
            return imageName;
        }
    }

    private static JdbcDatabaseContainer<?> container(final ExtensionContext context, String imageName) {
        final Store store = context.getRoot().getStore(NAMESPACE);
        return store.getOrComputeIfAbsent(imageName, DatabaseContainers::createContainer,
            JdbcDatabaseContainer.class);
    }

    private static final class ContainerParameterResolver implements ParameterResolver {
        private final String imageName;

        private ContainerParameterResolver(final String imageName) {
            this.imageName = imageName;
        }

        @Override
        public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext context) {
            return container(context, imageName);
        }

        @Override
        public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext context) {
            return parameterContext.getParameter().getType().equals(JdbcDatabaseContainer.class);
        }
    }

    @Override
    public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(final ExtensionContext context) {
        final Database[] databases = context.getRequiredTestClass().getAnnotationsByType(Database.class);

        return Stream.of(databases)
            .flatMap(d -> Stream.of(d.value()))
            .map(DatabaseContainers::createTemplate);
    }

    @Override
    public boolean supportsTestTemplate(final ExtensionContext context) {
        return context.getRequiredTestClass().isAnnotationPresent(Database.class);
    }

    private static JdbcDatabaseContainer<?> createContainer(final String imageName) {
        final String nameWithoutTag = imageName.replaceFirst(":.*", "");

        switch (nameWithoutTag) {
        case "postgres":
            return new PostgreSQLContainer<>(imageName);
        case "mariadb":
            return new MariaDBContainer<>(imageName);
        case "mysql":
            return new MySQLContainer<>(imageName);
        default:
            throw new IllegalArgumentException("Unsupported container: " + imageName);
        }
    }

    private static TestTemplateInvocationContext createTemplate(final String imageName) {
        return new ContainerInvocationContext(imageName);
    }

}
