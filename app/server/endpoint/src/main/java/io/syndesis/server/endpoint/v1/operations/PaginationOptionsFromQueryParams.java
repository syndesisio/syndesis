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
package io.syndesis.server.endpoint.v1.operations;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import io.syndesis.server.endpoint.util.PaginationOptions;

/**
 * Extracts the pagination options from the request.
 */
public class PaginationOptionsFromQueryParams implements PaginationOptions {

    private final int page;
    private final int perPage;

    /**
     *  Extracts the pagination options from the request.
     * @param uri The request context.
     */
    public PaginationOptionsFromQueryParams(UriInfo uri) {
        MultivaluedMap<String, String> queryParams = uri.getQueryParameters();
        String pageQuery = queryParams.getFirst("page");
        if (pageQuery == null || pageQuery.isEmpty()) {
            page = 1;
        } else {
            page = Integer.parseInt(pageQuery);
        }

        String perPageQuery = queryParams.getFirst("per_page");
        if (perPageQuery == null || perPageQuery.isEmpty()) {
            perPage = 20;
        } else {
            perPage = Integer.parseInt(perPageQuery);
        }
    }

    /**
     *
     * @return The requested page number.
     */
    @Override
    public int getPage() {
        return page;
    }

    /**
     *
     * @return The requested number per page.
     */
    @Override
    public int getPerPage() {
        return perPage;
    }

}
