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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import io.syndesis.dv.datasources.DefaultSyndesisDataSource;
import io.syndesis.dv.metadata.TeiidVdb;
import io.syndesis.dv.metadata.internal.DefaultMetadataInstance;
import io.syndesis.dv.metadata.internal.TeiidDataSourceImpl;
import io.syndesis.dv.repository.RepositoryConfiguration;
import io.syndesis.dv.repository.RepositoryManagerImpl;
import io.syndesis.dv.rest.JsonMarshaller;
import io.syndesis.dv.server.endpoint.EditorService;
import io.syndesis.dv.server.endpoint.MetadataService;
import io.syndesis.dv.server.endpoint.QueryAttribute;
import io.syndesis.dv.server.endpoint.RestSchemaNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.server.ResponseStatusException;
import org.teiid.adminapi.impl.VDBMetaData;

import io.syndesis.dv.KException;

@SuppressWarnings({ "javadoc", "nls" })
@RunWith(SpringRunner.class)
@DataJpaTest
@ContextConfiguration(classes = {RepositoryConfiguration.class, ServiceTestConfiguration.class})
@DirtiesContext
public class MetadataServiceTest {
    @Autowired
    private RepositoryManagerImpl repositoryManagerImpl;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private DefaultMetadataInstance metadataInstance;

    @Test
    public void testSourceVdbGeneration() throws Exception {
//        Map<String, String> properties = new LinkedHashMap<String, String>();
//        properties.put(TeiidDataSource.DATASOURCE_JNDINAME, "something");
//        properties.put(TeiidDataSource.DATASOURCE_DRIVERNAME, "type");
//        properties.put(TeiidOpenShiftClient.ID, "source id");

        DefaultSyndesisDataSource sds = DataVirtualizationServiceTest.createH2DataSource("source");
        metadataInstance.registerDataSource(sds);

        TeiidDataSourceImpl tds = metadataInstance.getDataSource("source");

        VDBMetaData vdb = MetadataService.generateSourceVdb(tds, "vdb", null);

        String s = new String(DefaultMetadataInstance.toBytes(vdb).toByteArray(), "UTF-8");
        assertEquals(
                "<?xml version=\"1.0\" ?><vdb name=\"vdb\" version=\"1\"><description>Vdb for source Data Source:	source\n"
                        + "Type: \t\th2</description><connection-type>BY_VERSION</connection-type>"
                        + "<property name=\"id\" value=\"someid\"></property><property name=\"async-load\" value=\"true\"></property>"
                        + "<model name=\"source\" type=\"PHYSICAL\" visible=\"true\">"
                        + "<property name=\"importer.TableTypes\" value=\"TABLE,VIEW\"></property>"
                        + "<property name=\"importer.UseFullSchemaName\" value=\"false\"></property>"
                        + "<property name=\"importer.UseQualifiedName\" value=\"true\"></property>"
                        + "<property name=\"importer.UseCatalogName\" value=\"false\"></property>"
                        + "<source name=\"source\" translator-name=\"h2\" connection-jndi-name=\"source\"></source></model></vdb>",
                s);

        //with ddl passed in
        vdb = MetadataService.generateSourceVdb(tds, "vdb", "create something...");

        s = new String(DefaultMetadataInstance.toBytes(vdb).toByteArray(), "UTF-8");
        assertEquals(
                "<?xml version=\"1.0\" ?><vdb name=\"vdb\" version=\"1\"><description>Vdb for source Data Source:	source\n"
                        + "Type: \t\th2</description><connection-type>BY_VERSION</connection-type>"
                        + "<property name=\"id\" value=\"someid\"></property>"
                        + "<model name=\"source\" type=\"PHYSICAL\" visible=\"false\">"
                        + "<property name=\"importer.TableTypes\" value=\"TABLE,VIEW\"></property>"
                        + "<property name=\"importer.UseFullSchemaName\" value=\"false\"></property>"
                        + "<property name=\"importer.UseQualifiedName\" value=\"true\"></property>"
                        + "<property name=\"importer.UseCatalogName\" value=\"false\"></property>"
                        + "<source name=\"source\" translator-name=\"h2\" connection-jndi-name=\"source\"></source>"
                        + "<metadata type=\"DDLDB\"><![CDATA[someid]]></metadata></model></vdb>",
                s);

    }

    @Test
    public void testGetSchema() throws Exception {
        List<RestSchemaNode> nodes = null;
        try {
            nodes = metadataService.getSchema("source2");
            fail();
        } catch (ResponseStatusException e) {
            //no source yet
        }

        DefaultSyndesisDataSource sds = DataVirtualizationServiceTest.createH2DataSource("source2");
        metadataInstance.registerDataSource(sds);

        repositoryManagerImpl.createSchema("someid", "source",
                "create foreign table tbl (col string) options (\"teiid_rel:fqn\" 'schema=s%20x/t%20bl=bar');"
                + "create foreign table tbl1 (col string) options (\"teiid_rel:fqn\" 'schema=s%20x/t%20bl=bar1');");

        nodes = metadataService.getSchema("source");
        assertEquals("[ {\n" +
                "  \"children\" : [ {\n" +
                "    \"children\" : [ ],\n" +
                "    \"name\" : \"bar\",\n" +
                "    \"teiidName\" : \"tbl\",\n" +
                "    \"connectionName\" : \"source\",\n" +
                "    \"type\" : \"t bl\",\n" +
                "    \"queryable\" : true\n" +
                "  }, {\n" +
                "    \"children\" : [ ],\n" +
                "    \"name\" : \"bar1\",\n" +
                "    \"teiidName\" : \"tbl1\",\n" +
                "    \"connectionName\" : \"source\",\n" +
                "    \"type\" : \"t bl\",\n" +
                "    \"queryable\" : true\n" +
                "  } ],\n" +
                "  \"name\" : \"s x\",\n" +
                "  \"connectionName\" : \"source\",\n" +
                "  \"type\" : \"schema\",\n" +
                "  \"queryable\" : false\n" +
                "} ]", JsonMarshaller.marshall(nodes));
    }

    @Test
    public void testGetSchemaSingleLevel() throws Exception {
        List<RestSchemaNode> nodes = null;
        try {
            nodes = metadataService.getSchema("source3");
            fail();
        } catch (ResponseStatusException e) {
            //no source yet
        }

        //add the data source, and schema
        DefaultSyndesisDataSource sds = DataVirtualizationServiceTest.createH2DataSource("source3");
        metadataInstance.registerDataSource(sds);


        repositoryManagerImpl.createSchema("someid", "source3",
                "create foreign table tbl (col string) options (\"teiid_rel:fqn\" 'collection=bar');"
                + "create foreign table tbl1 (col string) options (\"teiid_rel:fqn\" 'collection=bar1');");

        nodes = metadataService.getSchema("source3");
        assertEquals("[ {\n" +
                "  \"children\" : [ ],\n" +
                "  \"name\" : \"bar\",\n" +
                "  \"teiidName\" : \"tbl\",\n" +
                "  \"connectionName\" : \"source3\",\n" +
                "  \"type\" : \"collection\",\n" +
                "  \"queryable\" : true\n" +
                "}, {\n" +
                "  \"children\" : [ ],\n" +
                "  \"name\" : \"bar1\",\n" +
                "  \"teiidName\" : \"tbl1\",\n" +
                "  \"connectionName\" : \"source3\",\n" +
                "  \"type\" : \"collection\",\n" +
                "  \"queryable\" : true\n" +
                "} ]", JsonMarshaller.marshall(nodes));
    }

    @Test
    public void testPreviewQuery() throws Exception {
        QueryAttribute kqa = new QueryAttribute();
        kqa.setQuery("select * from myview");
        kqa.setTarget("dv1");

        repositoryManagerImpl.createDataVirtualization("dv1");

        //get rid of the default preview vdb
        metadataInstance.undeployDynamicVdb(EditorService.PREVIEW_VDB);

        try {
            metadataService.updatePreviewVdb("dv1");
            fail();
        } catch (KException e) {
            //preveiw vdb does not exist
        }

        metadataInstance.deploy(EditorServiceTest.dummyPreviewVdb());

        //even with no views, we should still succeed
        TeiidVdb vdb = metadataService.updatePreviewVdb("dv1");

        //there will be a validity error from being empty
        assertTrue(!vdb.getValidityErrors().isEmpty());

        metadataInstance.query(vdb.getName(), "select * from v", DefaultMetadataInstance.NO_OFFSET, DefaultMetadataInstance.NO_LIMIT);
    }
}