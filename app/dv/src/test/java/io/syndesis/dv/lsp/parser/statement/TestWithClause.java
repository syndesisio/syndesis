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

import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;

public class TestWithClause extends TestStatementUtils {

    @Test
    public void testNoWithClause() {
        String ddl =
              // 0      1    2    3 4  5 6  7      8  9    10     11 12 13 14     15 16 17
                "CREATE VIEW abcd ( id ) AS SELECT id FROM fooBar as t1 ,  barFoo as t2 ;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 18);
        printBeforeAndAfter(cvs, "testNoWithClause");
        assertEquals(0, cvs.getExceptions().size());
    }
    
    @Test
    public void testWithClause() {
        String ddl =
              // 0      1    2    3 4  5 6  7    8 9 10 11 12 13 14     15 16   17 18 19     20 21   22    23
                "CREATE VIEW abcd ( id ) AS with a ( x  )  AS (  SELECT e1 from t1 )  SELECT *  FROM fooBar;";
              //           0         0         0         0         0         0         0         0         0         0
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 24);
        printBeforeAndAfter(cvs, "testWithClause");
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testWithClause_2_Elements() {
        String ddl =
              // 0      1    2    3 4  5 6  7    8 9 10 11 12 13 14     15 16   17     18 19 20 21 22 23 24 25 26     27 28   29     30 31     32 33   34     35 36 37 38     39 40 41
                "CREATE VIEW abcd ( id ) AS with a ( x  )  AS (  SELECT e1 from pm1.g1 )  ,  b  (  x  )  AS (  SELECT e2 from pm1.g2 )  SELECT id FROM fooBar as t1 ,  barFoo as t2 ;";
              //           0         0         0         0         0         0         0         0         0         0
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 42);
        printBeforeAndAfter(cvs, "testWithClause_2_Elements");
        assertEquals(0, cvs.getExceptions().size());
    }
    
    @Test
    public void testWithClause_3_Elements() {
        String ddl =
              // 0      1    2    3 4  5 6  7     
                "CREATE VIEW abcd ( id ) AS with " + 
                    // 8 9 10 11 12 13 14     15 16   17     18 19 
                      "a ( x  )  AS (  SELECT e1 from pm1.g1 )  ," + 
                    // 20 21 22 23 24 25 26     27 28   29     30 31
                      "b  (  y  )  AS (  SELECT e2 from pm2.g2 )  , " +
                    // 32 33 34 35 36 37 38     39 40   41     42 43     44 45   46     47 48 49 50     51 52 53
                      "c  (  z  )  AS (  SELECT e3 from pm3.g3 )  SELECT id FROM fooBar as t1 ,  barFoo as t2 ;";
              //           0         0         0         0         0         0         0         0         0         0
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 54);
        printBeforeAndAfter(cvs, "testWithClause_3_Elements");
        assertEquals(0, cvs.getExceptions().size());
        
        CreateViewStatement cvs2 = parseAndBasicAssertStatement(cvs.toString(), 54);
        assertEquals(cvs.toString(), cvs2.toString());
    }
    
    @Test
    public void testWithClauseWithWhereAndSymbolMismatch() {
        String ddl =
              // 0      1    2    3 4  5 6  7    8 9 10 11 12 13 14     15 16   17 18    19 20 21 22 23     24 25 26 27 28  29   30    31
                "CREATE VIEW abcd ( c1 ) AS with a ( x  )  AS (  SELECT e1 from t1 WHERE e1 >  12 )  SELECT c1 ,  c2 ,  c3  FROM fooBar;";
              //           0         0         0         0         0         0         0         0         0         0
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 32);
        printBeforeAndAfter(cvs, "testWithClause");
        List<DdlAnalyzerException> exceptions = cvs.getAnalyzer().getReport().getExceptions();
        assertEquals(1, exceptions.size());
        assertEquals(QuickFixFactory.DiagnosticErrorId.PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH.getErrorCode(),
                exceptions.get(0).getErrorCode());
    }
    
    // WITH CLAUSE SCENARIOS
    // CREATE VIEW abcd ( c1 ) AS with 
    // CREATE VIEW abcd ( c1 ) AS with a 
    // CREATE VIEW abcd ( c1 ) AS with a (
    // CREATE VIEW abcd ( c1 ) AS with a ( x 
    // CREATE VIEW abcd ( c1 ) AS with a ( x )
    // CREATE VIEW abcd ( c1 ) AS with a ( x )  AS
    // CREATE VIEW abcd ( c1 ) AS with a ( x )  AS (
    // CREATE VIEW abcd ( c1 ) AS with a ( x )  AS (  SELECT
    // CREATE VIEW abcd ( c1 ) AS with a ( x )  AS (  SELECT e1 
    // CREATE VIEW abcd ( c1 ) AS with a ( x )  AS (  SELECT e1 from 
    // CREATE VIEW abcd ( c1 ) AS with a ( x )  AS (  SELECT e1 from t1
    // CREATE VIEW abcd ( c1 ) AS with a ( x )  AS (  SELECT e1 from t1 )  SELECT 
    // CREATE VIEW abcd ( c1 ) AS with a ( x )  AS (  SELECT e1 from t1 )  SELECT c1
    // CREATE VIEW abcd ( c1 ) AS with a ( x )  AS (  SELECT e1 from t1 )  SELECT c1 FROM
    // CREATE VIEW abcd ( c1 ) AS with a ( x )  AS (  SELECT e1 from t1 )  SELECT c1 FROM fooBar;
    // CREATE VIEW abcd ( c1 ) AS with a ( x )  AS (  SELECT e1 from t1 )  SELECT c1 FROM fooBar;
    @Test
    public void testWithClauseIncompleteWithListElement() {
        String ddl =
              // 0      1    2    3 4  5 6  7    8 9 10 11 12 13
                "CREATE VIEW abcd ( c1 ) AS with a ( x  )  AS SELECT";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 14);
        printBeforeAndAfter(cvs, "testWithClauseIncompleteWithListElement");
        assertEquals(3, cvs.getExceptions().size());
    }
    
    @Test
    public void testWithClauseMissingRPAREN() {
        String ddl =
              // 0      1    2   3 4 5 6  7    8 9 10 11 12 13 14 15 16     17 18 19 20   21      22     23 24   25    26
                "CREATE VIEW aaa ( a2) AS WITH a ( a1 ,  a2 )  AS (  SELECT b1 ,  b2 from pg1.t1  SELECT a2 FROM table1;";
              //           0         0         0         0         0         0         0         0         0         0
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 27);
        printBeforeAndAfter(cvs, "testWithClauseMissingRPAREN");
        assertEquals(3, cvs.getExceptions().size());
    }
    
    @Test
    public void testWithListElementMissingFrom() {
        String ddl =
              // 0      1    2   3 4 5 6  7    8 9 10 11  12 13 14     15 16 17 18     19 20   21    22
                "CREATE VIEW abcd ( c1 ) AS with a ( x )  AS (  SELECT e1 )  SELECT c1 FROM fooBar;";
              //           0         0         0         0         0         0         0         0         0         0
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 22);
        printBeforeAndAfter(cvs, "testWithListElementMissingFrom");
        assertEquals(1, cvs.getExceptions().size());
    }
    
    // CREATE VIEW aaa (a2) AS WITH a AS (SELECT * from tbl1) SELECT * FROM aaa
    // SHOULD BE VALID
    @Test
    public void testWithListELementNoTableBody() {
        String ddl =
              // 0      1    2   3 4 5 6  7    8 9 10 11     12 13   14  15 16     17 18   19
                "CREATE VIEW aaa ( a2) AS WITH a AS ( SELECT *  from tbl1)  SELECT *  FROM aaa";
              //           0         0         0         0         0         0         0         0         0         0
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 20);
        printBeforeAndAfter(cvs, "testWithListELementNoTableBody");
        assertEquals(0, cvs.getExceptions().size());
    }
}
