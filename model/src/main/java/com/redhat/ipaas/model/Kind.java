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
package com.redhat.ipaas.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.Collectors;

public enum Kind {

    Action(com.redhat.ipaas.model.connection.Action.class),
    Connection(com.redhat.ipaas.model.connection.Connection.class),
    Connector(com.redhat.ipaas.model.connection.Connector.class),
    ConnectorGroup(com.redhat.ipaas.model.connection.ConnectorGroup.class),

    Environment(com.redhat.ipaas.model.environment.Environment.class),
    EnvironmentType(com.redhat.ipaas.model.environment.EnvironmentType.class),
    Organization(com.redhat.ipaas.model.environment.Organization.class),

    Integration(com.redhat.ipaas.model.integration.Integration.class),
    IntegrationConnectionStep( com.redhat.ipaas.model.integration.IntegrationConnectionStep.class),
    IntegrationPattern( com.redhat.ipaas.model.integration.IntegrationPattern.class),
    IntegrationPatternGroup(com.redhat.ipaas.model.integration.IntegrationPatternGroup.class),
    IntegrationRuntime(com.redhat.ipaas.model.integration.IntegrationRuntime.class),
    IntegrationTemplate(com.redhat.ipaas.model.integration.IntegrationTemplate.class),
    IntegrationTemplateConnectionStep(com.redhat.ipaas.model.integration.IntegrationTemplateConnectionStep.class),
    Step(com.redhat.ipaas.model.integration.Step.class),
    Tag(com.redhat.ipaas.model.integration.Tag.class),

    Permission(com.redhat.ipaas.model.user.Permission.class),
    Role(com.redhat.ipaas.model.user.Role.class),
    User(com.redhat.ipaas.model.user.User.class),
    ;

    public final String modelName;
    public final Class<? extends WithId> modelClass;

    private static final HashMap<String, Kind> nameMap = new HashMap<>();
    private static final HashMap<Class, Kind> modelMap = new HashMap<>();
    static {
        for (Kind kind : Kind.values()) {
            nameMap.put(kind.modelName, kind);
            modelMap.put(kind.modelClass, kind);
        }
    }

    Kind(Class<? extends WithId> model) {
        this(name(model.getSimpleName()), model);
    }

    Kind(String name, Class<? extends WithId> model) {
        this.modelName = name;
        this.modelClass = model;
    }

    private static String name(String value) {
        String regex = "(?<!(^|[A-Z]))(?=[A-Z])|(?<!^)(?=[A-Z][a-z])";
        return Arrays.stream(value.split(regex)).map(x->x.toLowerCase()).collect(Collectors.joining("-"));
    }

    @Override
    public String toString() {
        return modelName;
    }

    public static Kind from(Class<?> x) {
        Kind kind = modelMap.get(x);
        if( kind == null ) {
            throw new IllegalArgumentException("No matching Kind found.");
        }
        return kind;
    }

    public static Kind from(String x) {
        Kind kind = nameMap.get(x);
        if( kind == null ) {
            throw new IllegalArgumentException("No matching Lind found.");
        }
        return kind;
    }

    public String getModelName() {
        return modelName;
    }

    public Class<? extends WithId> getModelClass() {
        return modelClass;
    }

}
