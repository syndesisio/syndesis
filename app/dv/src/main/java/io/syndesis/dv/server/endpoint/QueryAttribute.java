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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;


/**
 * Object that encapsulates an artifact path object
 */
@JsonSerialize(as = QueryAttribute.class)
@JsonInclude(value=Include.NON_NULL)
public class QueryAttribute {

    private String query;

    private String target;

    private int limit = -1;

    private int offset;

    /**
     * @return the query
     */
    public String getQuery() {
        return this.query;
    }

    /**
     * @param query the query to set
     */
    public void setQuery(String query) {
        this.query = query;
    }

    /**
     * @return the target
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target the target to set
     */
    public void setTarget(String target) {
        this.target = target;
    }

    /**
     * @return the result limit
     */
    public int getLimit() {
        return limit;
    }

    /**
     * @param limit the result limit
     */
    public void setLimit(int limit) {
        this.limit = limit;
    }

    /**
     * @return the result offset
     */
    public int getOffset() {
        return offset;
    }

    /**
     * @param offset the minimum record of the result set to start at
     */
    public void setOffset(int offset) {
        this.offset = offset;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + limit;
        result = prime * result + offset;
        result = prime * result + ((query == null) ? 0 : query.hashCode());
        result = prime * result + ((target == null) ? 0 : target.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof QueryAttribute)) {
            return false;
        }
        QueryAttribute other = (QueryAttribute)obj;

        return limit == other.limit
               && offset == other.offset
               && Objects.equal(query, other.query)
               && Objects.equal(target, other.target);
    }

    @Override
    public String toString() {
        return "KomodoQueryAttribute [query=" + query + ", target=" + target + ", limit=" + limit + ", offset=" + offset + "]";
    }
}
