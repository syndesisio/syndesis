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
package io.syndesis.dv.lsp.completion;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.teiid.metadata.Column;
import org.teiid.metadata.Schema;
import org.teiid.metadata.Table;

import io.syndesis.dv.lsp.completion.providers.MetadataItemProvider;
import io.syndesis.dv.lsp.completion.providers.items.MetadataCompletionItem;
import io.syndesis.dv.server.endpoint.RestSourceSchema;
import io.syndesis.dv.server.endpoint.RestViewSourceInfo;

public class TestMetadataItemProvider {

    private final MetadataItemProvider itemProvider = new MetadataItemProvider(null, null, null);

    public static RestViewSourceInfo generateSampleMetadata() {
        Schema schema = new Schema();
        schema.setName("SchemaDB_1");

        for (int tCount = 1; tCount < 6; tCount++) {
            Table table = new Table();
            table.setName("table_" + tCount);

            for (int cCount = 1; cCount < 6; cCount++) {
                Column column = new Column();
                column.setName("column_" + cCount + "_" + tCount);
                table.addColumn(column);
            }
            schema.addTable(table);
        }
        List<RestSourceSchema> rssList = new ArrayList<RestSourceSchema>();
        rssList.add(new RestSourceSchema(schema));

        return new RestViewSourceInfo(rssList);
    }

    @Test
    public void testIsColumnMetadataForAliasedTable() {
        List<MetadataCompletionItem> mcItems = MetadataItemProvider.createItems(generateSampleMetadata(), null, null);

        // Gonna look for SchemaDB_1.table_1 columns
        // should be 5
        int numColumns = 0;
        for (MetadataCompletionItem nextMCI : mcItems) {

            if (this.itemProvider.isColumnMetadataForAliasedTable(nextMCI, "SchemaDB_1", "table_1")) {
                numColumns++;
            }
        }
        assertEquals(5, numColumns);

    }
}
