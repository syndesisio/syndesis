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

package io.syndesis.dv.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import io.syndesis.dv.StringConstants;

@Entity
@DiscriminatorValue("v")
public class DataVirtualization extends BaseDataVirtualization {

    /**
     * Get the preview vdb name for the virtualization name -
     * the suffix is added to not conflict with the source and
     * main preview vdbs
     * @param name
     * @return
     */
    public static String getPreviewVdbName(String name) {
        return name + StringConstants.SERVICE_VDB_SUFFIX;
    }

    private String description;
    private boolean modified = true;

    protected DataVirtualization() {
    }

    public DataVirtualization(String name) {
        setName(name);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        setModified(true);
    }

    /**
     * increment version and update modification timestamp
     */
    public void touch() {
        setModifiedAt(null);
        modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

}
