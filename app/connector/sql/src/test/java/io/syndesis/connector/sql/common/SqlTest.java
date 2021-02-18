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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.Properties;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.sql.DataSource;

import io.syndesis.connector.sql.util.SqlConnectorTestSupport;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

import com.zaxxer.hikari.HikariDataSource;

public final class SqlTest implements ParameterResolver, BeforeAllCallback, AfterAllCallback, BeforeEachCallback, AfterEachCallback {

    private static final Namespace NAMESPACE = Namespace.create(SqlTest.class);

    public static final class ConnectionInfo {
        public final String password;

        public final String url;

        public final String username;

        public ConnectionInfo(final String username, final String password, final String url) {
            this.username = username;
            this.password = password;
            this.url = url;
        }

    }

    @Target({ElementType.FIELD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface SchemaName {
        // tag for the String parameter that will receive the schema
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Setup {
        String[] value() default {};

        Variant[] variants() default {};
    }

    @Target({ElementType.METHOD, ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Teardown {
        String[] value() default {};

        Variant[] variants() default {};
    }

    public @interface Variant {
        DbEnum type();

        String[] value();
    }

    @Override
    public void afterAll(final ExtensionContext context) throws Exception {
        final Stream<String> statements = testInstanceStatements(context, Teardown.class);

        executeStatements(context, statements);
    }

    @Override
    public void afterEach(final ExtensionContext context) throws Exception {
        final Stream<String> statements = methodStatements(context, Teardown.class);

        executeStatements(context, statements);
    }

    @Override
    public void beforeAll(final ExtensionContext context) throws Exception {
        final Properties properties = new Properties();
        try (InputStream is = SqlTest.class.getClassLoader().getResourceAsStream("test-options.properties")) {
            properties.load(is);
        } catch (final IOException e) {
            throw new UncheckedIOException("Unable to read application.properties", e);
        }

        final String user = properties.getProperty("sql-connector.user");
        final String password = properties.getProperty("sql-connector.password");
        final String url = properties.getProperty("sql-connector.url");

        context.getStore(NAMESPACE).getOrComputeIfAbsent("info", ignored -> new ConnectionInfo(user, password, url));

        try (Connection connection = context.getStore(NAMESPACE).getOrComputeIfAbsent("pool", ignored -> {
            final HikariDataSource dataSource = new HikariDataSource();
            dataSource.setUsername(user);
            dataSource.setPassword(password);
            dataSource.setJdbcUrl(url);
            return dataSource;
        }, DataSource.class).getConnection()) {
            context.getStore(NAMESPACE).getOrComputeIfAbsent("schema", ignored -> {
                try {
                    return new DbMetaDataHelper(connection).getDefaultSchema(user);
                } catch (final SQLException e) {
                    throw new IllegalStateException(e);
                }
            }, String.class);
        } catch (final SQLException e) {
            throw new AssertionError("Exception during database startup.", e);
        }

        final Stream<String> statements = testInstanceStatements(context, Setup.class);

        executeStatements(context, statements);
    }

    @Override
    public void beforeEach(final ExtensionContext context) throws Exception {
        final Stream<String> statements = methodStatements(context, Setup.class);

        executeStatements(context, statements);
    }

    @Override
    public Object resolveParameter(final ParameterContext parameterContext, final ExtensionContext context) {
        final Class<?> parameterType = parameterContext.getParameter().getType();

        if (Connection.class.equals(parameterType)) {
            try {
                return context.getStore(NAMESPACE).get("pool", DataSource.class).getConnection();
            } catch (final SQLException e) {
                throw new ParameterResolutionException("Unable to get database connection from the pool", e);
            }
        }

        if (ConnectionInfo.class.equals(parameterType)) {
            return context.getStore(NAMESPACE).get("info", ConnectionInfo.class);
        }

        if (String.class.equals(parameterType) && parameterContext.isAnnotated(SchemaName.class)) {
            return context.getStore(NAMESPACE).get("schema", String.class);
        }

        throw new IllegalStateException("Unsupported parameter: " + parameterContext);
    }

    @Override
    public boolean supportsParameter(final ParameterContext parameterContext, final ExtensionContext context) {
        final Class<?> parameterType = parameterContext.getParameter().getType();
        return Connection.class.equals(parameterType) || ConnectionInfo.class.equals(parameterType)
            || (String.class.equals(parameterType) && parameterContext.isAnnotated(SchemaName.class));
    }

    static void executeStatements(final ExtensionContext context, final Stream<String> statements) {
        try (Connection connection = context.getStore(NAMESPACE).get("pool", DataSource.class).getConnection();
            Statement statement = connection.createStatement()) {
            statements.forEach(sql -> {
                try {
                    statement.execute(sql);
                } catch (final SQLException e) {
                    throw new IllegalArgumentException("Cannot execute statement: `" + sql + "`", e);
                }
            });
        } catch (final SQLException e) {
            throw new IllegalStateException(e);
        }
    }

    static Stream<String> fetchStatementsFrom(final ExtensionContext context, final Class<? extends Annotation> target, final AnnotatedElement element) {
        if (!element.isAnnotationPresent(target)) {
            return Stream.<String>empty();
        }

        final Annotation annotation = element.getAnnotation(target);
        try {
            final Class<? extends Annotation> type = annotation.annotationType();

            final String[] valueStatements = (String[]) type.getMethod("value").invoke(annotation);

            final Variant[] variantStatements = (Variant[]) type.getMethod("variants").invoke(annotation);

            if (variantStatements == null || variantStatements.length == 0) {
                return Stream.of(valueStatements);
            }

            final DbEnum databaseVariant;
            try (Connection connection = context.getStore(NAMESPACE).get("pool", DataSource.class).getConnection()) {
                databaseVariant = SqlConnectorTestSupport.determineDatabaseTypeFrom(connection);
            } catch (final SQLException e) {
                throw new IllegalStateException(e);
            }

            return Stream.concat(
                Stream.of(variantStatements).filter(v -> v.type() == databaseVariant)
                    .flatMap(v -> Stream.of(v.value())),
                Stream.of(valueStatements));
        } catch (final ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    static Stream<String> methodStatements(final ExtensionContext context, final Class<? extends Annotation> target) {
        return fetchStatementsFrom(context, target, context.getRequiredTestMethod());
    }

    static Stream<String> testInstanceStatements(final ExtensionContext context, final Class<? extends Annotation> target) {
        final Class<?> testClass = context.getRequiredTestClass();

        final Stream<AnnotatedElement> start = Stream.of((AnnotatedElement) testClass);

        final Stream<AnnotatedElement> enclosing = StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<AnnotatedElement>() {
            Class<?> current = testClass;

            @Override
            public boolean hasNext() {
                return current.getEnclosingClass() != null;
            }

            @Override
            public AnnotatedElement next() {
                current = current.getEnclosingClass();
                return current;
            }
        }, Spliterator.ORDERED | Spliterator.IMMUTABLE), false);

        return Stream.concat(start, enclosing)
            .flatMap(type -> fetchStatementsFrom(context, target, type));
    }

}
