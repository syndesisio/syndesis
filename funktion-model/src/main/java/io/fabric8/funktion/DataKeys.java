/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.fabric8.funktion;

/**
 * Keys used in Data inside ConfigMap resources
 */
public class DataKeys {
    public static class Connector {
        public static final String SCHEMA_YAML = "schema.yml";
        public static final String ASCIIDOC = "documentation.adoc";
        public static final String DEPLOYMENT_YAML = "deployment.yml";
    }

    public static class Subscription {
        public static final String APPLICATION_PROPERTIES = "application.properties";
        public static final String FUNKTION_YAML = "funktion.yml";
    }
}
