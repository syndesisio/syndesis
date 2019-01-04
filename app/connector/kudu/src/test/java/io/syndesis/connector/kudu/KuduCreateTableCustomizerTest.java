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

package io.syndesis.connector.kudu;

import io.syndesis.connector.kudu.model.KuduTable;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.apache.kudu.Schema;
import org.apache.kudu.client.CreateTableOptions;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class KuduCreateTableCustomizerTest extends AbstractKuduCustomizerTestSupport {
    private KuduCreateTableCustomizer customizer;

    @Before
    public void setupCustomizer() {
        customizer = new KuduCreateTableCustomizer();
    }

    @Test
    public void testBeforeProducerFromOptions() throws Exception {
        Map<String, Object> options = new HashMap<>();
        options.put("columns", "Integer,id;String,title;String,name;String,lastname");

        customizer.customize(getComponent(), options);

        Exchange inbound = new DefaultExchange(createCamelContext());
        getComponent().getBeforeProducer().process(inbound);

        Schema schema = (Schema) inbound.getIn().getHeader("Schema");
        CreateTableOptions builder = (CreateTableOptions) inbound.getIn().getHeader("TableOptions");

        Assert.assertNotNull(schema);
        Assert.assertNotNull(builder);

        Assert.assertEquals("Table schema has all elements", 4, schema.getColumnCount());
        Assert.assertEquals("Name of the first column matches", "id", schema.getColumn("id").getName());
        Assert.assertEquals("Type of the first column matches", "int32", schema.getColumn("id").getType().getName());

        Assert.assertEquals("Name of the first column matches", "name", schema.getColumn("name").getName());
        Assert.assertEquals("Type of the first column matches", "string", schema.getColumn("name").getType().getName());
    }

    @Test
    public void testBeforeProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        KuduTable model = new KuduTable();
        model.setName("KuduTestTable");

        KuduTable.Schema modelSchema = new KuduTable.Schema();
        KuduTable.ColumnSchema[] modelSchemaColumns = {
                new KuduTable.ColumnSchema("id", "Integer", true),
                new KuduTable.ColumnSchema("title", "String", false),
                new KuduTable.ColumnSchema("name", "String", false),
                new KuduTable.ColumnSchema("lastname", "String", false)
        };

        modelSchema.setColumns(modelSchemaColumns, true);
        model.setSchema(modelSchema);

        Exchange inbound = new DefaultExchange(createCamelContext());
        inbound.getIn().setBody(model);
        getComponent().getBeforeProducer().process(inbound);

        Schema schema = (Schema) inbound.getIn().getHeader("Schema");
        CreateTableOptions builder = (CreateTableOptions) inbound.getIn().getHeader("TableOptions");

        Assert.assertNotNull(schema);
        Assert.assertNotNull(builder);

        Assert.assertEquals("Table schema has all elements", 4, schema.getColumnCount());
        Assert.assertEquals("Name of the first column matches", "id", schema.getColumn("id").getName());
        Assert.assertEquals("Type of the first column matches", "int32", schema.getColumn("id").getType().getName());

        Assert.assertEquals("Name of the first column matches", "name", schema.getColumn("name").getName());
        Assert.assertEquals("Type of the first column matches", "string", schema.getColumn("name").getType().getName());
    }

    @Test
    public void testAfterProducerFromModel() throws Exception {
        customizer.customize(getComponent(), new HashMap<>());

        KuduTable model = new KuduTable();
        model.setName("KuduTestTable");

        KuduTable.Schema modelSchema = new KuduTable.Schema();
        KuduTable.ColumnSchema[] modelSchemaColumns = {
                new KuduTable.ColumnSchema("id", "Integer", true),
                new KuduTable.ColumnSchema("title", "String", false),
                new KuduTable.ColumnSchema("name", "String", false),
                new KuduTable.ColumnSchema("lastname", "String", false)
        };

        modelSchema.setColumns(modelSchemaColumns, true);
        model.setSchema(modelSchema);

        Exchange inbound = new DefaultExchange(createCamelContext());
        inbound.getIn().setBody(model);
        getComponent().getAfterProducer().process(inbound);

        KuduTable table = (KuduTable) inbound.getIn().getBody();

        Assert.assertNotNull(table);
        Assert.assertEquals("Table name matches", "KuduTestTable", table.getName());
        Assert.assertEquals("Right ammount of columns", 4, table.getSchema().getColumns().length);
    }
}