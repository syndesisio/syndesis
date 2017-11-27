/**
 * Copyright (C) 2017 Red Hat, Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.timer;

import org.apache.camel.component.connector.DefaultConnectorComponent;

/**
 * Camel periodic timer connector
 */
public class PeriodicTimerComponent extends DefaultConnectorComponent {

    public PeriodicTimerComponent() {
        this(null);
    }

    public PeriodicTimerComponent(String componentSchema) {
        super("periodic-timer-connector", componentSchema, PeriodicTimerComponent.class.getName());
    }
}
