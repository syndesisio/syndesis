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
package io.syndesis.server.controller.integration.camelk.crd;

import io.fabric8.kubernetes.client.CustomResource;

public class Integration extends CustomResource {
    private IntegrationSpec spec;
    private IntegrationStatus status;

    public IntegrationSpec getSpec() {
        return spec;
    }

    public IntegrationStatus getStatus() {
        return status;
    }

    public void setStatus(IntegrationStatus status) {
        this.status = status;
    }

    public void setSpec(IntegrationSpec spec) {
        this.spec = spec;
    }

    @Override
    public String toString() {
        return "Integration{" +
            "spec=" + spec +
            ", status=" + status +
            '}';
    }
}
