/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public enum Kind {
    Action(io.syndesis.model.action.Action.class),

    Connection(io.syndesis.model.connection.Connection.class),
    Connector(io.syndesis.model.connection.Connector.class),
    ConnectorAction(io.syndesis.model.action.ConnectorAction.class),
    ConnectorGroup(io.syndesis.model.connection.ConnectorGroup.class),

    Environment(io.syndesis.model.environment.Environment.class),
    EnvironmentType(io.syndesis.model.environment.EnvironmentType.class),

    Extension(io.syndesis.model.extension.Extension.class),
    ExtensionAction(io.syndesis.model.action.ExtensionAction.class),

    Organization(io.syndesis.model.environment.Organization.class),

    Integration(io.syndesis.model.integration.Integration.class),
    IntegrationRuntime(io.syndesis.model.integration.IntegrationRuntime.class),
    Step(io.syndesis.model.integration.Step.class),

    Permission(io.syndesis.model.user.Permission.class),
    Role(io.syndesis.model.user.Role.class),
    User(io.syndesis.model.user.User.class)
    ;

    public final String modelName;
    public final Class<? extends WithKind> modelClass;

    private static final Map<String, Kind> NAME_MAP;
    private static final Map<Class<?>, Kind> MODEL_MAP;
    static {
        final Map<String, Kind> kindByName = new HashMap<>();
        final Map<Class<?>, Kind> kindByType = new HashMap<>();

        for (Kind kind : Kind.values()) {
            kindByName.put(kind.modelName, kind);
            kindByType.put(kind.modelClass, kind);
        }

        NAME_MAP = Collections.unmodifiableMap(kindByName);
        MODEL_MAP = Collections.unmodifiableMap(kindByType);
    }

    Kind(Class<? extends WithKind> model) {
        this(name(model.getSimpleName()), model);
    }

    Kind(String name, Class<? extends WithKind> model) {
        this.modelName = name;
        this.modelClass = model;
    }

    private static String name(String value) {
        String regex = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";
        return Arrays.stream(value.split(regex)).map(x->x.toLowerCase(Locale.US)).collect(Collectors.joining("-"));
    }

    @Override
    public String toString() {
        return modelName;
    }

    public static Kind from(Class<?> x) {
        Kind kind = MODEL_MAP.get(x);
        if( kind == null ) {
            throw new IllegalArgumentException("No matching Kind found.");
        }
        return kind;
    }

    public static Kind from(String x) {
        Kind kind = NAME_MAP.get(x);
        if( kind == null ) {
            throw new IllegalArgumentException("No matching Kind found.");
        }
        return kind;
    }

    public String getModelName() {
        return modelName;
    }

    @SuppressWarnings("unchecked")
    public <T extends WithId<T>> Class<T> getModelClass() {
        return (Class<T>) modelClass;
    }

}
