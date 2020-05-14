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
package io.syndesis.dv.model;

import java.sql.Timestamp;
import java.util.Locale;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.Version;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@MappedSuperclass
@SuppressWarnings({"PMD.AbstractClassWithoutAbstractMethod", "PMD.UnusedPrivateField"})
public abstract class BaseEntity {

    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;
    @Column(unique=true)
    private String name;
    @JsonIgnore
    @Column(unique=true)
    private String upperName;
    @Version
    private Long version;
    @Column(updatable = false)
    @CreationTimestamp
    private Timestamp createdAt;
    @UpdateTimestamp
    private Timestamp modifiedAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        if (name != null) {
            upperName = name.toUpperCase(Locale.US);
        } else {
            upperName = null;
        }
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Timestamp getModifiedAt() {
        return modifiedAt;
    }

    @PrePersist
    protected void prePersist() {
        if (name != null) {
            upperName = name.toUpperCase(Locale.US);
        }
    }

    public Long getVersion() {
        return version;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP2")
    public void setModifiedAt(Timestamp modifiedAt) {
        this.modifiedAt = modifiedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
