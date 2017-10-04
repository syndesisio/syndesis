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
package io.syndesis.controllers.integration.online;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import io.syndesis.dao.manager.DataManager;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.model.connection.Connector;
import io.syndesis.model.integration.Integration;
import io.syndesis.model.integration.Step;
import io.syndesis.openshift.OpenShiftService;

public class BaseHandler {
    private final OpenShiftService openShiftService;

    protected BaseHandler(OpenShiftService openShiftService) {
        this.openShiftService = openShiftService;
    }

    protected OpenShiftService openShiftService() {
        return openShiftService;
    }


    /**
     * Fetch the Connectors
     */
    protected static Map<String, Connector> fetchConnectorsMap(DataManager dataManager) {
        return dataManager.fetchAll(Connector.class).getItems()
            .stream()
            .collect(Collectors.toMap(o -> o.getId().get(), Function.identity()));
    }

    /**
     * Build Connector Suffix map.
     */
    protected static Map<Step, String> buildConnectorSuffixMap(Integration integration) {
        final Map<Step, String> connectorIdMap = new HashMap<>();

        integration.getSteps().ifPresent(steps -> {
            steps.stream()
                .filter(s -> s.getStepKind().equals(Endpoint.KIND))
                .filter(s -> s.getAction().isPresent())
                .filter(s -> s.getConnection().isPresent())
                .collect(Collectors.groupingBy(s -> s.getAction().get().getCamelConnectorPrefix()))
                .forEach(
                    (prefix, stepList) -> {
                        if (stepList.size() > 1) {
                            for (int i = 0; i < stepList.size(); i++) {
                                connectorIdMap.put(stepList.get(i), Integer.toString(i + 1));
                            }
                        }
                    }
                );
            })
        ;

        return Collections.unmodifiableMap(connectorIdMap);
    }
}
