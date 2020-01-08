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
package io.syndesis.server.api.generator.soap.parser;

import io.syndesis.common.model.Violation;

/**
 * Errors when parsing from {@link SoapApiModelParser}.
 */
public class ParserException extends Exception {

    private final String error;
    private final String property;

    /**
     * Exception with default error of type 'error'.
     * @param message error message.
     */
    public ParserException(String message) {
        this(message, null, "error");
    }

    /**
     * Exception with error message and property name.
     * @param message error message.
     * @param property property name.
     */
    public ParserException(String message, String property) {
        this(message, property, null);
    }

    /**
     * Exception with error message, type and property name.
     * @param message error message.
     * @param property property name.
     * @param error error type.
     */
    public ParserException(String message, String property, String error) {
        this(message, property, error, null);
    }

    public ParserException(String message, Exception cause) {
        this(message, null, "error", cause);
    }

    /**
     * Exception with error message, type and property name.
     * @param message error message.
     * @param property property name.
     * @param error error type.
     * @param cause error cause.
     */
    public ParserException(String message, String property, String error, Exception cause) {
        super(message, cause);
        this.error = error;
        this.property = property;
    }

    public Violation toViolation() {
        return new Violation.Builder().message(super.getMessage()).error(this.error).property(this.property).build();
    }
}
