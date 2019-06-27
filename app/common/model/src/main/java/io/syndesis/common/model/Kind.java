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
package io.syndesis.common.model;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import io.syndesis.common.model.integration.IntegrationDeployment;

public enum Kind {
    Action(io.syndesis.common.model.action.Action.class),

    Connection(io.syndesis.common.model.connection.Connection.class),
    ConnectionOverview(io.syndesis.common.model.connection.ConnectionOverview.class),
    Connector(io.syndesis.common.model.connection.Connector.class),
    ConnectorAction(io.syndesis.common.model.action.ConnectorAction.class),
    ConnectorGroup(io.syndesis.common.model.connection.ConnectorGroup.class),
    ConnectorTemplate(io.syndesis.common.model.connection.ConnectorTemplate.class),
    Icon(io.syndesis.common.model.icon.Icon.class),

    Environment(io.syndesis.common.model.environment.Environment.class),
    EnvironmentType(io.syndesis.common.model.environment.EnvironmentType.class),

    Extension(io.syndesis.common.model.extension.Extension.class),
    StepAction(io.syndesis.common.model.action.StepAction.class),

    Organization(io.syndesis.common.model.environment.Organization.class),

    Integration(io.syndesis.common.model.integration.Integration.class),
    IntegrationOverview(io.syndesis.common.model.integration.IntegrationOverview.class),
    IntegrationDeployment(IntegrationDeployment.class),
    IntegrationDeploymentStateDetails(io.syndesis.common.model.monitoring.IntegrationDeploymentStateDetails.class),
    IntegrationMetricsSummary(io.syndesis.common.model.metrics.IntegrationMetricsSummary.class),
    IntegrationRuntime(io.syndesis.common.model.integration.IntegrationRuntime.class),
    IntegrationEndpoint(io.syndesis.common.model.integration.IntegrationEndpoint.class),

    Step(io.syndesis.common.model.integration.Step.class),

    Permission(io.syndesis.common.model.user.Permission.class),
    Role(io.syndesis.common.model.user.Role.class),
    User(io.syndesis.common.model.user.User.class),
    //Quota(io.syndesis.common.model.user.Quota.class),

    ConnectionBulletinBoard(io.syndesis.common.model.bulletin.ConnectionBulletinBoard.class),
    IntegrationBulletinBoard(io.syndesis.common.model.bulletin.IntegrationBulletinBoard.class),

    OpenApi(io.syndesis.common.model.openapi.OpenApi.class)
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

    public static Kind from(final String x) {
        Kind kind = NAME_MAP.get(x);
        if( kind == null ) {
            throw new IllegalArgumentException("No matching Kind found.");
        }
        return kind;
    }

    /**
     * A method required by the JAX-RS implementation (RESTEasy) to map
     * model names to enum values.
     */
    public static Kind fromString(String given) {
        return from(given);
    }

    public String getModelName() {
        return modelName;
    }

    public String getPluralModelName() {
        return modelName + "s";
    }

    @SuppressWarnings("unchecked")
    public <T extends WithId<T>> Class<T> getModelClass() {
        return (Class<T>) modelClass;
    }

    public boolean sameAs(WithKind other) {
        return this == other.getKind();
    }
}
