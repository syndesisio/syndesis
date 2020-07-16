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

public class TestQueryExpression extends TestStatementUtils {

    @Test
    public void testSimpleSelectQuery() {
        String ddl =
              // 0      1    2    3 4  5 6  7      8     9    10     11 12 13
                "CREATE VIEW abcd ( id ) AS SELECT t1.id FROM fooBar as t1 ;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 14);
        printBeforeAndAfter(cvs, "testSimpleSelectQuery");
        assertEquals(0, cvs.getExceptions().size());
    }
    
    @Test
    public void testSimpleSelectQueryWithWhereClause() {
        String ddl =
              // 0      1    2    3 4  5 6  7      8     9    10     11 12 13    14 15 16 17
                "CREATE VIEW abcd ( id ) AS SELECT t1.id FROM fooBar as t1 WHERE id >  256;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 18);
        printBeforeAndAfter(cvs, "testSimpleSelectQuery");
        assertEquals(0, cvs.getExceptions().size());
    }
    
    @Test
    public void testMissingASPrefix() {
        String ddl =
              // 0      1    2    3 4  5 6      7     8    9     10
                "CREATE VIEW abcd ( id ) SELECT t1.id FROM fooBar;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 11);
        printBeforeAndAfter(cvs, "testSimpleSelectQuery");
        assertEquals(1, cvs.getExceptions().size());
    }
}
