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
package io.syndesis.dv.server.endpoint;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Represents the configuration for the view's source info
 */
@JsonSerialize(as = RestViewSourceInfo.class)
@JsonInclude(Include.NON_NULL)
public class RestViewSourceInfo {

    /*
     * The array of source schemas
     */
    private final List<RestSourceSchema> schemas;

    public RestViewSourceInfo(List<RestSourceSchema> sourceSchemas) {
        this.schemas = sourceSchemas;
    }

    /**
     * @return the projected columns
     */
    public List<RestSourceSchema> getSchemas() {
        return schemas;
    }
}
