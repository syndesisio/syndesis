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
package io.syndesis.rest.v1.operations;

import java.util.Locale;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import io.syndesis.rest.util.SortOptions;

public class SortOptionsFromQueryParams implements SortOptions {

    private final String sortField;
    private final SortDirection sortDirection;

    public SortOptionsFromQueryParams(UriInfo uri) {
        if( uri == null ) {
            sortField = null;
            sortDirection = SortOptions.SortDirection.ASC;
        } else {
            MultivaluedMap<String, String> queryParams = uri.getQueryParameters();
            sortField = queryParams.getFirst("sort");
            String dir = queryParams.getFirst("direction");
            sortDirection = dir == null ?  SortOptions.SortDirection.ASC : SortOptions.SortDirection.valueOf(dir.toUpperCase(Locale.US));
        }
    }

    @Override
    public String getSortField() {
        return sortField;
    }

    @Override
    public SortDirection getSortDirection() {
        return sortDirection;
    }
}
