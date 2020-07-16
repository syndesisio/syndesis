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
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;


public class TestSelectFunction extends TestStatementUtils {

    @Test
    public void testSelectFunction() {
        String ddl =
              // 0      1    2    3 4  5 6  7      8     9 10       11 12      13 14 15 16   17     18
                "CREATE VIEW abcd ( id ) AS SELECT concat( firstName,  lastName)  as c1 FROM names  ;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 19);
        printBeforeAndAfter(cvs, "testSelectFunctionColumn");
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals(null, cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getNameToken());
        assertEquals("concat", ((SelectFunction) cvs.getQueryExpression().getSelectClause().getSelectColumns()[0])
                .getFunctionNameToken().image);
        assertEquals(2, ((SelectFunction) cvs.getQueryExpression().getSelectClause().getSelectColumns()[0])
                .getParameters().size());
        assertEquals("c1", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getAliasNameToken().image);
        assertEquals(0, cvs.getExceptions().size());
    }
    
    @Test
    public void testSelectTwoFunctions() {
        String ddl =
              // 0      1    2    3 4  5 6  7      8     9 10       11 12      13 14 15  16 17     18 19 20  21 22 23   24   25
                "CREATE VIEW abcd ( id ) AS SELECT concat( firstName,  lastName)  as c1  ,  concat2(  foo,  'bar')  FROM names;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 26);
        printBeforeAndAfter(cvs, "testSelectTwoFunctions");
        assertEquals(2, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals(null, cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getNameToken());
        assertEquals("concat", ((SelectFunction) cvs.getQueryExpression().getSelectClause().getSelectColumns()[0])
                .getFunctionNameToken().image);
        assertEquals(2, ((SelectFunction) cvs.getQueryExpression().getSelectClause().getSelectColumns()[0])
                .getParameters().size());
        assertEquals("c1", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getAliasNameToken().image);
        assertEquals(1, cvs.getExceptions().size());
    }
    
    @Test
    public void testSelectFunctionInvalid_1() {
        String ddl =
              // 0      1    2    3 4  5 6  7      8     9 10       11 12      13 14 15 16 17   18   19 
                "CREATE VIEW abcd ( id ) AS SELECT concat( firstName,  lastName)  as c1 ,  FROM names;";

        CreateViewStatement cvs = parseAndBasicAssertStatement(ddl, 20);
        printBeforeAndAfter(cvs, "testSelectFunctionColumn");
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("concat", ((SelectFunction) cvs.getQueryExpression().getSelectClause().getSelectColumns()[0])
                .getFunctionNameToken().image);
        assertEquals(2, ((SelectFunction) cvs.getQueryExpression().getSelectClause().getSelectColumns()[0])
                .getParameters().size());
        assertEquals("c1", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getAliasNameToken().image);
        assertEquals(1, cvs.getExceptions().size());
        assertEquals("Expected error not found: " + Messages.Error.UNEXPECTED_COMMA,
                true, containsErrorCode(cvs, QuickFixFactory.DiagnosticErrorId.UNEXPECTED_COMMA.getErrorCode()));
    }

}
