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
package io.syndesis.dv.server.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.metadata.MetadataInstance.ValidationResult;
import io.syndesis.dv.metadata.TeiidDataSource;
import io.syndesis.dv.metadata.TeiidVdb;
import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.model.TablePrivileges;
import io.syndesis.dv.model.TablePrivileges.Privilege;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.server.endpoint.ServiceVdbGenerator.SchemaFinder;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.metadata.Column;
import org.teiid.metadata.MetadataFactory;
import org.teiid.metadata.Schema;
import org.teiid.metadata.Table;
import org.teiid.query.metadata.SystemMetadata;
import org.teiid.query.parser.QueryParser;

@SuppressWarnings({ "javadoc", "nls" })
public class ServiceVdbGeneratorTest {

    private static final String VIEW_DEFINITION_NAME = "orderInfoView";
    private static final String DESCRIPTION = "test view description text";
    private final boolean isComplete = true;
    private static final String SOURCE_TABLE_PATH_1 = "connection=pgconnection1/table=orders";
    private static final String SOURCE_TABLE_PATH_1B = "connection=pgconnection1/table=orders2";
    private static final String SOURCE_TABLE_PATH_2 = "connection=pgconnection1/table=customers";
    private static final String SOURCE_TABLE_PATH_3 = "connection=pgconnection2/table=customers";

    private static final String FQN_TABLE_1 = "schema=public/table=orders";
    private static final String FQN_TABLE_2 = "schema=public/table=orders2";
    private static final String FQN_TABLE_3 = "schema=public/table=customers";

    private static final String DS_NAME = "pgconnection1";
    private static final String MODEL_NAME = "pgconnection1schemamodel";
    private static final String DS_NAME_2 = "pgconnection2";
    private static final String MODEL_NAME_2 = "pgconnection2schemamodel";

    private final boolean doPrint = false;


    private final static String TABLE_OPTION_FQN = "teiid_rel:fqn"; //$NON-NLS-1$

    private final static String SET_NAMESPACE_STRING = "SET NAMESPACE 'http://www.teiid.org/ext/relational/2012' AS teiid_rel;\n\n";

    private final static String PG_CONNECTION_1_SCHEMA_MODEL_DDL =
            SET_NAMESPACE_STRING +
            "CREATE FOREIGN TABLE orders ( "
            + "ID long primary key, orderDate timestamp) OPTIONS(\"" + TABLE_OPTION_FQN + "\" '" + FQN_TABLE_1 + "');\n" +
            "CREATE FOREIGN TABLE orders2 ( "
            + "ID long primary key, \"year\" string, orderDate timestamp) OPTIONS(\"" + TABLE_OPTION_FQN + "\" '" + FQN_TABLE_2 + "');\n" +
            "CREATE FOREIGN TABLE customers ( "
            + "ID long primary key, name string) OPTIONS(\"" + TABLE_OPTION_FQN + "\" '" + FQN_TABLE_3 + "');";

    private final static String PG_CONNECTION_2_SCHEMA_MODEL_DDL =
            SET_NAMESPACE_STRING +
            "CREATE FOREIGN TABLE orders ( "
            + "ID long primary key, orderDate timestamp) OPTIONS(\"" + TABLE_OPTION_FQN + "\" '" + FQN_TABLE_1 + "');\n" +
            "CREATE FOREIGN TABLE orders2 ( "
            + "ID long primary key, \"year\" string, orderDate timestamp) OPTIONS(\"" + TABLE_OPTION_FQN + "\" '" + FQN_TABLE_2 + "');\n" +
            "CREATE FOREIGN TABLE customers ( "
            + "ID long primary key, customerName string) OPTIONS(\"" + TABLE_OPTION_FQN + "\" '" + FQN_TABLE_3 + "');";

    private final static String EXPECTED_JOIN_SQL_TWO_SOURCES_START =
            "CREATE VIEW orderInfoView (RowId long PRIMARY KEY, ID LONG, orderDate TIMESTAMP, customerName STRING) OPTIONS (ANNOTATION 'test view description text') AS \n"
          + "SELECT ROW_NUMBER() OVER (ORDER BY A.ID), A.ID, A.orderDate, B.customerName\n"
          + "FROM pgconnection1schemamodel.orders AS A \n";

    private final static String  EXPECTED_JOIN_SQL_TWO_SOURCES_END = "pgconnection2schemamodel.customers AS B \n"
            + "ON \n"
            + "A.ID = B.ID;";

    private final static String EXPECTED_JOIN_SQL_SINGE_SOURCE_START =
            "CREATE VIEW orderInfoView (RowId long PRIMARY KEY, ID LONG, orderDate TIMESTAMP, name STRING) OPTIONS (ANNOTATION 'test view description text') AS \n"
          + "SELECT ROW_NUMBER() OVER (ORDER BY A.ID), A.ID, A.orderDate, B.name\n"
          + "FROM pgconnection1schemamodel.orders AS A \n";

    private final static String  EXPECTED_JOIN_SQL_SINGLE_SOURCE_END = "pgconnection1schemamodel.customers AS B \n"
          + "ON \n"
          + "A.ID = B.ID;";

    private final static String EXPECTED_NO_JOIN_SQL_SINGE_SOURCE =
            "CREATE VIEW orderInfoView (\n" +
            "  ID, orderDate, PRIMARY KEY(ID)\n" +
            ") OPTIONS (ANNOTATION 'test view description text') AS \n" +
            "  SELECT \n" +
            "    t1.ID, t1.orderDate\n" +
            "  FROM \n" +
            "    pgconnection1schemamodel.orders AS t1";

    private final static String EXPECTED_NO_JOIN_SQL_SINGE_SOURCE_WITH_KEYWORD =
            "CREATE VIEW orderInfoView (\n" +
            "  ID, \"year\", orderDate, PRIMARY KEY(ID)\n" +
            ") OPTIONS (ANNOTATION 'test view description text') AS \n" +
            "  SELECT \n" +
            "    t1.ID, t1.\"year\", t1.orderDate\n" +
            "  FROM \n" +
            "    pgconnection1schemamodel.orders2 AS t1";

    // ===========================
    // orders
    //  - ID LONG
    //  - orderDate TIMESTAMP
    //  - orderName STRING
    //  - orderDesc STRING
    //
    // customers
    //  - ID LONG
    //  - customerName STRING
    //  - customerAddress STRING
    //  - customerState STRING

    // ===========================

    private final static String INNER_JOIN_STR = "INNER JOIN \n";
    private final static String LEFT_OUTER_JOIN_STR = "LEFT OUTER JOIN \n";
    private final static String RIGHT_OUTER_JOIN_STR = "RIGHT OUTER JOIN \n";
    private final static String FULL_OUTER_JOIN_STR = "FULL OUTER JOIN \n";

    private final Map<String, TeiidDataSource> dataSources = new HashMap<>();
    private final Map<String, Schema> schemas = new HashMap<>();

    @Before
    public void init() {
        addSourceInfo(DS_NAME, PG_CONNECTION_1_SCHEMA_MODEL_DDL, MODEL_NAME);
        addSourceInfo(DS_NAME_2, PG_CONNECTION_2_SCHEMA_MODEL_DDL, MODEL_NAME_2);
    }

    private void addSourceInfo(String connectionName, String ddl, String modelName) {
        DefaultSyndesisDataSource sds = DataVirtualizationServiceTest.createH2DataSource(connectionName);
        dataSources.put(connectionName, sds.createDataSource());

        MetadataFactory mf = new MetadataFactory("x", 1, modelName, SystemMetadata.getInstance().getRuntimeTypeMap(), new Properties(), null);
        QueryParser.getQueryParser().parseDDL(mf, ddl);
        schemas.put(connectionName, mf.getSchema());
    }

    private String helpGenerateDdlForWithJoinType(String secondSourceTablePath) {
        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        String[] sourceTablePaths = { SOURCE_TABLE_PATH_1, secondSourceTablePath };

        ViewDefinition viewDef = mock(ViewDefinition.class);
        when(viewDef.getName()).thenReturn(VIEW_DEFINITION_NAME);
        when(viewDef.getDescription()).thenReturn(DESCRIPTION);
        when(viewDef.isComplete()).thenReturn(isComplete);
        when(viewDef.getSourcePaths()).thenReturn(Arrays.asList(sourceTablePaths));

        return vdbGenerator.getODataViewDdl(viewDef);
    }

    private String helpGenerateDdlFor(String ...tablePath) {
        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());
        ViewDefinition viewDef = mock(ViewDefinition.class);
        when(viewDef.getName()).thenReturn(VIEW_DEFINITION_NAME);
        when(viewDef.getDescription()).thenReturn(DESCRIPTION);
        when(viewDef.isComplete()).thenReturn(isComplete);
        when(viewDef.getSourcePaths()).thenReturn(Arrays.asList(tablePath));

        return vdbGenerator.getODataViewDdl(viewDef);
    }

    private ViewDefinition helpCreateViewEditorState(int numSources) {

        ViewDefinition viewDef = new ViewDefinition("dvName", VIEW_DEFINITION_NAME);
        viewDef.setId("1");
        if( numSources == 1 ) {
            helpCreateViewDefinitionAll(viewDef, SOURCE_TABLE_PATH_2);
        } else {
            helpCreateViewDefinitionAll(viewDef, SOURCE_TABLE_PATH_3);
        }

        return viewDef;
    }

    private ViewDefinition helpCreateViewDefinitionAll(ViewDefinition viewDef, String secondSourceTablePath) {

        String[] sourceTablePaths = { SOURCE_TABLE_PATH_1, secondSourceTablePath };

        viewDef.setDescription(DESCRIPTION);
        viewDef.setComplete(isComplete);
        viewDef.setSourcePaths(Arrays.asList(sourceTablePaths));

        return viewDef;
    }

    private void printResults(String expected, String generated) {
        if( doPrint ) {
            System.out.println("\nServiceVdbGeneratorTest\n    EXPECTED DDL = \n" + expected);
            System.out.println("\nServiceVdbGeneratorTest\n    GENERATED DDL = \n" + generated);
        }
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_NoJoinOneTable() {
        String EXPECTED_DDL = EXPECTED_NO_JOIN_SQL_SINGE_SOURCE;

        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        String[] sourceTablePaths = { SOURCE_TABLE_PATH_1 };
        ViewDefinition viewDef = mock(ViewDefinition.class);
        when(viewDef.getName()).thenReturn(VIEW_DEFINITION_NAME);
        when(viewDef.getDescription()).thenReturn(DESCRIPTION);
        when(viewDef.isComplete()).thenReturn(isComplete);
        when(viewDef.getSourcePaths()).thenReturn(Arrays.asList(sourceTablePaths));

        String viewDdl = vdbGenerator.getODataViewDdl(viewDef);
        printResults(EXPECTED_DDL, viewDdl);
        assertEquals(EXPECTED_DDL, viewDdl);
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_NoJoinOneTable_withKeywordCol() {
        String EXPECTED_DDL = EXPECTED_NO_JOIN_SQL_SINGE_SOURCE_WITH_KEYWORD;

        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        String[] sourceTablePaths = { SOURCE_TABLE_PATH_1B };
        ViewDefinition viewDef = mock(ViewDefinition.class);
        when(viewDef.getName()).thenReturn(VIEW_DEFINITION_NAME);
        when(viewDef.getDescription()).thenReturn(DESCRIPTION);
        when(viewDef.isComplete()).thenReturn(isComplete);
        when(viewDef.getSourcePaths()).thenReturn(Arrays.asList(sourceTablePaths));

        String viewDdl = vdbGenerator.getODataViewDdl(viewDef);
        printResults(EXPECTED_DDL, viewDdl);
        assertEquals(EXPECTED_DDL, viewDdl);
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_InnerJoinAll() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + INNER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_2);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_InnerJoin() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + INNER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_2);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_LeftOuterJoinAll() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + LEFT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_2);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_LeftOuterJoin() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + LEFT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_2);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_RightOuterJoinAll() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + RIGHT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_2);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_RightOuterJoin() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + RIGHT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_2);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_FullOuterJoinAll() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + FULL_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_2);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_FullOuterJoin() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + FULL_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_2);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_InnerJoinAll() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + INNER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_3);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_InnerJoin() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + INNER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_3);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_LeftOuterJoinAll() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + LEFT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_3);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_LeftOuterJoin() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + LEFT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_3);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_RightOuterJoinAll() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + RIGHT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_3);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void twoTables() {
        String EXPECTED_DDL = "CREATE VIEW orderInfoView (\n" +
                              "  ID, orderDate\n" +
                              "  /*,ID, customerName*/, \n" +
                              "  PRIMARY KEY(ID)\n" +
                              ") OPTIONS (ANNOTATION 'test view description text') AS \n" +
                              "  SELECT \n" +
                              "    t1.ID, t1.orderDate\n" +
                              "    /*,t2.ID, t2.customerName*/\n" +
                              "  FROM \n" +
                              "    pgconnection1schemamodel.orders AS t1\n" +
                              "    /*, [INNER|LEFT OUTER|RIGHT OUTER] JOIN pgconnection2schemamodel.customers AS t2 ON t1.ID=t2.<?>*/";
        String viewDdl = helpGenerateDdlFor(SOURCE_TABLE_PATH_1, SOURCE_TABLE_PATH_3);
        printResults(EXPECTED_DDL, viewDdl);
        assertEquals(EXPECTED_DDL, viewDdl);
    }

    @Test
    public void threeTables() {
        String EXPECTED_DDL = "CREATE VIEW orderInfoView (\n" +
                              "  ID, orderDate\n" +
                              "  /*,ID, customerName*/\n" +
                              "  /*,ID, customerName*/, \n" +
                              "  PRIMARY KEY(ID)\n" +
                              ") OPTIONS (ANNOTATION 'test view description text') AS \n" +
                              "  SELECT \n" +
                              "    t1.ID, t1.orderDate\n" +
                              "    /*,t2.ID, t2.customerName*/\n" +
                              "    /*,t3.ID, t3.customerName*/\n" +
                              "  FROM \n" +
                              "    pgconnection1schemamodel.orders AS t1\n" +
                              "    /*, [INNER|LEFT OUTER|RIGHT OUTER] JOIN pgconnection2schemamodel.customers AS t2 ON t1.ID=t2.<?>*/\n" +
                              "    /*, [INNER|LEFT OUTER|RIGHT OUTER] JOIN pgconnection2schemamodel.customers AS t3 ON t1.ID=t3.<?>*/";
        String viewDdl = helpGenerateDdlFor(SOURCE_TABLE_PATH_1, SOURCE_TABLE_PATH_3, SOURCE_TABLE_PATH_3);
        printResults(EXPECTED_DDL, viewDdl);
        assertEquals(EXPECTED_DDL, viewDdl);
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_RightOuterJoin() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + RIGHT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_3);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_FullOuterJoinAll() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + FULL_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_3);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_FullOuterJoin() {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + FULL_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(SOURCE_TABLE_PATH_3);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldRefreshServiceVdb_SingleSource() throws UnsupportedEncodingException {
        ViewDefinition state = helpCreateViewEditorState(1);

        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        state.setDdl(vdbGenerator.getODataViewDdl(state));

        TeiidVdb mock = Mockito.mock(TeiidVdb.class);
        Mockito.when(mock.getName()).thenReturn("servicevdb");
        ValidationResult result = new DefaultMetadataInstance().parse(state.getDdl());
        Table t = result.getSchema().getTables().firstEntry().getValue();
        t.setIncomingObjects(new ArrayList<>());
        t.getIncomingObjects().add(schemas.get("pgconnection1").getTable("orders"));
        t.getIncomingObjects().add(schemas.get("pgconnection1").getTable("customers"));
        Mockito.when(mock.getSchema("servicevdb")).thenReturn(result.getSchema());

        VDBMetaData serviceVdb = vdbGenerator.createServiceVdb("servicevdb", mock, Arrays.asList(state), null);

        assertEquals("<?xml version=\"1.0\" ?><vdb name=\"servicevdb\" version=\"1\"><connection-type>BY_VERSION</connection-type>" +
                "<property name=\"hidden-qualified\" value=\"true\"></property>" +
                "<model name=\"servicevdb\" type=\"VIRTUAL\" visible=\"true\"><metadata type=\"DDL\"><![CDATA[CREATE VIEW orderInfoView (\n" +
                "  ID, orderDate\n" +
                "  /*,ID, name*/, \n" +
                "  PRIMARY KEY(ID)\n" +
                ") OPTIONS (ANNOTATION 'test view description text') AS \n" +
                "  SELECT \n" +
                "    t1.ID, t1.orderDate\n" +
                "    /*,t2.ID, t2.name*/\n" +
                "  FROM \n" +
                "    pgconnection1schemamodel.orders AS t1\n" +
                "    /*, [INNER|LEFT OUTER|RIGHT OUTER] JOIN pgconnection1schemamodel.customers AS t2 ON t1.ID=t2.<?>*/;\n" +
                "]]></metadata></model><model name=\"pgconnection1schemamodel\" type=\"PHYSICAL\" visible=\"false\"><metadata type=\"DDL\"><![CDATA[" +
                "CREATE FOREIGN TABLE orders (\n" +
                "\tID long,\n" +
                "\torderDate timestamp,\n" +
                "\tPRIMARY KEY(ID)\n" +
                ") OPTIONS (\"teiid_rel:fqn\" 'schema=public/table=orders');\n" +
                "\n" +
                "CREATE FOREIGN TABLE customers (\n" +
                "\tID long,\n" +
                "\tname string,\n" +
                "\tPRIMARY KEY(ID)\n" +
                ") OPTIONS (\"teiid_rel:fqn\" 'schema=public/table=customers');]]></metadata></model></vdb>", new String(DefaultMetadataInstance.toBytes(serviceVdb).toByteArray(), "UTF-8"));

        List<org.teiid.adminapi.Model> models = serviceVdb.getModels();

        assertThat(models).hasSize(2);
        ModelMetaData viewModel = serviceVdb.getModel("servicevdb");
        assertNotNull(viewModel);
        assertEquals("CREATE VIEW orderInfoView (\n" +
                "  ID, orderDate\n" +
                "  /*,ID, name*/, \n" +
                "  PRIMARY KEY(ID)\n" +
                ") OPTIONS (ANNOTATION 'test view description text') AS \n" +
                "  SELECT \n" +
                "    t1.ID, t1.orderDate\n" +
                "    /*,t2.ID, t2.name*/\n" +
                "  FROM \n" +
                "    pgconnection1schemamodel.orders AS t1\n" +
                "    /*, [INNER|LEFT OUTER|RIGHT OUTER] JOIN pgconnection1schemamodel.customers AS t2 ON t1.ID=t2.<?>*/;\n",
                viewModel.getSourceMetadataText().get(0));

        //Add some role info
        List<TablePrivileges> privileges = new ArrayList<>();
        privileges.add(new TablePrivileges("x", "1", Privilege.I));
        privileges.add(new TablePrivileges("y", "1", Privilege.I));
        privileges.add(new TablePrivileges(ServiceVdbGenerator.ANY_AUTHENTICATED, "1", Privilege.S));
        privileges.add(new TablePrivileges("x", "doesn't exist", Privilege.S));

        serviceVdb = vdbGenerator.createServiceVdb("servicevdb", mock, Arrays.asList(state), privileges);

        String actual = new String(DefaultMetadataInstance.toBytes(serviceVdb).toByteArray(), "UTF-8");
        actual = actual.substring(actual.indexOf("<data-role"));

        assertEquals("<data-role name=\"x\" any-authenticated=\"false\" grant-all=\"false\">"
                + "<permission><resource-name>servicevdb.orderInfoView</resource-name><resource-type>TABLE</resource-type><allow-create>true</allow-create></permission><mapped-role-name>x</mapped-role-name></data-role>"
                + "<data-role name=\"y\" any-authenticated=\"false\" grant-all=\"false\">"
                + "<permission><resource-name>servicevdb.orderInfoView</resource-name><resource-type>TABLE</resource-type><allow-create>true</allow-create></permission><mapped-role-name>y</mapped-role-name></data-role>"
                + "<data-role name=\"any authenticated\" any-authenticated=\"true\" grant-all=\"false\">"
                + "<permission><resource-name>servicevdb.orderInfoView</resource-name><resource-type>TABLE</resource-type><allow-read>true</allow-read></permission></data-role></vdb>", actual);
    }

    @Test
    public void shouldRefreshServiceVdb_TwoSources() {
        ViewDefinition state = helpCreateViewEditorState(2);

        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        state.setDdl(vdbGenerator.getODataViewDdl(state));

        TeiidVdb mock = Mockito.mock(TeiidVdb.class);
        Mockito.when(mock.getName()).thenReturn("servicevdb");
        ValidationResult result = new DefaultMetadataInstance().parse(state.getDdl());
        Table t = result.getSchema().getTables().firstEntry().getValue();
        t.setIncomingObjects(new ArrayList<>());
        t.getIncomingObjects().add(schemas.get("pgconnection1").getTable("orders"));
        t.getIncomingObjects().add(schemas.get("pgconnection2").getTable("customers"));
        Mockito.when(mock.getSchema("servicevdb")).thenReturn(result.getSchema());

        VDBMetaData serviceVdb = vdbGenerator.createServiceVdb("servicevdb", mock, Arrays.asList(state), null);

        List<org.teiid.adminapi.Model> models = serviceVdb.getModels();

        assertThat(models).hasSize(3);
        ModelMetaData viewModel = serviceVdb.getModel("servicevdb");
        assertNotNull(viewModel);
        assertEquals("CREATE VIEW orderInfoView (\n" +
                     "  ID, orderDate\n" +
                     "  /*,ID, customerName*/, \n" +
                     "  PRIMARY KEY(ID)\n" +
                     ") OPTIONS (ANNOTATION 'test view description text') AS \n" +
                     "  SELECT \n" +
                     "    t1.ID, t1.orderDate\n" +
                     "    /*,t2.ID, t2.customerName*/\n" +
                     "  FROM \n" +
                     "    pgconnection1schemamodel.orders AS t1\n" +
                     "    /*, [INNER|LEFT OUTER|RIGHT OUTER] JOIN pgconnection2schemamodel.customers AS t2 ON t1.ID=t2.<?>*/;\n"
                     , viewModel.getSourceMetadataText().get(0));

    }

    @Test
    public void shouldRefreshServiceVdbPreviewNoViews() throws UnsupportedEncodingException {
        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        VDBMetaData serviceVdb = vdbGenerator.createPreviewVdb("dv", "preview", Collections.emptyList());

        assertEquals("<?xml version=\"1.0\" ?><vdb name=\"preview\" version=\"1\"><connection-type>BY_VERSION</connection-type><property name=\"hidden-qualified\" value=\"true\"></property><property name=\"preview\" value=\"true\"></property><import-vdb name=\"Preview\" version=\"1\" import-data-policies=\"true\"></import-vdb><model name=\"dv\" type=\"VIRTUAL\" visible=\"true\"><metadata type=\"DDL\"><![CDATA[]]></metadata></model></vdb>", new String(DefaultMetadataInstance.toBytes(serviceVdb).toByteArray(), "UTF-8"));
    }

    @Test
    public void shouldGenerateEmptyView() {
        ViewDefinition view = new ViewDefinition("x", "y");
        view.setComplete(true);

        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        assertEquals("CREATE VIEW y AS \n" +
                "SELECT 1 as col;", vdbGenerator.getODataViewDdl(view));
    }

    @Test
    public void shouldGenerateVirtualSource() {
        ViewDefinition view = new ViewDefinition("x", "y");
        view.addSourcePath("schema=$dv/view=v");
        view.setComplete(true);

        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        assertEquals("CREATE VIEW y (\n" +
                "  col\n" +
                ") AS \n" +
                "  SELECT \n" +
                "    t1.col\n" +
                "  FROM \n" +
                "    v AS t1", vdbGenerator.getODataViewDdl(view));
    }
    protected ServiceVdbGenerator.SchemaFinder schemaFinder() {
        return new SchemaFinder() {

            @Override
            public TeiidDataSource findTeiidDatasource(String connectionName) {
                return dataSources.get(connectionName);
            }

            @Override
            public Schema findConnectionSchema(String connectionName) {
                return schemas.get(connectionName);
            }

            @Override
            public Schema findVirtualSchema(String virtualization) {
                Schema dummy = new Schema();
                dummy.setPhysical(false);
                Table table = new Table();
                table.setName("v");
                table.setVirtual(true);
                Column c = new Column();
                c.setName("col");
                c.setDatatype(SystemMetadata.getInstance().getRuntimeTypeMap().get("string"));
                table.addColumn(c);
                dummy.addTable(table);
                return dummy;
            }

        };
    }

}
