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
package io.syndesis.connector.sql.stored;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import io.syndesis.connector.sql.SqlSupport;
import io.syndesis.connector.sql.common.stored.ColumnMode;
import io.syndesis.connector.sql.common.stored.StoredProcedureColumn;
import io.syndesis.connector.sql.common.stored.StoredProcedureMetadata;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SqlStoredConnectorMetaDataExtensionTest {

    @Test
    public void shouldFetchStoredProcedureMetadata() throws SQLException {
        final Connection connection = mock(Connection.class);

        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);

        when(databaseMetaData.getDatabaseProductName()).thenReturn("POSTGRESQL");

        final ResultSet result = mock(ResultSet.class);
        when(databaseMetaData.getFunctionColumns("catalog", "schema", "procedureName", null)).thenReturn(result);

        when(result.next()).thenReturn(true, true, true, false);
        when(result.getString("COLUMN_NAME")).thenReturn("A", "B", "C");
        when(result.getInt("COLUMN_TYPE")).thenReturn(ColumnMode.IN.ordinal(), ColumnMode.IN.ordinal(),
            ColumnMode.OUT.ordinal());
        when(result.getInt("DATA_TYPE")).thenReturn(JDBCType.INTEGER.getVendorTypeNumber(),
            JDBCType.INTEGER.getVendorTypeNumber(), JDBCType.INTEGER.getVendorTypeNumber());

        final StoredProcedureMetadata metadata = SqlSupport.getStoredProcedureMetadata(connection, "catalog", "schema",
            "procedureName");

        final StoredProcedureColumn columnA = new StoredProcedureColumn();
        columnA.setJdbcType(JDBCType.INTEGER);
        columnA.setName("A");
        columnA.setOrdinal(0);
        columnA.setMode(ColumnMode.IN);

        final StoredProcedureColumn columnB = new StoredProcedureColumn();
        columnB.setJdbcType(JDBCType.INTEGER);
        columnB.setName("B");
        columnB.setOrdinal(0);
        columnB.setMode(ColumnMode.IN);

        final StoredProcedureColumn columnC = new StoredProcedureColumn();
        columnC.setJdbcType(JDBCType.INTEGER);
        columnC.setName("C");
        columnC.setOrdinal(0);
        columnC.setMode(ColumnMode.OUT);

        assertThat(metadata.getName()).isEqualTo("procedureName");
        assertThat(metadata.getTemplate())
            .isEqualTo("procedureName(INTEGER ${body[A]}, INTEGER ${body[B]}, OUT INTEGER C)");
        final List<StoredProcedureColumn> columnList = metadata.getColumnList();
        assertThat(columnList.get(0)).isEqualToComparingFieldByField(columnA);
        assertThat(columnList.get(1)).isEqualToComparingFieldByField(columnB);
        assertThat(columnList.get(2)).isEqualToComparingFieldByField(columnC);
    }

    @Test
    public void shouldFetchStoredProcedureMetadataWithSingleParameter() throws SQLException {
        final Connection connection = mock(Connection.class);

        final DatabaseMetaData databaseMetaData = mock(DatabaseMetaData.class);
        when(connection.getMetaData()).thenReturn(databaseMetaData);

        when(databaseMetaData.getDatabaseProductName()).thenReturn("POSTGRESQL");

        final ResultSet result = mock(ResultSet.class);
        when(databaseMetaData.getFunctionColumns("catalog", "schema", "procedureName", null)).thenReturn(result);

        when(result.next()).thenReturn(true, false);
        when(result.getString("COLUMN_NAME")).thenReturn("A");
        when(result.getInt("COLUMN_TYPE")).thenReturn(ColumnMode.IN.ordinal());
        when(result.getInt("DATA_TYPE")).thenReturn(JDBCType.INTEGER.getVendorTypeNumber());

        final StoredProcedureMetadata metadata = SqlSupport.getStoredProcedureMetadata(connection, "catalog", "schema",
            "procedureName");

        final StoredProcedureColumn columnA = new StoredProcedureColumn();
        columnA.setJdbcType(JDBCType.INTEGER);
        columnA.setName("A");
        columnA.setOrdinal(0);
        columnA.setMode(ColumnMode.IN);

        assertThat(metadata.getName()).isEqualTo("procedureName");
        assertThat(metadata.getTemplate()).isEqualTo("procedureName(INTEGER ${body[A]})");
        final List<StoredProcedureColumn> columnList = metadata.getColumnList();
        assertThat(columnList.get(0)).isEqualToComparingFieldByField(columnA);
    }
}
