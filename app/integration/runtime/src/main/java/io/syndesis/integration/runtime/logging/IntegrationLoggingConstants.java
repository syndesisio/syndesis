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
package io.syndesis.integration.runtime.logging;

public final class IntegrationLoggingConstants {
    public static final String ACTIVITY_ID = "Syndesis.ACTIVITY_ID";
    public static final String ACTIVITY_SPAN = "Syndesis.ACTIVITY_SPAN";
    public static final String FLOW_ID = "Syndesis.FLOW_ID";
    public static final String STEP_ID = "Syndesis.STEP_ID";
    public static final String STEP_INDEX = "Syndesis.STEP_INDEX";
    public static final String STEP_TRACKER_ID = "Syndesis.STEP_TRACKER_ID";
    public static final String STEP_TRACKER_STARTED_AT = "Syndesis.STEP_TRACKER_STARTED_AT";
    public static final String STEP_SPAN = "Syndesis.STEP_SPAN";

    private IntegrationLoggingConstants() {
    }
}
