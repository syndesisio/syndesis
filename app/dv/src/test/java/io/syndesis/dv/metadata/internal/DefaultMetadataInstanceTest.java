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

package io.syndesis.dv.metadata.internal;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import io.syndesis.dv.metadata.MetadataInstance.ValidationResult;
import io.syndesis.dv.metadata.TeiidVdb;
import io.syndesis.dv.metadata.internal.DefaultMetadataInstance.TeiidVdbImpl;
import org.teiid.adminapi.impl.VDBMetadataParser;
import org.teiid.runtime.EmbeddedConfiguration;

import io.syndesis.dv.KException;

@SuppressWarnings("nls")
public class DefaultMetadataInstanceTest {

    DefaultMetadataInstance metadataInstance;
    TeiidServer server;

    @Before
    public void init() {
        EmbeddedConfiguration ec = new EmbeddedConfiguration();
        server = new TeiidServer();
        server.start(ec);

        metadataInstance = new DefaultMetadataInstance(server);
    }

    @After
    public void tearDown() {
        server.stop();
    }

    @Test
    public void shouldParse() throws KException {
        ValidationResult result = metadataInstance.parse("create view v as select 1");
        assertNull(result.getMetadataException());
    }

    @Test
    public void shouldFailParse() throws KException {
        ValidationResult result = metadataInstance.parse("create view v as ");
        assertNotNull(result.getMetadataException());
    }

    @Test
    public void shouldValidate() throws Exception {
        String vdb = "<vdb name=\"myservice\" version=\"1\">\n" +
                "    <model visible=\"true\" name=\"accounts\" type=\"VIRTUAL\">\n" +
                "      <metadata type=\"DDL\">create view tbl (col) as select 1;</metadata>" +
                "    </model>    \n" +
                "</vdb>";

        metadataInstance.deploy(VDBMetadataParser.unmarshell(new ByteArrayInputStream(vdb.getBytes("UTF-8"))));

        ValidationResult report = metadataInstance.getVdb("myservice").validate("create view v as select * from tbl");
        assertFalse(report.getReport().toString(), report.getReport().hasItems());

        report = metadataInstance.getVdb("myservice").validate("create view v as select * from tbl1");
        assertTrue(report.toString(), report.getReport().hasItems());
    }

    @Test
    public void testHasLoaded() throws Exception {
        String vdb = "<vdb name=\"myservice\" version=\"1\">\n" +
                "    <model visible=\"true\" name=\"accounts\" type=\"VIRTUAL\">\n" +
                "      <metadata type=\"DDL\">create view tbl (col) as select 1;</metadata>" +
                "    </model>    \n" +
                "</vdb>";

        metadataInstance.deploy(VDBMetadataParser.unmarshell(new ByteArrayInputStream(vdb.getBytes("UTF-8"))));

        TeiidVdbImpl vdb2 = metadataInstance.getVdb("myservice");
        assertTrue(vdb2.hasLoaded());
        assertTrue(vdb2.isActive());

        metadataInstance.undeployDynamicVdb("myservice");

        assertTrue(vdb2.hasLoaded());
        assertFalse(vdb2.isActive());
    }

    @Test
    public void shouldFindValidationErrors() throws Exception {
        String vdb = "<vdb name=\"myservice\" version=\"1\">\n" +
                "    <property name=\"preview\" value=\"true\"/>" +
                "    <model visible=\"true\" name=\"views\" type=\"VIRTUAL\">\n" +
                "      <metadata type=\"DDL\">create view tbl (col) as select 1; create view tbl2 (col string) as select 1;</metadata>" +
                "    </model>    \n" +
                "</vdb>";

        metadataInstance.deploy(VDBMetadataParser.unmarshell(new ByteArrayInputStream(vdb.getBytes("UTF-8"))));

        TeiidVdb teiidVdb = metadataInstance.getVdb("myservice");

        assertFalse(teiidVdb.hasValidationError("views", "tbl", "table"));

        assertTrue(teiidVdb.hasValidationError("views", "tbl2", "table"));
    }

}
