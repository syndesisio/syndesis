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
package io.syndesis.server.dao;


import io.syndesis.common.model.openapi.OpenApi;
import io.syndesis.server.dao.manager.DataAccessObject;

public interface OpenApiDao extends DataAccessObject<OpenApi> {

    @Override
    default Class<OpenApi> getType() {
        return OpenApi.class;
    }

}
