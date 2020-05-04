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

package io.syndesis.dv.model.export.v1;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.syndesis.dv.model.DataVirtualization;

@JsonInclude(value=Include.NON_NULL)
public class DataVirtualizationV1Adapter extends BaseEntityAdapter<DataVirtualization> {

    private List<ViewDefinitionV1Adapter> views = new ArrayList<>();
    private List<SourceV1> sources = new ArrayList<>();
    private List<TablePrivilegeV1Adapter> tablePriveleges = new ArrayList<>();

    public DataVirtualizationV1Adapter() {
        super(new DataVirtualization(null));
    }

    public DataVirtualizationV1Adapter(DataVirtualization dv) {
        super(dv);
    }

    public String getDescription() {
        return entity.getDescription();
    }

    public void setDescription(String description) {
        entity.setDescription(description);
    }

    public List<ViewDefinitionV1Adapter> getViews() {
        return views;
    }

    public void setViews(List<ViewDefinitionV1Adapter> views) {
        this.views = views;
    }

    public List<SourceV1> getSources() {
        return sources;
    }

    public void setSources(List<SourceV1> sources) {
        this.sources = sources;
    }

    public List<TablePrivilegeV1Adapter> getTablePriveleges() {
        return tablePriveleges;
    }

    public void setTablePriveleges(
            List<TablePrivilegeV1Adapter> tablePriveleges) {
        this.tablePriveleges = tablePriveleges;
    }

}
