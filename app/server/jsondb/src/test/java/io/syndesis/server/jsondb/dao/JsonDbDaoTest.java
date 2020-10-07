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
import java.util.HashSet;
import java.util.Set;

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

    private JsonDbDao<T> dao;

    final private JsonDB jsondb = mock(JsonDB.class);

    public static Class<? extends WithUsage>[] getClasses() {
        final Reflections reflections =
            new Reflections(new ConfigurationBuilder()
                                .forPackages("io.syndesis")
                                .filterInputsBy(r -> !r.contains("Immutable")));

        Set<Class<? extends WithUsage>> withUsageSubtypes = new HashSet<>();
        withUsageSubtypes.addAll(reflections.getSubTypesOf(WithUsage.class));
        final Set<Class<?>> immutables =
            reflections.getTypesAnnotatedWith(Value.Immutable.class);
        @SuppressWarnings("rawtypes") final Set<Class<? extends TargetWithDomain>> withDomainClasses =
            reflections.getSubTypesOf(TargetWithDomain.class);

        withUsageSubtypes.retainAll(immutables);
        withUsageSubtypes.removeAll(withDomainClasses);
        // not sure why this is a DAO type
        withUsageSubtypes.remove(ConnectionOverview.class);

        @SuppressWarnings("unchecked")
        final Class<? extends WithUsage>[] classes =
            (Class<? extends WithUsage>[]) withUsageSubtypes.toArray(new Class<?>[withUsageSubtypes.size()]);

        return classes;
    }

    @ParameterizedTest
    @MethodSource("getClasses")
    public void shouldDeserializeUsage(final Class<T> type) {
        dao = new JsonDbDao<T>(jsondb) {
            @Override
            public Class<T> getType() {
                return type;
            }
        };

        final String path = "/" + Kind.from(type).getPluralModelName() + "/:id";
        when(jsondb.getAsByteArray(path)).thenReturn(JSON_BYTES);
        final T fetched = dao.fetch("id");

        assertThat(fetched.getUses()).isEqualTo(14);
    }
}
