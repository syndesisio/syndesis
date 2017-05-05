/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.syndesis.catalog;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.catalog.connector.ConnectorDto;

public class CatalogExampleMain {

    public static void main(String[] args) throws Exception {
        System.out.println("Running ...");
        System.out.println("");

        ConnectorCatalog catalog = new ConnectorCatalog();
        catalog.init();

        catalog.addConnector("io.syndesis", "timer-connector", "0.2.0");
        catalog.addConnector("io.syndesis", "http-get-connector", "0.2.0");
        catalog.addConnector("io.syndesis", "http-post-connector", "0.2.0");

        System.out.println("");
        catalog.listConnectors().stream().forEach(c -> {
            System.out.println("Connector: " + c.getName() + " scheme: " + c.getScheme() + " (" + c.getDescription() + ") in (" + c.getMavenGav() + ")");
        });

        ConnectorDto dto = catalog.listConnectors().stream().filter(c -> c.getScheme().equals("periodic-timer")).findFirst().get();
        // grab the timer
        String scheme = dto.getScheme();
        System.out.println("");
        System.out.println("Lets build an uri using scheme " + scheme);

        Map<String, String> options = new HashMap<>();
        options.put("timerName", "dude");
        options.put("period", "1234");

        String uri = catalog.buildEndpointUri(scheme, options);
        System.out.println(">>> " + uri);

        System.out.println("");
        System.out.println("The end");
    }

}
