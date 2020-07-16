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
package io.syndesis.dv.lsp.parser.statement;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestFromClause extends TestStatementUtils {

    @Test
    public void testFromTable() {
        String ddl =
             //  0      1    2    3 4 5 6  7 8  9      10   11 12     13   14     15 16 17
                "CREATE VIEW abcd ( aa, bb ) AS SELECT t1.aa,  t2.bb  FROM fooBar as t1 ;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 18);
        printBeforeAndAfter(cvs, "testFromTable");
        assertEquals(1, cvs.getQueryExpression().getFromClause().getTableSymbols().length);
        assertEquals(0, cvs.getExceptions().size());
    }
    
    @Test
    public void testFromTableIncompleteName() {
        String ddl =
             //  0      1    2    3 4  5 6  7      8     9    10     11 12 13 14
                "CREATE VIEW abcd ( aa ) AS SELECT t1.aa FROM schema1.  as t1 ;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 15);
        printBeforeAndAfter(cvs, "testFromTableIncompleteName");
        assertEquals(1, cvs.getQueryExpression().getFromClause().getTableSymbols().length);
        assertEquals(1, cvs.getExceptions().size());
    }
    
    @Test
    public void testFromTableMissingAliasName() {
        String ddl =
             //  0      1    2    3 4  5 6  7      8     9    10              11 12
                "CREATE VIEW abcd ( aa ) AS SELECT t1.aa FROM schema1.foobar  as ;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 13);
        printBeforeAndAfter(cvs, "testFromTableMissingAliasName");
        assertEquals(1, cvs.getQueryExpression().getFromClause().getTableSymbols().length);
        assertEquals(1, cvs.getExceptions().size());
    }
    
    @Test
    public void testTwoFromTables() {
        String ddl =
             //  0      1    2    3 4 5 6  7 8  9      10   11 12     13   14     15 16 17 18     19 20 21
                "CREATE VIEW abcd ( aa, bb ) AS SELECT t1.aa,  t2.bb  FROM fooBar as t1 ,  barFoo as t2 ;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 22);
        printBeforeAndAfter(cvs, "testTwoFromTables");
        assertEquals(2, cvs.getQueryExpression().getFromClause().getTableSymbols().length);
        assertEquals(0, cvs.getExceptions().size());
    }
}
