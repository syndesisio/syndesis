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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class KuduCreateTableCustomizerTest extends AbstractKuduCustomizerTestSupport {
    private KuduCreateTableCustomizer customizer;

    @BeforeEach
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

        Assertions.assertNotNull(schema);
        Assertions.assertNotNull(builder);

        Assertions.assertEquals(4, schema.getColumnCount(), "Table schema has all elements");
        Assertions.assertEquals("id", schema.getColumn("id").getName(), "Name of the first column matches");
        Assertions.assertEquals("int32", schema.getColumn("id").getType().getName(), "Type of the first column matches");

        Assertions.assertEquals("name", schema.getColumn("name").getName(), "Name of the first column matches");
        Assertions.assertEquals("string", schema.getColumn("name").getType().getName(), "Type of the first column matches");
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

        Assertions.assertNotNull(schema);
        Assertions.assertNotNull(builder);

        Assertions.assertEquals(4, schema.getColumnCount(), "Table schema has all elements");
        Assertions.assertEquals("id", schema.getColumn("id").getName(), "Name of the first column matches");
        Assertions.assertEquals("int32", schema.getColumn("id").getType().getName(), "Type of the first column matches");

        Assertions.assertEquals("name", schema.getColumn("name").getName(), "Name of the first column matches");
        Assertions.assertEquals("string", schema.getColumn("name").getType().getName(), "Type of the first column matches");
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

        Assertions.assertNotNull(table);
        Assertions.assertEquals(table.getName(), "KuduTestTable", "Table name matches");
        Assertions.assertEquals(4, table.getSchema().getColumns().length, "Right ammount of columns");
    }
}