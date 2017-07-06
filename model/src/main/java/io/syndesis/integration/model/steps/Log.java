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
package io.syndesis.integration.model.steps;

import io.syndesis.integration.model.StepKinds;

/**
 * Sets the payload body
 */
public class Log extends Step {
    private String message;
    private String marker;
    private String logger;
    private String loggingLevel = "INFO";

    public Log() {
        super(StepKinds.LOG);
    }

    public Log(String message, String loggingLevel, String logger, String marker) {
        this();
        this.message = message;
        this.marker = marker;
        this.logger = logger;
        this.loggingLevel = loggingLevel;
    }

    @Override
    public String toString() {
        return "Log: " + message;
    }

    public String getKind() {
        return "log";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public String getLogger() {
        return logger;
    }

    public void setLogger(String logger) {
        this.logger = logger;
    }

    public String getLoggingLevel() {
        return loggingLevel;
    }

    public void setLoggingLevel(String loggingLevel) {
        this.loggingLevel = loggingLevel;
    }
}
