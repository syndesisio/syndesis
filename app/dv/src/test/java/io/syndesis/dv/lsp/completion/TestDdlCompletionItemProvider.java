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

import io.syndesis.dv.lsp.completion.providers.DdlCompletionProvider;

import java.util.List;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.Position;
import org.junit.Test;
import org.teiid.query.parser.SQLParserConstants;

@SuppressWarnings("nls")
public class TestDdlCompletionItemProvider {

    private final DdlCompletionProvider itemProvider = new DdlCompletionProvider();
    private final static int MAX_ITEMS = 615;

    /**
     * The tokenImage[...] call is returning strings wrapped in double-quotes
     *
     * Need to return a simple string
     * 
     * @return string without double quotes
     */
    public static String getLabel(int keywordId) {
        String tokenImageStr = SQLParserConstants.tokenImage[keywordId];
        return tokenImageStr.substring(1, tokenImageStr.length() - 1);
    }

    @Test
    public void testEmptyViewCompletions() {
        // 01234567890123456789
        String stmt = "";

        // CREATE 0, 0, expect 1 items (i.e. CREATE )
        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 0));
        assertEquals(4, items.size());
    }

    @Test
    public void testViewNameCompletions() {
        // 01234567890123456789
        String stmt = "";
        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 0));
        assertEquals(4, items.size());

        // 01234567890123456789
        stmt = "CREATE "; // VIEW ";
        items = itemProvider.getCompletionItems(stmt, new Position(0, 7));
        assertEquals(3, items.size());

        // 01234567890123456789
        stmt = "CREATE VIEW ";
        items = itemProvider.getCompletionItems(stmt, new Position(0, 12));
        assertEquals(0, items.size());

             // 01234567890123456789
        stmt = "CREATE VIEW foobar ";
        items = itemProvider.getCompletionItems(stmt, new Position(0, 19));
        assertEquals(1, items.size());

        // looking for left parenthesis (
             // 012345678901234567890
        stmt = "CREATE VIEW foobar ()";
        items = itemProvider.getCompletionItems(stmt, new Position(0, 20));
        assertEquals(5, items.size());
    }

    @Test
    public void testCreateViewCompletions() {
        // 01234567890123456789
        String stmt = "CREATE VIEW winelist ( \n" + "   wine string(255), price decimal(2, 15), vendor string(255) \n"
        + ") AS SELECT * FROM PostgresDB.winelist";

        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 0));
        assertEquals(4, items.size());

        items = itemProvider.getCompletionItems(stmt, new Position(0, 5));
        assertEquals(4, items.size());

        items = itemProvider.getCompletionItems(stmt, new Position(0, 6));
        assertEquals(4, items.size());

        items = itemProvider.getCompletionItems(stmt, new Position(0, 7));
        assertEquals(3, items.size());

        items = itemProvider.getCompletionItems(stmt, new Position(0, 10));
        assertEquals(3, items.size());

        items = itemProvider.getCompletionItems(stmt, new Position(0, 11));
        assertEquals(3, items.size());

        items = itemProvider.getCompletionItems(stmt, new Position(1, 8));
        assertEquals(46, items.size());
    }

    @Test
    public void testTableBody() {
        String stmt =
//               01234567890123456789012345678901234567890123456789
                "CREATE VIEW winelist ( wine string(255), id integer )"
//               0123456789 123456789 123456789 123456789 123456789 123456789 123456789
              + "AS SELECT * FROM winelist";

        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 22));
        assertEquals(5, items.size());


        // after comma in table element
        items = itemProvider.getCompletionItems(stmt, new Position(0, 40));
        assertEquals(5, items.size());
    }

    @Test
    public void testNoTableBody() {
        String stmt =
//               0123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW winelist AS SELECT * FROM winelist ";

        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 22));
        assertEquals(1, items.size());

        items = itemProvider.getCompletionItems(stmt, new Position(0, 21));
        assertEquals(1, items.size());

        items = itemProvider.getCompletionItems(stmt, new Position(0, 47));
        assertEquals(366, items.size());
    }

    @Test
    public void testFromClause_1() {
        // SchemaDB_1
        // table_1, table_2.... table_5
        // column_1_1, column_1_2....column_7_5

        String stmt =
//               0123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW newViewName AS SELECT column_1_1 FROM SchemaDB_1.table_2 AS t1, ";

        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 85));
        assertEquals(0, items.size());

    }

    @Test
    public void testSelectClause_1() {
        // SchemaDB_1
        // table_1, table_2.... table_5
        // column_1_1, column_1_2....column_7_5

        String stmt =
//              10        20        30        40        50        60        70        80        90        100       110       120       130
//      0123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW newViewName AS SELECT concat(column_1_1, concat(', ', column1_2)) FROM SchemaDB_1.table_2 AS t1, ";

        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 52));
        assertEquals(366, items.size());

    }

    @Test
    public void testSelectFunction() {
        // SchemaDB_1
        // table_1, table_2.... table_5
        // column_1_1, column_1_2....column_7_5

        String stmt =
//                 10        20        30        40        50        60        70        80        90        100       110       120       130
//       0123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW aaa (full_name) AS SELECT concat(t2.last_name, concat(', ', t2.first_name) ) as full_name FROM PostgresDB.contact as t2";

        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 59));
        assertEquals(MAX_ITEMS, items.size());

    }

    @Test
    public void testCompletionsForTableAliasDot_2() {
        String stmt =
        // CHARACTER--------->
//                         10        20        30        40        50        60        70        80        90        100       110       120       130
              // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
              // 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW winelist (priceInCents, id, productcode) AS\n" + "  SELECT\n"
              + "    t1.price as priceInCents, id, productcode, t2. \n" + "  FROM\n"
              + "    PostgresDB.winelist AS t1, PostgresDB.contact as t2 WHERE id > 70 ORDER BY t1.id";

        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(2, 49));
        assertEquals(MAX_ITEMS, items.size());
    }

    @Test
    public void testCompletionsForWhereAliasDot() {
        String stmt =
        // CHARACTER--------->
//                 10        20        30        40        50        60        70        80        90        100       110       120       130
                // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
                // 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW winelist (priceInCents, id, productcode) AS\n"
              + "  SELECT\n"
              + "    t1.price as priceInCents, id, productcode, t2.first_name\n"
              + "  FROM\n"
              + "    PostgresDB.winelist AS t1, PostgresDB.contact as t2 WHERE id > 70 ORDER BY t1.";

        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(4, 82));
        assertEquals(0, items.size());
    }

    @Test
    public void testFromClause_3() {
        String stmt =
//               0123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW xyz AS SELECT c1 FROM s1.table1 AS t1 O";

        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 61));
        assertEquals(366, items.size());

    }

    @Test
    public void testCharAfterTableName() {
        String stmt =
            "CREATE VIEW contact AS SELECT company FROM PostgresDB.contact AS t1, PostgresDB.winelist a b";
        //   0123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 12345
    
        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 89));
        assertEquals(1, items.size());
        items = itemProvider.getCompletionItems(stmt, new Position(0, 90));
        assertEquals(1, items.size());
        items = itemProvider.getCompletionItems(stmt, new Position(0, 91));
        assertEquals(1, items.size());
        items = itemProvider.getCompletionItems(stmt, new Position(0, 92));
        assertEquals(366, items.size());
    }
    
    @Test
    public void testMetadataItemsAfterTableAlias() {
        String stmt =
            "CREATE VIEW contact AS SELECT t1. FROM contact AS t1";
        //   0123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 33));
        assertEquals(0, items.size());
    }

    @Test
    public void testForForegnKeyItem() {
        String stmt =
            "CREATE VIEW contact (c1, ) AS SELECT * FROM contact";
        //   0123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 25));
        assertEquals(5, items.size());
    }
    
    @Test
    public void testWithClause_1() {
        String stmt =
            "CREATE VIEW contact (c1) AS WITH a(a1) AS (SELECT a from tbl1) SELECT * FROM contact";
        //   0123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789 123456789
        List<CompletionItem> items = itemProvider.getCompletionItems(stmt, new Position(0, 43));
        assertEquals(366, items.size());
    }
}
