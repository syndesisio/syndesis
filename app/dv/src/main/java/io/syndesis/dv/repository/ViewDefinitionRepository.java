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

package io.syndesis.dv.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.syndesis.dv.model.ViewDefinition;

@Repository
public interface ViewDefinitionRepository extends JpaRepository<ViewDefinition, String> {

    @Query("from ViewDefinition vd where vd.dataVirtualizationName = :dvName and vd.upperName = UPPER(:viewDefinitionName)")
    public ViewDefinition findByNameIgnoreCase(@Param("dvName") String dvName, @Param("viewDefinitionName") String viewDefinitionName);

    public List<ViewDefinition> findAllByDataVirtualizationName(String dvName);

    @Query(value = "SELECT name FROM view_definition WHERE dv_name = ?1", nativeQuery = true)
    public List<String> findAllNamesByDataVirtualizationName(String dvName);

    public Long deleteByDataVirtualizationName(String virtualization);

}
