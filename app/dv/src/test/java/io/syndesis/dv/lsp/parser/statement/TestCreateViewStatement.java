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

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.StatementType;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

import org.junit.Test;
import org.teiid.query.parser.Token;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("nls")
public class TestCreateViewStatement {
    public void printTokens(Token[] tkns, String headerMessage) {
        System.out.println(headerMessage);
        for (Token token : tkns) {
            System.out.println(
                    " tkn ==>   " + token.image + "\t @ ( " + token.beginLine + ", " + token.beginColumn + ")");
        }
    }

    public CreateViewStatement createStatatement(String stmt) {
        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);

        return new CreateViewStatement(analyzer);
    }

    @Test
    public void testCreateViewStatement() {

        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW wineList (\n" +
                // 01234567890123456789012345678901234567890123456789
                        "e1 integer primary key OPTIONS (UPDATEABLE 'false', FOO 'BAR'),\n" +
                        // 01234567890123456789012345678901234567890123456789
                        "e6 varchar index default 'hello',\n" +
                        // 01234567890123456789012345678901234567890123456789
                        "e7 decimal(10,2) NOT NULL unique)\n" +
                        // 01234567890123456789012345678901234567890123456789
                        "OPTIONS (CARDINALITY 12, UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')\n" +
                        // 01234567890123456789012345678901234567890123456789
                        "AS SELECT * FROM winelist WHERE e1 > '10';";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(58, cvs.analyzer.getTokens().size());

        assertEquals("wineList", cvs.getViewName());

        assertTrue(cvs.getExceptions().isEmpty());

        TableBody tb = cvs.getTableBody();
        assertEquals(3, tb.getTableElements().length);

    }

    @Test
    public void testMissingViewName() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW (\n" +
                // 01234567890123456789012345678901234567890123456789
                        "e1 integer primary key,\n" +
                        // 01234567890123456789012345678901234567890123456789
                        "e6 varchar index default 'hello')\n" +
                        // 01234567890123456789012345678901234567890123456789
                        "AS SELECT * FROM winelist WHERE e1 > '10'";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(23, cvs.analyzer.getTokens().size());

        assertEquals(2, cvs.getExceptions().size());
    }

    @Test
    public void testNoTableBody() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW (\n" +
                // 01234567890123456789012345678901234567890123456789
                        ")\nAS SELECT * FROM winelist WHERE e1 > '10'";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(13, cvs.analyzer.getTokens().size());

        assertEquals(2, cvs.getExceptions().size());
    }

    @Test
    public void testSelectStar() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT * FROM fooBar";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(11, cvs.analyzer.getTokens().size());
        assertEquals(true, cvs.getQueryExpression().getSelectClause().isStar());

        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectWithAll() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT ALL * FROM fooBar";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(12, cvs.analyzer.getTokens().size());
        assertEquals(true, cvs.getQueryExpression().getSelectClause().isAll());

        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectWithDistinct() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT DISTINCT * FROM fooBar";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(12, cvs.analyzer.getTokens().size());
        assertEquals(true, cvs.getQueryExpression().getSelectClause().isDistinct());

        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectIntegerConstant() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT 1 FROM fooBar";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(11, cvs.analyzer.getTokens().size());
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("1",
                cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getLiteralValueToken().image);

        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectStringConstant() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT 'xxx' FROM fooBar";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(11, cvs.analyzer.getTokens().size());
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("'xxx'",
                cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getLiteralValueToken().image);

        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectConstantsAndColumn() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT 'xxx', 1, foo FROM fooBar";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(15, cvs.analyzer.getTokens().size());
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
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT bar FROM foo";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(11, cvs.analyzer.getTokens().size());
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("bar", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getNameToken().image);
        assertEquals("bar", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getColumnName());
        assertEquals(null, cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getTableName());

        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectTableAndColumn() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT foo.bar FROM foo";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(11, cvs.analyzer.getTokens().size());
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("foo.bar", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getNameToken().image);
        assertEquals("bar", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getColumnName());
        assertEquals("foo", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getTableName());

        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectAliasedColumn() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT foo as c1 FROM bar";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(13, cvs.analyzer.getTokens().size());
        assertEquals(1, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("foo", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getNameToken().image);
        assertEquals("c1", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getAliasNameToken().image);

        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testSelectFunctionColumn() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT concat(firstName, lastName) as c1 FROM names";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(18, cvs.analyzer.getTokens().size());
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
    public void testSelectTwoAliasedColumn() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW abcd ( id ) AS SELECT columnA as c1, columnB as c2 FROM bar";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(17, cvs.analyzer.getTokens().size());
        assertEquals(2, cvs.getQueryExpression().getSelectClause().getSelectColumns().length);
        assertEquals("columnA", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getNameToken().image);
        assertEquals("c1", cvs.getQueryExpression().getSelectClause().getSelectColumns()[0].getAliasNameToken().image);
        assertEquals("columnB", cvs.getQueryExpression().getSelectClause().getSelectColumns()[1].getNameToken().image);
        assertEquals("c2", cvs.getQueryExpression().getSelectClause().getSelectColumns()[1].getAliasNameToken().image);
        assertEquals(1, cvs.getExceptions().size()); // # View and SELECT columns do not match
    }
    
    @Test
    public void testReservedViewName() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW view ( id ) AS SELECT id FROM bar;";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(12, cvs.analyzer.getTokens().size());
        assertEquals(1, cvs.getExceptions().size());
    }
    
    @Test
    public void testReservedViewNameQuoted() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW \"view\" ( id ) AS SELECT id FROM bar;";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(12, cvs.analyzer.getTokens().size());
        assertEquals(0, cvs.getExceptions().size());
    }

    @Test
    public void testValidNumProjectedSymbols() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW foo (c1, c2, c3 ) AS SELECT c1, c2, c3 FROM bar;";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(20, cvs.analyzer.getTokens().size());
        assertEquals(0, cvs.getExceptions().size());
    }
    
    @Test
    public void testInvalidNumProjectedSymbols() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW foo (c1, c2 ) AS SELECT c1, FROM bar;";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(15, cvs.analyzer.getTokens().size());
        assertEquals(2, cvs.getExceptions().size());
    }
    
    @Test
    public void testExtraTableElementColumn() {
        String stmt =
                // 01234567890123456789012345678901234567890123456789
                "CREATE VIEW foo (c1, c2, ) AS SELECT c1, c2 FROM bar;";

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(17, cvs.analyzer.getTokens().size());
        assertEquals(1, cvs.getExceptions().size());
    }
}