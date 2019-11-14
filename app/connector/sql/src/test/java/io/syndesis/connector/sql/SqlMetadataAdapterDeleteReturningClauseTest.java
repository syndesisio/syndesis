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
package io.syndesis.connector.sql;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.skyscreamer.jsonassert.JSONAssert.assertEquals;

import com.fasterxml.jackson.databind.ObjectWriter;
import io.syndesis.common.util.json.JsonUtils;
import io.syndesis.connector.sql.common.SqlParam;
import io.syndesis.connector.sql.common.SqlStatementMetaData;
import io.syndesis.connector.sql.common.SqlStatementParser;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.PostgreSQLContainer;

@RunWith(Parameterized.class)
@Ignore("Needs docker installed to run")
public class SqlMetadataAdapterDeleteReturningClauseTest {

    @Rule
    public JdbcDatabaseContainer<?> database;

    public SqlMetadataAdapterDeleteReturningClauseTest(final JdbcDatabaseContainer<?> database) {
        this.database = database;
    }

    @SuppressWarnings("resource")
    @Parameters
    public static Collection<Object> databaseContainerImages() {
        return Arrays.asList(
            new PostgreSQLContainer<>()
        );
    }

    @Before
    public void setupTable() throws SQLException {
        try (Statement stmt = database.createConnection("").createStatement()) {
            final String createTable = "CREATE TABLE TEST (charType CHAR, varcharType VARCHAR(255), "
                                           + "numericType NUMERIC, decimalType DECIMAL, smallintType SMALLINT," +
                                           "dateType DATE, timeType TIME )";
            stmt.executeUpdate(createTable);
            final String sql = String.format("INSERT INTO TEST VALUES ('J', 'Jackson',0, 0, 0, '%s', '%s')",
                SqlParam.SqlSampleValue.DATE_VALUE,
                SqlParam.SqlSampleValue.TIME_VALUE);
            stmt.executeUpdate(sql);
        }
    }

    @Test
    public void deleteReturningAll() throws SQLException, IOException, JSONException {

        String query = "DELETE FROM TEST where charType=:#myCharValue RETURNING *";
        final SqlStatementParser sqlParser = new SqlStatementParser(database.createConnection(""), query);
        final SqlStatementMetaData paramInfo = sqlParser.parse();
        final List<SqlParam> inputParams = paramInfo.getInParams();
        // information for input
        Assert.assertEquals(1, inputParams.size());
        Assert.assertEquals(Character.class, inputParams.get(0).getTypeValue().getClazz());

        // information for output of select statement
        final List<SqlParam> outputParams = paramInfo.getOutParams();
        Assert.assertEquals(7, outputParams.size());
        Assert.assertEquals(Character.class, outputParams.get(0).getTypeValue().getClazz());

        CamelContext camelContext = new DefaultCamelContext();
        SqlConnectorMetaDataExtension ext = new SqlConnectorMetaDataExtension(camelContext);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("url", database.getJdbcUrl());
        parameters.put("user", database.getUsername());
        parameters.put("password", database.getPassword());

        parameters.put("query", query);
        Optional<MetaDataExtension.MetaData> metadata = ext.meta(parameters);
        SqlMetadataRetrieval adapter = new SqlMetadataRetrieval();

        SyndesisMetadata syndesisMetaData2 = adapter.adapt(camelContext, "sql", "sql-connector", parameters,
            metadata.get());
        ObjectWriter writer = JsonUtils.writer();
        String actualMetadata =
            writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsString(syndesisMetaData2);

        String expectedMetadata = IOUtils.toString(this.getClass().getResource("/sql/delete_returning_all.json"),
            StandardCharsets.UTF_8).trim();
        assertEquals(expectedMetadata, actualMetadata, JSONCompareMode.NON_EXTENSIBLE);
    }

    @Test
    public void deleteReturningOne() throws SQLException, IOException, JSONException {
        String query = "DELETE FROM TEST where charType=:#myCharValue RETURNING charType";
        final SqlStatementParser sqlParser = new SqlStatementParser(database.createConnection(""), query);
        final SqlStatementMetaData paramInfo = sqlParser.parse();
        final List<SqlParam> inputParams = paramInfo.getInParams();
        // information for input
        Assert.assertEquals(1, inputParams.size());
        Assert.assertEquals(Character.class, inputParams.get(0).getTypeValue().getClazz());

        // information for output of select statement
        final List<SqlParam> outputParams = paramInfo.getOutParams();
        Assert.assertEquals(1, outputParams.size());
        Assert.assertEquals(Character.class, outputParams.get(0).getTypeValue().getClazz());


        CamelContext camelContext = new DefaultCamelContext();
        SqlConnectorMetaDataExtension ext = new SqlConnectorMetaDataExtension(camelContext);
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("url", database.getJdbcUrl());
        parameters.put("user", database.getUsername());
        parameters.put("password", database.getPassword());

        parameters.put("query", query);
        Optional<MetaDataExtension.MetaData> metadata = ext.meta(parameters);
        SqlMetadataRetrieval adapter = new SqlMetadataRetrieval();

        SyndesisMetadata syndesisMetaData2 = adapter.adapt(camelContext, "sql", "sql-connector", parameters,
            metadata.get());
        ObjectWriter writer = JsonUtils.writer();
        String actualMetadata =
            writer.with(writer.getConfig().getDefaultPrettyPrinter()).writeValueAsString(syndesisMetaData2);

        String expectedMetadata = IOUtils.toString(this.getClass().getResource("/sql/delete_returning_one.json"),
            StandardCharsets.UTF_8).trim();

        assertEquals(expectedMetadata, actualMetadata, JSONCompareMode.NON_EXTENSIBLE);
    }


    @After
    public void afterClass() throws SQLException {
        try (final Statement stmt = database.createConnection("").createStatement()) {
            stmt.execute("DROP table TEST");
        }
    }

}
