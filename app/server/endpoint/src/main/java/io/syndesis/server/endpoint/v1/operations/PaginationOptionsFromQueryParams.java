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

import io.syndesis.server.endpoint.util.PaginationOptions;

/**
 * Extracts the pagination options from the request.
 */
public class PaginationOptionsFromQueryParams implements PaginationOptions {

    private final int page;
    private final int perPage;


    public PaginationOptionsFromQueryParams(int page, int perPage){
        this.page = page;
        this.perPage = perPage;
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
