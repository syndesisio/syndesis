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
package io.syndesis.server.jsondb.dao;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.stream.Stream;

import io.syndesis.common.model.Kind;
import io.syndesis.common.model.WithId;
import io.syndesis.common.model.WithUsage;
import io.syndesis.common.model.connection.ConnectionOverview;
import io.syndesis.common.model.validation.TargetWithDomain;
import io.syndesis.server.jsondb.JsonDB;

import org.immutables.value.Value;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JsonDbDaoTest<T extends WithId<T> & WithUsage> {

    private static final byte[] JSON_BYTES = "{\"uses\":14}".getBytes(StandardCharsets.UTF_8);

    static final JsonDB JSONDB = mock(JsonDB.class);

    @ParameterizedTest
    @MethodSource("parameters")
    public void shouldDeserializeUsage(final JsonDbDao<T> dao) {
        final String path = "/" + Kind.from(dao.getType()).getPluralModelName() + "/:id";
        when(JSONDB.getAsByteArray(path)).thenReturn(JSON_BYTES);

        final T fetched = dao.fetch("id");

        assertThat(fetched.getUses()).isEqualTo(14);
    }

    static Stream<JsonDbDao<?>> parameters() {
        final Reflections reflections = new Reflections(new ConfigurationBuilder()
            .forPackages("io.syndesis")
            .filterInputsBy(r -> !r.contains("Immutable")));

        final Set<Class<? extends WithUsage>> withUsageSubtypes = reflections.getSubTypesOf(WithUsage.class);
        final Set<Class<?>> immutables = reflections.getTypesAnnotatedWith(Value.Immutable.class);
        @SuppressWarnings("rawtypes")
        final Set<Class<? extends TargetWithDomain>> withDomainClasses = reflections.getSubTypesOf(TargetWithDomain.class);

        withUsageSubtypes.retainAll(immutables);
        withUsageSubtypes.removeAll(withDomainClasses);
        withUsageSubtypes.remove(ConnectionOverview.class); // not sure why this
                                                            // is a DAO type

        return withUsageSubtypes.stream()
            .map(JsonDbDaoTest::stubDao);
    }

    static <T extends WithId<T>> JsonDbDao<?> stubDao(Class<?> type) {
        return new JsonDbDao<T>(JSONDB) {
            @SuppressWarnings("unchecked")
            @Override
            public Class<T> getType() {
                return (Class<T>) type;
            }

            @Override
            public String toString() {
                return type.getSimpleName();
            }
        };
    }

}
