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
package io.syndesis.server.api.generator;

/**
 * Provide additional information on the type of validation that should be performed on the API.
 */
public enum APIValidationContext {

    /**
     * No validation required.
     */
    NONE,

    /**
     * Apply all validation rules for APIs exposed by Syndesis.
     */
    PROVIDED_API,

    /**
     * Apply all validation rules for external APIs consumed by Syndesis.
     */
    CONSUMED_API
}
