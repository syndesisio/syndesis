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

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("controllers")
public class DvConfigurationProperties {

    private boolean exposeVia3scale;

    public void setExposeVia3scale(final boolean exposeVia3scale) {
        this.exposeVia3scale = exposeVia3scale;
    }

    public boolean isExposeVia3scale() {
        return exposeVia3scale;
    }
}
