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

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.persistence.AttributeConverter;
import javax.persistence.Column;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

class TablePrivilegesId implements Serializable {
    private String viewDefinitionId;
    private String roleName;

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TablePrivilegesId)) {
            return false;
        }
        TablePrivilegesId other = (TablePrivilegesId)obj;
        return Objects.equals(viewDefinitionId, other.viewDefinitionId)
                && Objects.equals(roleName, other.roleName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(viewDefinitionId, roleName);
    }

    public String getViewDefinitionId() {
        return viewDefinitionId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setViewDefinitionId(String viewDefinitionId) {
        this.viewDefinitionId = viewDefinitionId;
    }
}

@Entity
@Table(name = "dv_table_privileges")
@IdClass(TablePrivilegesId.class)
public class TablePrivileges {

    public enum Privilege {
        S("SELECT"),
        I("INSERT"),
        U("UPDATE"),
        D("DELETE");

        private String fullName;

        private Privilege(String name) {
            this.fullName = name;
        }

        @JsonValue
        public String getFullName() {
            return fullName;
        }

        @Override
        public String toString() {
            return fullName;
        }

    };

    /**
     * Rather than dealing with enum arrays in the database,
     * we'll instead serialize as just the distinct single character
     * enum names
     */
    public static class PrivilegeConvertor implements AttributeConverter<TreeSet<Privilege>, String> {

        @Override
        public String convertToDatabaseColumn(TreeSet<Privilege> attribute) {
            return attribute.stream().map(p -> p.name())
                    .collect(Collectors.joining());
        }

        @Override
        public TreeSet<Privilege> convertToEntityAttribute(String dbData) {
            if (dbData == null) {
                return new TreeSet<>();
            }
            return dbData.chars()
                    .mapToObj(i -> Privilege.valueOf(String.valueOf((char) i)))
                    .collect(Collectors.toCollection(TreeSet::new));
        }
    }

    @JsonIgnore
    @Column(name = "view_definition_id")
    @Id private String viewDefinitionId;
    //for bulk operations, not part of the db/import/export
    @Transient
    private String[] viewDefinitionIds;
    @Id private String roleName;
    @Convert(converter = PrivilegeConvertor.class)
    private Set<Privilege> grantPrivileges = new TreeSet<>();
    //Set<Privilege> revokePrivilege = new TreeSet<>();

    public TablePrivileges() {
    }

    public TablePrivileges(String roleName, String viewId, Privilege... privileges) {
        this.roleName = roleName;
        this.viewDefinitionId = viewId;
        if (privileges != null) {
            grantPrivileges.addAll(Arrays.asList(privileges));
        }
    }

    public String getViewDefinitionId() {
        return viewDefinitionId;
    }

    public void setViewDefinitionId(String viewDefinitionId) {
        this.viewDefinitionId = viewDefinitionId;
    }

    public Set<Privilege> getGrantPrivileges() {
        return grantPrivileges;
    }

    public TablePrivileges addPrivilege(Privilege p) {
        this.grantPrivileges.add(p);
        return this;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setGrantPrivileges(Set<Privilege> grantPrivileges) {
        this.grantPrivileges = grantPrivileges;
    }

    public void setRoleName(String role) {
        this.roleName = role;
    }

    @Override
    public String toString() {
        return String.join(" ", viewDefinitionId, roleName, grantPrivileges.toString()); //$NON-NLS-1$
    }

    public String[] getViewDefinitionIds() {
        if (viewDefinitionIds == null) {
            return new String[] {viewDefinitionId};
        }
        return viewDefinitionIds;
    }

    public void setViewDefinitionIds(String[] viewDefinitionIds) {
        this.viewDefinitionIds = viewDefinitionIds;
    }

}
