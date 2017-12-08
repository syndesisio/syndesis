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
package io.syndesis.integration.model.steps;

import com.google.auto.service.AutoService;

/**
 * Invokes an endpoint URI (typically HTTP or HTTPS) with the current payload
 */
@AutoService(Step.class)
public class Endpoint extends Step {
    public static final String KIND = "endpoint";

    private String uri;

    public Endpoint() {
        super(KIND);
    }

    public Endpoint(String uri) {
        super(KIND);

        this.uri = uri;
    }

    @Override
    public String toString() {
        return "Endpoint: " + uri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

}
