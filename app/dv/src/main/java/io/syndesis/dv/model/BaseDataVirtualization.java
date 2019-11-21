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

import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.Table;

@Entity
@Table(name ="data_virtualization")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorValue("Z")
@DiscriminatorColumn(discriminatorType = DiscriminatorType.CHAR, name = "TYPE", length = 1)
public abstract class BaseDataVirtualization extends BaseEntity {

    private String sourceId;

    public String getSourceId() {
        return sourceId;
    }

    public void setSourceId(String id) {
        this.sourceId = id;
    }

}
