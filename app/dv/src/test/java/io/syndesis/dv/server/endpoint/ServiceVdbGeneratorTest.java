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
package io.syndesis.dv.server.endpoint;

import static org.hamcrest.core.Is.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.metadata.MetadataInstance.ValidationResult;
import io.syndesis.dv.metadata.TeiidDataSource;
import io.syndesis.dv.metadata.TeiidVdb;
import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.model.ViewDefinition;
import io.syndesis.dv.server.endpoint.ServiceVdbGenerator.SchemaFinder;

import org.mockito.Mockito;
import org.teiid.adminapi.impl.ModelMetaData;
import org.teiid.adminapi.impl.VDBMetaData;
import org.teiid.metadata.MetadataFactory;
import org.teiid.metadata.Schema;
import org.teiid.metadata.Table;
import org.teiid.query.metadata.SystemMetadata;
import org.teiid.query.parser.QueryParser;

import io.syndesis.dv.KException;

@SuppressWarnings({ "javadoc", "nls" })
public class ServiceVdbGeneratorTest {

    private static String viewDefinitionName = "orderInfoView";
    private static String description = "test view description text";
    private boolean isComplete = true;
    private static String sourceTablePath1 = "connection=pgconnection1/table=orders";
    private static String sourceTablePath1b = "connection=pgconnection1/table=orders2";
    private static String sourceTablePath2 = "connection=pgconnection1/table=customers";
    private static String sourceTablePath3 = "connection=pgconnection2/table=customers";

    private static String FQN_TABLE_1 = "schema=public/table=orders";
    private static String FQN_TABLE_2 = "schema=public/table=orders2";
    private static String FQN_TABLE_3 = "schema=public/table=customers";

    private static final String DS_NAME = "pgconnection1";
    private static final String MODEL_NAME = "pgconnection1schemamodel";
    private static final String DS_NAME_2 = "pgconnection2";
    private static final String MODEL_NAME_2 = "pgconnection2schemamodel";

    private boolean doPrint = false;


    private final static String TABLE_OPTION_FQN = "teiid_rel:fqn"; //$NON-NLS-1$

    private final static String SET_NAMESPACE_STRING = "SET NAMESPACE 'http://www.teiid.org/ext/relational/2012' AS teiid_rel;\n\n";

    private final static String pgconnection1schemamodelDDL =
            SET_NAMESPACE_STRING +
            "CREATE FOREIGN TABLE orders ( "
            + "ID long primary key, orderDate timestamp) OPTIONS(\"" + TABLE_OPTION_FQN + "\" '" + FQN_TABLE_1 + "');\n" +
            "CREATE FOREIGN TABLE orders2 ( "
            + "ID long primary key, \"year\" string, orderDate timestamp) OPTIONS(\"" + TABLE_OPTION_FQN + "\" '" + FQN_TABLE_2 + "');\n" +
            "CREATE FOREIGN TABLE customers ( "
            + "ID long primary key, name string) OPTIONS(\"" + TABLE_OPTION_FQN + "\" '" + FQN_TABLE_3 + "');";

    private final static String pgconnection2schemamodelDDL =
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
            "CREATE VIEW orderInfoView (ID, orderDate, PRIMARY KEY(ID)) OPTIONS (ANNOTATION 'test view description text') AS \n" +
            "SELECT ID, orderDate\n" +
            "FROM pgconnection1schemamodel.orders";

    private final static String EXPECTED_NO_JOIN_SQL_SINGE_SOURCE_WITH_KEYWORD =
            "CREATE VIEW orderInfoView (ID, \"year\", orderDate, PRIMARY KEY(ID)) OPTIONS (ANNOTATION 'test view description text') AS \n" +
            "SELECT ID, \"year\", orderDate\n" +
            "FROM pgconnection1schemamodel.orders2";

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

    private Map<String, TeiidDataSource> dataSources = new HashMap<>();
    private Map<String, Schema> schemas = new HashMap<>();

    @Before
    public void init() throws Exception {
        addSourceInfo(DS_NAME, pgconnection1schemamodelDDL, MODEL_NAME);
        addSourceInfo(DS_NAME_2, pgconnection2schemamodelDDL, MODEL_NAME_2);
    }

    private void addSourceInfo(String connectionName, String ddl, String modelName) {
        DefaultSyndesisDataSource sds = DataVirtualizationServiceTest.createH2DataSource(connectionName);
        dataSources.put(connectionName, sds.createDataSource());

        MetadataFactory mf = new MetadataFactory("x", 1, modelName, SystemMetadata.getInstance().getRuntimeTypeMap(), new Properties(), null);
        QueryParser.getQueryParser().parseDDL(mf, ddl);
        schemas.put(connectionName, mf.getSchema());
    }

    private String helpGenerateDdlForWithJoinType(String secondSourceTablePath, String joinType, boolean singleConnection, boolean useAll) throws KException {
        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        String[] sourceTablePaths = { sourceTablePath1, secondSourceTablePath };

        ViewDefinition viewDef = mock(ViewDefinition.class);
        when(viewDef.getName()).thenReturn(viewDefinitionName);
        when(viewDef.getDescription()).thenReturn(description);
        when(viewDef.isComplete()).thenReturn(isComplete);
        when(viewDef.getSourcePaths()).thenReturn(Arrays.asList(sourceTablePaths));

        return vdbGenerator.getODataViewDdl(viewDef);
    }

    private ViewDefinition helpCreateViewEditorState(int numSources) throws KException {

        ViewDefinition viewDef = new ViewDefinition("dvName", viewDefinitionName);
        if( numSources == 1 ) {
            helpCreateViewDefinitionAll(viewDef, sourceTablePath2, false);
        } else {
            helpCreateViewDefinitionAll(viewDef, sourceTablePath3, false);
        }

        return viewDef;
    }

    private ViewDefinition helpCreateViewDefinitionAll(ViewDefinition viewDef, String secondSourceTablePath, boolean useAll) throws KException {

        String[] sourceTablePaths = { sourceTablePath1, secondSourceTablePath };

        viewDef.setDescription(description);
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
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_NoJoinOneTable() throws Exception {
        String EXPECTED_DDL = EXPECTED_NO_JOIN_SQL_SINGE_SOURCE;

        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        String[] sourceTablePaths = { sourceTablePath1 };
        ViewDefinition viewDef = mock(ViewDefinition.class);
        when(viewDef.getName()).thenReturn(viewDefinitionName);
        when(viewDef.getDescription()).thenReturn(description);
        when(viewDef.isComplete()).thenReturn(isComplete);
        when(viewDef.getSourcePaths()).thenReturn(Arrays.asList(sourceTablePaths));

        String viewDdl = vdbGenerator.getODataViewDdl(viewDef);
        printResults(EXPECTED_DDL, viewDdl);
        assertEquals(EXPECTED_DDL, viewDdl);
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_NoJoinOneTable_withKeywordCol() throws Exception {
        String EXPECTED_DDL = EXPECTED_NO_JOIN_SQL_SINGE_SOURCE_WITH_KEYWORD;

        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        String[] sourceTablePaths = { sourceTablePath1b };
        ViewDefinition viewDef = mock(ViewDefinition.class);
        when(viewDef.getName()).thenReturn(viewDefinitionName);
        when(viewDef.getDescription()).thenReturn(description);
        when(viewDef.isComplete()).thenReturn(isComplete);
        when(viewDef.getSourcePaths()).thenReturn(Arrays.asList(sourceTablePaths));

        String viewDdl = vdbGenerator.getODataViewDdl(viewDef);
        printResults(EXPECTED_DDL, viewDdl);
        assertEquals(EXPECTED_DDL, viewDdl);
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_InnerJoinAll() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + INNER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ServiceVdbGenerator.JOIN_INNER, true, true);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_InnerJoin() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + INNER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ServiceVdbGenerator.JOIN_INNER, true, false);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_LeftOuterJoinAll() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + LEFT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ServiceVdbGenerator.JOIN_LEFT_OUTER, true, true);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_LeftOuterJoin() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + LEFT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ServiceVdbGenerator.JOIN_LEFT_OUTER, true, false);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_RightOuterJoinAll() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + RIGHT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ServiceVdbGenerator.JOIN_RIGHT_OUTER, true, true);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_RightOuterJoin() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + RIGHT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ServiceVdbGenerator.JOIN_RIGHT_OUTER, true, false);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_FullOuterJoinAll() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + FULL_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ServiceVdbGenerator.JOIN_FULL_OUTER, true, true);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithSingleSourceViewDefinition_FullOuterJoin() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_SINGE_SOURCE_START + FULL_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_SINGLE_SOURCE_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath2, ServiceVdbGenerator.JOIN_FULL_OUTER, true, false);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_InnerJoinAll() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + INNER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ServiceVdbGenerator.JOIN_INNER, false, true);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_InnerJoin() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + INNER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ServiceVdbGenerator.JOIN_INNER, false, false);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_LeftOuterJoinAll() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + LEFT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ServiceVdbGenerator.JOIN_LEFT_OUTER, false, true);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_LeftOuterJoin() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + LEFT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ServiceVdbGenerator.JOIN_LEFT_OUTER, false, false);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_RightOuterJoinAll() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + RIGHT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ServiceVdbGenerator.JOIN_RIGHT_OUTER, false, true);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_RightOuterJoin() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + RIGHT_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ServiceVdbGenerator.JOIN_RIGHT_OUTER, false, false);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_FullOuterJoinAll() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + FULL_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ServiceVdbGenerator.JOIN_FULL_OUTER, false, true);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldGenerateOdataViewDDL_WithTwoSourcesViewDefinition_FullOuterJoin() throws Exception {
        String EXPECTED_DDL = EXPECTED_JOIN_SQL_TWO_SOURCES_START + FULL_OUTER_JOIN_STR + EXPECTED_JOIN_SQL_TWO_SOURCES_END;
        String viewDdl = helpGenerateDdlForWithJoinType(sourceTablePath3, ServiceVdbGenerator.JOIN_FULL_OUTER, false, false);
        printResults(EXPECTED_DDL, viewDdl);
        // TODO Uncomment after JOINs are working
        // assertThat(viewDdl, is(EXPECTED_DDL));
    }

    @Test
    public void shouldRefreshServiceVdb_SingleSource() throws Exception {
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

        VDBMetaData serviceVdb = vdbGenerator.createServiceVdb("servicevdb", mock, Arrays.asList(state));

        assertEquals("<?xml version=\"1.0\" ?><vdb name=\"servicevdb\" version=\"1\"><connection-type>BY_VERSION</connection-type><model name=\"servicevdb\" type=\"VIRTUAL\" visible=\"true\"><metadata type=\"DDL\"><![CDATA[CREATE VIEW orderInfoView (ID, orderDate, name) OPTIONS (ANNOTATION 'test view description text') AS \n" +
                "SELECT t1.ID, t1.orderDate, t2.name\n" +
                "FROM pgconnection1schemamodel.orders AS t1;\n" +
                "]]></metadata></model><model name=\"pgconnection1schemamodel\" type=\"PHYSICAL\" visible=\"false\"><metadata type=\"DDL\"><![CDATA[SET NAMESPACE 'http://www.teiid.org/ext/relational/2012' AS teiid_rel;\n" +
                "\n" +
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

        assertThat(models.size(), is(2));
        ModelMetaData viewModel = serviceVdb.getModel("servicevdb");
        assertNotNull(viewModel);
        assertEquals("CREATE VIEW orderInfoView (ID, orderDate, name) OPTIONS (ANNOTATION 'test view description text') AS \n" +
                "SELECT t1.ID, t1.orderDate, t2.name\n" +
                "FROM pgconnection1schemamodel.orders AS t1;\n", viewModel.getSourceMetadataText().get(0));

    }

    @Test
    public void shouldRefreshServiceVdb_TwoSources() throws Exception {
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

        VDBMetaData serviceVdb = vdbGenerator.createServiceVdb("servicevdb", mock, Arrays.asList(state));

        List<org.teiid.adminapi.Model> models = serviceVdb.getModels();

        assertThat(models.size(), is(3));
        ModelMetaData viewModel = serviceVdb.getModel("servicevdb");
        assertNotNull(viewModel);
        assertEquals("CREATE VIEW orderInfoView (ID, orderDate, customerName) OPTIONS (ANNOTATION 'test view description text') AS \n" +
                "SELECT t1.ID, t1.orderDate, t2.customerName\n" +
                "FROM pgconnection1schemamodel.orders AS t1;\n", viewModel.getSourceMetadataText().get(0));

    }

    @Test
    public void shouldRefreshServiceVdbPreviewNoViews() throws Exception {
        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        VDBMetaData serviceVdb = vdbGenerator.createPreviewVdb("dv", "preview", Collections.emptyList());

        assertEquals("<?xml version=\"1.0\" ?><vdb name=\"preview\" version=\"1\"><connection-type>BY_VERSION</connection-type><property name=\"preview\" value=\"true\"></property><import-vdb name=\"Preview\" version=\"1\" import-data-policies=\"true\"></import-vdb><model name=\"dv\" type=\"VIRTUAL\" visible=\"true\"><metadata type=\"DDL\"><![CDATA[]]></metadata></model></vdb>", new String(DefaultMetadataInstance.toBytes(serviceVdb).toByteArray(), "UTF-8"));
    }

    @Test
    public void shouldGenerateEmptyView() throws Exception {
        ViewDefinition view = new ViewDefinition("x", "y");
        view.setComplete(true);

        ServiceVdbGenerator vdbGenerator = new ServiceVdbGenerator(schemaFinder());

        assertEquals("CREATE VIEW y AS \n" +
                "SELECT 1 as col;", vdbGenerator.getODataViewDdl(view));
    }


    protected ServiceVdbGenerator.SchemaFinder schemaFinder() {
        return new SchemaFinder() {

            @Override
            public TeiidDataSource findTeiidDatasource(String connectionName) throws KException {
                return dataSources.get(connectionName);
            }

            @Override
            public Schema findSchema(String connectionName) throws KException {
                return schemas.get(connectionName);
            }

        };
    }

}
