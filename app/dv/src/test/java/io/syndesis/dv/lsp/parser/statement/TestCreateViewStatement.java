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

import io.syndesis.dv.lsp.Messages;
//import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.StatementType;
//import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
//import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

import org.junit.Test;
//import org.teiid.query.parser.Token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("nls")
public class TestCreateViewStatement extends TestStatementUtils {

    @Test
    public void testSimpleStatement() {
                 //   0      1    2   3 4 5 6  7      8  9    10      11
        String ddl = "CREATE VIEW xyz ( a ) AS SELECT c1 FROM winelist;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 12);
        printBeforeAndAfter(cvs, "testFromAndWhereClauses");
        assertEquals(0, cvs.getExceptions().size());
    }
    
    @Test
    public void testCreateViewStatement() {

        String ddl =
                // 01234567890123456789012345678901234567890123456789
               //0      1    2        3
                "CREATE VIEW wineList (\n" +
                // 01234567890123456789012345678901234567890123456789
                      // 4  5       6       7   8       9 10          11    12 13  14   15
                        "e1 integer primary key OPTIONS ( UPDATEABLE 'false',  FOO 'BAR')  ,\n" +
                        // 01234567890123456789012345678901234567890123456789
                      // 17 18      19    20       21    22
                        "e6 varchar index default 'hello',\n" +
                        // 01234567890123456789012345678901234567890123456789
                      // 23 24     25 26 27 28 29 30  31   32    33 
                        "e7 decimal(  10 ,  2  )  NOT NULL unique)\n" +
                        // 01234567890123456789012345678901234567890123456789
                      // 34      35 36          37 38 39        40   41  42  43   44 45          46         47
                        "OPTIONS (  CARDINALITY 12 ,  UPDATABLE 'true',  FOO 'BAR',  ANNOTATION 'Test Table')\n" +
                        // 01234567890123456789012345678901234567890123456789
                      // 48 49     50 51   52       53    54 55 56 57
                        "AS SELECT *  FROM winelist WHERE e1 > '10';";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 58);
        printBeforeAndAfter(cvs, "testCreateViewStatement");
        assertEquals("wineList", cvs.getViewName());
        assertTrue(cvs.getExceptions().isEmpty());
        TableBody tb = cvs.getTableBody();
        assertEquals(3, tb.getTableElements().length);
        assertEquals(0, cvs.getExceptions().size()); 

    }

    @Test
    public void testMissingViewName() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW (\n" +
                // 01234567890123456789012345678901234567890123456789
                        "e1 integer primary key,\n" +
                        // 01234567890123456789012345678901234567890123456789
                        "e6 varchar index default 'hello')\n" +
                        // 01234567890123456789012345678901234567890123456789
                        "AS SELECT * FROM winelist WHERE e1 > '10'";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 23);
        printBeforeAndAfter(cvs, "testMissingViewName");
        assertEquals(2, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.MISSING_VIEW_NAME));
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_CREATE_VIEW_STATEMENT));
    }
    
    @Test
    public void testFromAndWhereClauses() {
                 //   0      1    2   3 4 5 6  7      8  9    10      11 12     13    14 15 16 17
        String ddl = "CREATE VIEW xyz ( a ) AS SELECT c1 FROM winelist,  stores WHERE e1 >  10 ;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 18);
        printBeforeAndAfter(cvs, "testFromAndWhereClauses");
        assertEquals(0, cvs.getExceptions().size());
    }
    
    @Test
    public void testNoTableBody() {
                 //   0      1    2    3  4      5       6 7          8 9           10 11 12 13   14   15    16
        String ddl = "CREATE VIEW test AS SELECT orderkey, orderstatus, totlalprice /  2  AS half from orders;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 17);
        printBeforeAndAfter(cvs, "testNoTableBody");
        assertEquals(0, cvs.getExceptions().size());
    }
}