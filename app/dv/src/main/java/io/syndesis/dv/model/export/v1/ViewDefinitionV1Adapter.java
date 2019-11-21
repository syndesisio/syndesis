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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import io.syndesis.dv.model.ViewDefinition;

@JsonInclude(value=Include.NON_NULL)
public class ViewDefinitionV1Adapter extends BaseEntityAdapter<ViewDefinition> {

    public ViewDefinitionV1Adapter() {
        super(new ViewDefinition(null, null));
    }

    public ViewDefinitionV1Adapter(ViewDefinition vd) {
        super(vd);
    }

    public String getDdl() {
        return entity.getDdl();
    }

    public void setDdl(String ddl) {
        entity.setDdl(ddl);
    }

    public String getDescription() {
        return entity.getDescription();
    }

    public void setDescription(String description) {
        entity.setDescription(description);
    }

    public boolean isComplete() {
        return entity.isComplete();
    }

    public void setComplete(boolean complete) {
        entity.setComplete(complete);
    }

    public boolean isUserDefined() {
        return entity.isUserDefined();
    }

    public void setUserDefined(boolean userDefined) {
        entity.setUserDefined(userDefined);
    }

}