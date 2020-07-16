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

import java.util.List;

import org.junit.Test;

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;

public class TestSelectClause extends TestStatementUtils {

    @Test
    public void testSelectStar() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT * FROM fooBar";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 11);
        printBeforeAndAfter(cvs, "testSelectStar");
        assertEquals(true, cvs.getQueryExpression().getSelectClause().isStar());
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectWithAll() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT ALL * FROM fooBar";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 12);
        printBeforeAndAfter(cvs, "testSelectWithAll");
        assertEquals(true, cvs.getQueryExpression().getSelectClause().isAll());
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectWithDistinct() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT DISTINCT * FROM fooBar";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 12);
        printBeforeAndAfter(cvs, "testSelectWithDistinct");
        assertEquals(true, cvs.getQueryExpression().getSelectClause().isDistinct());
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectIntegerConstant() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT 1 FROM fooBar";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 11);
        printBeforeAndAfter(cvs, "testSelectIntegerConstant");
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("1",
                cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getLiteralValueToken().image);
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectStringConstant() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT 'xxx' FROM fooBar";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 11);
        printBeforeAndAfter(cvs, "testSelectStringConstant");
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("'xxx'",
                cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getLiteralValueToken().image);
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectConstantsAndColumn() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT 'xxx', 1, foo FROM fooBar";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 15);
        printBeforeAndAfter(cvs, "testSelectConstantsAndColumn");
        assertEquals(3, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("'xxx'",
                cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getLiteralValueToken().image);
        assertEquals("1",
                cvs.getQueryExpression().getSelectClause().getSelectColumns()[1].getLiteralValueToken().image);
        assertEquals("foo", cvs.getQueryExpression().getSelectClause().getSelectColumns()[2].getNameToken().image);
        assertEquals(1, cvs.getExceptions().size()); // # View and SELECT columns do not match
    }

    @Test
    public void testSelectColumn() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT bar FROM foo";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 11);
        printBeforeAndAfter(cvs, "testSelectColumn");
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("bar", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getNameToken().image);
        assertEquals("bar", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getColumnName());
        assertEquals(null, cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getTableName());
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectTableAndColumn() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
            //   0      1    2    3 4  5 6  7      8       9    10
                "CREATE VIEW abcd ( id ) AS SELECT foo.bar FROM foo";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 11);
        printBeforeAndAfter(cvs, "testSelectTableAndColumn");
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("foo.bar", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getNameToken().image);
        assertEquals("bar", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getColumnName());
        assertEquals("foo", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getTableName());
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectAliasedColumn() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT foo as c1 FROM bar";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 13);
        printBeforeAndAfter(cvs, "testSelectAliasedColumn");
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("foo", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getNameToken().image);
        assertEquals("c1", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getAliasNameToken().image);
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectTwoAliasedColumn() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT columnA as c1, columnB as c2 FROM bar";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 17);
        printBeforeAndAfter(cvs, "testSelectTwoAliasedColumn");
        assertEquals(2, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("columnA", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getNameToken().image);
        assertEquals("c1", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getAliasNameToken().image);
        assertEquals("columnB", cvs.getQueryExpression().getSelectClause().getSelectColumns()[1].getNameToken().image);
        assertEquals("c2", cvs.getQueryExpression().getSelectClause().getSelectColumns()[1].getAliasNameToken().image);
        assertEquals(1, cvs.getExceptions().size()); // # View and SELECT columns do not match
    }
    
    @Test
    public void testReservedViewName() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW view ( id ) AS SELECT id FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 12);
        printBeforeAndAfter(cvs, "testReservedViewName");
        assertEquals(1, cvs.getExceptions().size());
    }
    
    @Test
    public void testReservedViewNameQuoted() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW \"view\" ( id ) AS SELECT id FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 12);
        printBeforeAndAfter(cvs, "testReservedViewNameQuoted");
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testValidNumProjectedSymbols() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW foo (c1, c2, c3, PRIMARY KEY (c1) ) AS SELECT c1, c2, c3 FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 26);
        printBeforeAndAfter(cvs, "testValidNumProjectedSymbols");
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testInvalidNumProjectedSymbols() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW foo (c1, c2) AS SELECT c1 FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 14);
        printBeforeAndAfter(cvs, "testInvalidNumProjectedSymbols");
        assertEquals(1, cvs.getExceptions().size());
    }
    
    @Test
    public void testExtraTableElementColumn() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW foo (c1, c2, ) AS SELECT c1, c2 FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 17);
        printBeforeAndAfter(cvs, "testExtraTableElementColumn");
        assertEquals(1, cvs.getExceptions().size());
    }

    @Test
    public void testPrimaryKeyTableElement() {
        String ddl =
              // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW foo (c1, c2, PRIMARY KEY (c1, c2)) AS SELECT c1, c2 FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 24);
        printBeforeAndAfter(cvs, "testPrimaryKeyTableElement");
        assertEquals(3, cvs.getTableBody().getTableElements().length);
        assertEquals(8, cvs.getTableBody().getTableElements()[2].getFirstTknIndex());
        assertEquals(14, cvs.getTableBody().getTableElements()[2].getLastTknIndex());
    }
    
    @Test
    public void testForeignKeyTableElement() {
        String ddl =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW foo (c1, c2, FOREIGN KEY (c1, c2) REFERENCES otherTable(a1, a2)) AS SELECT c1, c2 FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 31);
        printBeforeAndAfter(cvs, "testForeignKeyTableElement");
        assertEquals(8, cvs.getTableBody().getTableElements()[2].getFirstTknIndex());
        assertEquals(21, cvs.getTableBody().getTableElements()[2].getLastTknIndex());
    }
    
    @Test
    public void testPrimaryKeyForeignKeyTableElement() {
        String ddl =
             // 0       1    2   3 4 5 6 7 8       9   10 11 12 13 14 15         16         17 18 19 20 21 22 23      24  25 26 27 28 29 30 31 32     33 34 35 36   37 38
                "CREATE VIEW foo ( c1, c2, FOREIGN KEY (  c1 ,  c2 )  REFERENCES otherTable (  a1 ,  a2 )  ,  PRIMARY KEY (  c1 ,  c2 )  )  AS SELECT c1 ,  c2 FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 39);
        printBeforeAndAfter(cvs, "testPrimaryKeyForeignKeyTableElement");
        assertEquals(8, cvs.getTableBody().getTableElements()[2].getFirstTknIndex());
        assertEquals(22, cvs.getTableBody().getTableElements()[2].getLastTknIndex());
    }
    
    @Test
    public void testForeignKeyTableElementPartial() {
        String ddl =
              // 0123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW foo (c1, c2, FOREIGN KEY (c1, c2) ) AS SELECT c1, c2 FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 24);
        printBeforeAndAfter(cvs, "testForeignKeyTableElementPartial");
        assertEquals(8, cvs.getTableBody().getTableElements()[2].getFirstTknIndex());
        assertEquals(14, cvs.getTableBody().getTableElements()[2].getLastTknIndex());
        assertEquals(1, cvs.getAnalyzer().getReport().getExceptions().size());
        assertEquals("Expected error not found: " + Messages.Error.INCOMPLETE_FOREIGN_KEY,
                true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.INCOMPLETE_FOREIGN_KEY.getErrorCode()));
    }
    
    @Test
    public void testInvalidSelect_1() {
        String ddl =
              // 0123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW foo (c1, c2) AS SELECT a1 c1, c2 FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 17);
        printBeforeAndAfter(cvs, "testInvalidSelect_1");
        List<DdlAnalyzerException> exceptions = cvs.getAnalyzer().getReport().getExceptions();
        assertEquals(2, exceptions.size());
    }
    
    @Test
    public void testInvalidSelect_2() {
        String ddl =
              // 0123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW foo (c1, c2) AS SELECT a1. c1, c2 FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 18);
        printBeforeAndAfter(cvs, "testInvalidSelect_2");
        List<DdlAnalyzerException> exceptions = cvs.getAnalyzer().getReport().getExceptions();
        assertEquals(3, exceptions.size());
        assertEquals("Expected error not found: " + Messages.Error.INCOMPLETE_SCHEMA_REF,
                true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.INCOMPLETE_SCHEMA_REF.getErrorCode()));
        assertEquals( "Expected error not found: " + Messages.Error.PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH,
                true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH.getErrorCode()));
    }
    
    @Test
    public void testInvalidSelect_3() {
        String ddl =
              // 0123456789 123456789 123456789 123456789 123456789
                "CREATE VIEW foo (c1) AS SELECT c1, FROM bar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 13);
        printBeforeAndAfter(cvs, "testInvalidSelect_3");
        List<DdlAnalyzerException> exceptions = cvs.getAnalyzer().getReport().getExceptions();
        assertEquals(1, exceptions.size());
        assertEquals("Expected error not found: " + Messages.Error.UNEXPECTED_COMMA,
                true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.UNEXPECTED_COMMA.getErrorCode()));
    }
    
    @Test
    public void testInvalidSelect_4() {
        String ddl =
              // 0      1    2   3 4  5 6  7 8  9      10 11 12 13 14 15   16  17
                "CREATE VIEW foo ( c1 , c2 ) AS SELECT a1 #  c1 ,  c2 FROM bar ;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 18);
        printBeforeAndAfter(cvs, "testInvalidSelect_4");
        List<DdlAnalyzerException> exceptions = cvs.getAnalyzer().getReport().getExceptions();
        assertEquals(3, exceptions.size());
        assertEquals("Expected error not found: " + Messages.Error.PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH,
                true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH.getErrorCode()));
        assertEquals("Expected error not found: " + Messages.Error.INVALID_COLUMN_MISSING_COMMA,
                true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.INVALID_COLUMN_MISSING_COMMA.getErrorCode()));
    }
    
    @Test
    public void testInvalidSelect_5() {
        String ddl =
              // 0      1    2   3 4  5 6  7 8  9      10 11 12 13 14 15 16   17  18
                "CREATE VIEW foo ( c1 , c2 ) AS SELECT c1 ,  c2 ,  a1 #  FROM bar ;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 19);
        printBeforeAndAfter(cvs, "testInvalidSelect_5");
        List<DdlAnalyzerException> exceptions = cvs.getAnalyzer().getReport().getExceptions();
        assertEquals(2, exceptions.size());
        assertEquals("Expected error not found: " + Messages.Error.PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH,
                true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH.getErrorCode()));
        assertEquals("Expected error not found: " + Messages.Error.INVALID_COLUMN_MISSING_COMMA,
                true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.INVALID_COLUMN_MISSING_COMMA.getErrorCode()));
    }
    

    @Test
    public void testInvalidSelect_6() {
        String ddl =
            // 0       1    2    3 4  5 6  7      8
               "CREATE VIEW abcd ( id ) AS SELECT #";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 9);
        printBeforeAndAfter(cvs, "testInvalidSelect_6");
        assertEquals(1, cvs.getExceptions().size());
        assertEquals("Expected error not found: " + Messages.Error.MISSING_FROM_KEYWORD,
        		true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.MISSING_FROM_KEYWORD.getErrorCode()));
    }
    
    @Test
    public void testInvalidSelect_7() {
        String ddl =
            // 0       1    2    3 4  5 6  7      8
               "CREATE VIEW abcd ( id ) AS SELECT 1";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 9);
        printBeforeAndAfter(cvs, "testInvalidSelect_7");
        assertEquals(false, cvs.getQueryExpression().getSelectClause().isStar());
        assertEquals(1, cvs.getExceptions().size());
        assertEquals("Expected error not found: " + Messages.Error.MISSING_FROM_KEYWORD,
        		true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.MISSING_FROM_KEYWORD.getErrorCode()));
    }
    
    @Test
    public void testInvalidSelect_8() {
        String ddl =
            // 0       1    2    3 4  5 6  7      8
               "CREATE VIEW abcd ( id ) AS SELECT FROM";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 9);
        printBeforeAndAfter(cvs, "testInvalidSelect_8");
        assertEquals(false, cvs.getQueryExpression().getSelectClause().isStar());
        assertEquals(1, cvs.getExceptions().size());
        assertEquals("Expected error not found: " + Messages.Error.PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH,
        		true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH.getErrorCode()));
    }
    
}
