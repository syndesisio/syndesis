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
package io.syndesis.server.jsondb;

import io.syndesis.common.model.ToJson;

/**
 * Options that can be configured on the {@link JsonDB#getAsString(String, GetOptions)}.
 */
@SuppressWarnings("PMD.TooManyMethods")
public class GetOptions implements ToJson, Cloneable {

    public enum Order {
        ASC,
        DESC
    };

    private boolean prettyPrint;
    private Integer depth;
    private String callback;
    private String startAfter;
    private String startAt;
    private String endAt;
    private String endBefore;
    private Integer limitToFirst;
    private Order order = Order.ASC;
    private Filter filter;

    public boolean prettyPrint() {
        return prettyPrint;
    }

    public Integer depth() {
        return depth;
    }

    public String callback() {
        return callback;
    }

    public GetOptions prettyPrint(final boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
        return this;
    }

    public GetOptions depth(final Integer depth) {
        this.depth = depth;
        return this;
    }

    public GetOptions callback(final String callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public GetOptions clone() throws CloneNotSupportedException{
        return (GetOptions) super.clone();
    }

    public String startAfter() {
        return startAfter;
    }

    public GetOptions startAfter(String startAfter) {
        this.startAfter = startAfter;
        return this;
    }

    public String startAt() {
        return startAt;
    }

    public GetOptions startAt(String startAt) {
        this.startAt = startAt;
        return this;
    }

    public String endBefore() {
        return endBefore;
    }

    public GetOptions endBefore(String endBefore) {
        this.endBefore = endBefore;
        return this;
    }

    public String endAt() {
        return endAt;
    }

    public GetOptions endAt(String endAt) {
        this.endAt = endAt;
        return this;
    }


    public Integer limitToFirst() {
        return limitToFirst;
    }

    public GetOptions limitToFirst(Integer limitToFirst) {
        this.limitToFirst = limitToFirst;
        return this;
    }

    public Order order() {
        return order;
    }

    public GetOptions order(Order order) {
        this.order = order;
        return this;
    }

    public Filter filter() {
        return filter;
    }

    public GetOptions filter(Filter filter) {
        this.filter = filter;
        return this;
    }

}
