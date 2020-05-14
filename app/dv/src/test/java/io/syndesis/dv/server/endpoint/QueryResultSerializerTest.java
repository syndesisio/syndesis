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

import io.syndesis.dv.metadata.query.QSColumn;
import io.syndesis.dv.metadata.query.QSResult;
import io.syndesis.dv.metadata.query.QSRow;
import io.syndesis.dv.rest.JsonMarshaller;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("nls")
public class QueryResultSerializerTest {

    private static final String[][] COLUMNS_DATA = {
        { "Id", "ID", "long" },
        { "Name", "Name", "varchar" },
        { "Code", "Code", "varchar" }
    };

    private static final Object[][] ROWS_DATA = {
        { 1, "Florida", "FL" },
        { 2, "Washington", "WA" },
        { 3, "Missouri", "MI" },
        { 4, "District of Columbia", "DC" },
        { 5, "Montana", "MO" }
    };

    private static final int COLUMN_NAME = 0;

    private static final int COLUMN_LABEL = 1;

    private static final int COLUMN_TYPE = 2;

    private QSResult queryResult;

    private String expected;

    @Before
    public void init() {
        queryResult = new QSResult();

        for (int i = 0; i < COLUMNS_DATA.length; ++i) {
            String[] columnData = COLUMNS_DATA[i];

            QSColumn column = new QSColumn(columnData[COLUMN_TYPE], columnData[COLUMN_NAME], columnData[COLUMN_LABEL]);

            queryResult.addColumn(column);
        }

        for (int i = 0; i < ROWS_DATA.length; ++i) {
            Object[] rowData = ROWS_DATA[i];

            QSRow row = new QSRow();
            for (Object o : rowData) {
                row.add(o);
            }

            queryResult.addRow(row);
        }

        expected = "{\n" +
                "  \"columns\" : [ {\n" +
                "    \"type\" : \"long\",\n" +
                "    \"name\" : \"Id\",\n" +
                "    \"label\" : \"ID\"\n" +
                "  }, {\n" +
                "    \"type\" : \"varchar\",\n" +
                "    \"name\" : \"Name\",\n" +
                "    \"label\" : \"Name\"\n" +
                "  }, {\n" +
                "    \"type\" : \"varchar\",\n" +
                "    \"name\" : \"Code\",\n" +
                "    \"label\" : \"Code\"\n" +
                "  } ],\n" +
                "  \"rows\" : [ {\n" +
                "    \"row\" : [ 1, \"Florida\", \"FL\" ]\n" +
                "  }, {\n" +
                "    \"row\" : [ 2, \"Washington\", \"WA\" ]\n" +
                "  }, {\n" +
                "    \"row\" : [ 3, \"Missouri\", \"MI\" ]\n" +
                "  }, {\n" +
                "    \"row\" : [ 4, \"District of Columbia\", \"DC\" ]\n" +
                "  }, {\n" +
                "    \"row\" : [ 5, \"Montana\", \"MO\" ]\n" +
                "  } ]\n" +
                "}";
    }

    @Test
    public void shouldExportResult() {
        String json = JsonMarshaller.marshall( this.queryResult );
        assertEquals(expected, json);
    }

}
