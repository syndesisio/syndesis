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
package io.syndesis.server.verifier;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("meta")
public class MetadataConfigurationProperties {

    private static final int DEFAULT_THREAD_COUNT = 3;

    private static final int DEFAULT_TIMEOUT = 15000;

    private String service = "syndesis-meta";

    private int threads = DEFAULT_THREAD_COUNT;

    private int timeout = DEFAULT_TIMEOUT;

    public String getService() {
        return service;
    }

    public int getThreads() {
        return threads;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setService(final String service) {
        this.service = service;
    }

    public void setThreads(final int threads) {
        this.threads = threads;
    }

    public void setTimeout(final int timeout) {
        this.timeout = timeout;
    }
}
