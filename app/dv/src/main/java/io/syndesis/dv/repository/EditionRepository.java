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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import io.syndesis.dv.model.Edition;

@Repository
public interface EditionRepository extends JpaRepository<Edition, String> {

    @Query(value = "SELECT max(revision) FROM edition where dv_name = :name", nativeQuery = true)
    Long findMaxRevision(@Param("name") String name);

    Edition findByDataVirtualizationNameAndRevision(String virtualization,
            long revision);

    List<Edition> findAllByDataVirtualizationName(String virtualization);

    @Modifying
    @Query(value = "update edition set dv_export = :byteArray where id = :id", nativeQuery = true)
    void saveExport(@Param("id") String id, @Param("byteArray") byte[] byteArray);

    @Query(value = "select dv_export from edition where id = :id", nativeQuery = true)
    byte[] findExport(@Param("id") String id);

}
