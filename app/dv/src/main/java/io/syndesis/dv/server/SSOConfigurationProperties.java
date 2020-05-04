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
package io.syndesis.dv.server;

import java.util.Map;
import java.util.TreeMap;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TODO: This could take overrides per VDB, but for now it just
 * represents the SSO_ env properties
 */
@ConfigurationProperties("sso")
public class SSOConfigurationProperties {

    private volatile TreeMap<String, String> allEnv;

    public String getAuthServerUrl() {
        return getAllKeycloakFromEnv().get("KEYCLOAK_AUTHSERVERURL");
    }

    public TreeMap<String, String> getAllKeycloakFromEnv() {
        if (allEnv == null) {
            //postconstruct doesn't seem to work with config props,
            //so we'll just do this lazily
            synchronized (this) {
                if (allEnv == null) {
                    allEnv = new TreeMap<>();
                    Map<String, String> env = System.getenv();
                    for (Map.Entry<String, String> entry : env.entrySet()) {
                        if (entry.getKey().startsWith("SSO_")) {
                            allEnv.put("KEYCLOAK_"+entry.getKey().substring(4), entry.getValue());
                        }
                    }
                }
            }
        }
        return allEnv;
    }

}
