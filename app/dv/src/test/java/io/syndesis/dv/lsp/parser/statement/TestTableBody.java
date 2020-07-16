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

import io.syndesis.dv.lsp.Messages;

public class TestTableBody extends TestStatementUtils {

    @Test
    public void testTableBody() {
                 //   0      1    2   3 4 5 6 7 8  9      10 11   12      13
        String ddl = "CREATE VIEW xyz ( a , b ) AS SELECT *  FROM winelist;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 14);
        printBeforeAndAfter(cvs, "testTableBody");
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testEmptyTableBody() {
                 //   0      1    2   3 4 5
        String ddl = "CREATE VIEW xyz ( ) ;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 6);
        printBeforeAndAfter(cvs, "testEmptyTableBody");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.NO_TABLE_BODY_COLUMNS_DEFINED));
    }
    
    @Test
    public void testEmptyTableBodyWithMore() {
                 //   0      1    2   3 4 5  6      7 8    9       10
        String ddl = "CREATE VIEW xyz ( ) AS SELECT * FROM winelist;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 11);
        printBeforeAndAfter(cvs, "testEmptyTableBodyWithMore");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.NO_TABLE_BODY_COLUMNS_DEFINED));
    }
    
    @Test
    public void testNoTableBody() {
                 //   0      1    2   3  4      5 6    7       8
        String ddl = "CREATE VIEW xyz AS SELECT * FROM winelist;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 9);
        printBeforeAndAfter(cvs, "testNoTableBody");
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testIncompletePrimaryKey_1() {
        String ddl = "CREATE VIEW xyz (c1, c2, PRIMARY KEY) AS SELECT c1, c2 FROM table1;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 19);
        printBeforeAndAfter(cvs, "testIncompletePrimaryKey_1");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_PRIMARY_KEY));  
    }

    @Test
    public void testIncompletePrimaryKey_2() {
        String ddl = "CREATE VIEW xyz (c1, c2, PRIMARY KEY ()) AS SELECT c1, c2 FROM table1;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 21);
        printBeforeAndAfter(cvs, "testIncompletePrimaryKey_2");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_PRIMARY_KEY));  
    }

    @Test
    public void testInvalidPrimaryKeyReference() {
        String ddl = "CREATE VIEW xyz (c1, c2, PRIMARY KEY (1)) AS SELECT c1, c2 FROM table1;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 22);
        printBeforeAndAfter(cvs, "testInvalidPrimaryKeyReference");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_PRIMARY_KEY));  
    }

    @Test
    public void testMissingCommaInPrimaryKeyReference() {
        String ddl = "CREATE VIEW xyz (c1, c2, PRIMARY KEY (c1 c2)) AS SELECT c1, c2 FROM table1;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 23);
        printBeforeAndAfter(cvs, "testMissingCommaInPrimaryKeyReference");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.MISSING_COMMA_SEPARATOR));  
    }

    @Test
    public void testCompleteForeignKey() {
                  //  0      1    2   3 4 5 6 7 8       9   10 11 12 13         14   15 16  17 18 19 20     21 22 23 24   25    26
        String ddl = "CREATE VIEW xyz ( c1, c2, FOREIGN KEY (  c1 )  REFERENCES tbl1 (  a1  )  )  AS SELECT c1 ,  c2 FROM table1;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 27);
        printBeforeAndAfter(cvs, "testCompleteForeignKey");
        assertEquals(0, cvs.getExceptions().size()); 
    }

    @Test
    public void testIncompleteForeignKey_1() {
        String ddl = "CREATE VIEW xyz (c1, c2, FOREIGN KEY) AS SELECT c1, c2 FROM table1;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 19);
        printBeforeAndAfter(cvs, "testIncompleteForeignKey_1");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_FOREIGN_KEY)); 
    }

    @Test
    public void testIncompleteForeignKey_2() {
        String ddl = "CREATE VIEW xyz (c1, c2, FOREIGN KEY ()) AS SELECT c1, c2 FROM table1;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 21);
        printBeforeAndAfter(cvs, "testIncompleteForeignKey_2");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_FOREIGN_KEY)); 
    }

    @Test
    public void testIncompleteForeignKey_3() {
        String ddl = "CREATE VIEW xyz (c1, c2, FOREIGN KEY ()) AS SELECT c1, c2 FROM table1;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 21);
        printBeforeAndAfter(cvs, "testIncompleteForeignKey_3");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_FOREIGN_KEY)); 
    }

    @Test
    public void testIncompleteForeignKey_4() {
        String ddl = "CREATE VIEW xyz (c1, c2, FOREIGN KEY (c1, c2)) AS SELECT c1, c2 FROM table1;";
        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 24);
        printBeforeAndAfter(cvs, "testIncompleteForeignKey_4");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_FOREIGN_KEY)); 
    }

    @Test
    public void testIncompleteForeignKey_5() {
        String ddl = "CREATE VIEW xyz (c1, c2, FOREIGN KEY (c1, c2) REFERENCES) AS SELECT c1, c2 FROM table1;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 25);
        printBeforeAndAfter(cvs, "testIncompleteForeignKey_5");
        assertEquals(2, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_FOREIGN_KEY));
        assertException(cvs, Messages.getString(Messages.Error.MISSING_FK_TABLE_REF));
    }

    @Test
    public void testIncompleteForeignKey_6() {
        String ddl = "CREATE VIEW xyz (c1, c2, FOREIGN KEY (c1, c2) REFERENCES tbl1) AS SELECT c1, c2 FROM table1;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 26);
        printBeforeAndAfter(cvs, "testIncompleteForeignKey_6");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_FOREIGN_KEY)); 
    }

    @Test
    public void testIncompleteForeignKey_7() {
        String ddl = "CREATE VIEW xyz (c1, c2, FOREIGN KEY (c1, c2) REFERENCES tbl1 ()) AS SELECT c1, c2 FROM table1;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 28);
        printBeforeAndAfter(cvs, "testIncompleteForeignKey_7");
        assertEquals(1, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_FOREIGN_KEY));
    }
    
    @Test
    public void testIncompleteForeignKey_8() {
    //           //   0      1    2   3 4 5 6 7 8       9  10 11  12 13  14         15 16 17 18 19 20     21  22 23   24    25
        String ddl = "CREATE VIEW xyz ( c1, c2, FOREIGN KEY ( c1, c2 )   REFERENCES (  c1 )  )  AS SELECT c1, c2 FROM table1;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 28);
        printBeforeAndAfter(cvs, "testIncompleteForeignKey_8");
        assertEquals(2, cvs.getExceptions().size());
        assertException(cvs, Messages.getString(Messages.Error.INCOMPLETE_FOREIGN_KEY));
        assertException(cvs, Messages.getString(Messages.Error.MISSING_FK_TABLE_REF));
    }
    
}
