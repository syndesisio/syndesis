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

package io.syndesis.dv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.syndesis.dv.model.TablePrivileges;

@Repository
public interface TablePrivilegesRepository extends JpaRepository<TablePrivileges, String> {

    @Query(nativeQuery = true, value="SELECT distinct role_name FROM dv_table_privileges")
    List<String> findRoleNames();

    @Query(value = "SELECT count(*) FROM "
            + "dv_table_privileges tp inner join view_definition vd on (tp.view_definition_id = vd.id)"
            + " where vd.dv_name = :name ", nativeQuery = true)
    public long countByVirtualizationName(@Param("name") String name);

    @Query(value = "SELECT tp.* FROM "
            + "dv_table_privileges tp inner join view_definition vd on (tp.view_definition_id = vd.id)"
            + " where vd.dv_name = :name ", nativeQuery = true)
    List<TablePrivileges> findAllByVirtualizationName(@Param("name") String virtualization);

    TablePrivileges findTablePrivilegesByViewDefinitionIdAndRoleName(String viewId, String role);

    List<TablePrivileges> findTablePrivilegesByViewDefinitionId(String viewId);

    void deleteByViewDefinitionIdIn(List<String> ids);

}
