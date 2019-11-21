/*
 * Copyright (C) 2013 Red Hat, Inc.
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

package io.syndesis.dv.model.export.v1;

import java.sql.Timestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import io.syndesis.dv.model.BaseEntity;

public class BaseEntityAdapter<T extends BaseEntity> {

    protected T entity;

    protected BaseEntityAdapter(T entity) {
        this.entity = entity;
    }

    public Timestamp getCreatedAt() {
        return entity.getCreatedAt();
    }

    public String getId() {
        return entity.getId();
    }

    public Timestamp getModifiedAt() {
        return entity.getModifiedAt();
    }

    public String getName() {
        return entity.getName();
    }

    public void setId(String id) {
        entity.setId(id);
    }

    public void setName(String name) {
        entity.setName(name);
    }

    @JsonIgnore
    public T getEntity() {
        return entity;
    }

}
