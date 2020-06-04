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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.lsp4j.Position;
import org.junit.Test;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;
import io.syndesis.dv.lsp.parser.DdlTokenWalker;

@SuppressWarnings("nls")
public class TestDdlTokenWalker {

    @Test
    public void testFindToken() {
        // 01234567890123456789012
        String stmt = "CREATE VIEW winelist (\n" +
                    // 01234567890123456789012345678901234567890123456789
                      "   winename string(255), price decimal(2, 15), vendor string(255) \n"
                    + ") AS SELECT * FROM PostgresDB.winelist";

        DdlTokenAnalyzer analyser = new DdlTokenAnalyzer(stmt);
        DdlTokenWalker walker = new DdlTokenWalker(analyser.getTokens());

        // LINE 1 TESTS
        Token token = walker.findToken(new Position(0, 0), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertNull(token);

        token = walker.findToken(new Position(0, 1), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertNull(token);

        token = walker.findToken(new Position(0, 5), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertNull(token);

        token = walker.findToken(new Position(0, 6), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertNull(token);

        token = walker.findToken(new Position(0, 7), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.CREATE, token.kind);

        token = walker.findToken(new Position(0, 8), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.CREATE, token.kind);

        token = walker.findToken(new Position(0, 11), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.CREATE, token.kind);

        token = walker.findToken(new Position(0, 12), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.VIEW, token.kind);

        token = walker.findToken(new Position(0, 19), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.VIEW, token.kind);

        token = walker.findToken(new Position(0, 20), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.VIEW, token.kind);

        token = walker.findToken(new Position(0, 21), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.ID, token.kind);

        token = walker.findToken(new Position(0, 22), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.LPAREN, token.kind);

        // LINE 2 TESTS
        token = walker.findToken(new Position(1, 3), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.LPAREN, token.kind);

        token = walker.findToken(new Position(1, 5), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.LPAREN, token.kind);

        token = walker.findToken(new Position(1, 7), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.LPAREN, token.kind);

        token = walker.findToken(new Position(1, 12), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.ID, token.kind);

        token = walker.findToken(new Position(1, 14), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.ID, token.kind);

        token = walker.findToken(new Position(1, 18), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.STRING, token.kind);

        token = walker.findToken(new Position(1, 19), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.LPAREN, token.kind);

        token = walker.findToken(new Position(1, 21), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.LPAREN, token.kind);

        token = walker.findToken(new Position(1, 22), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.UNSIGNEDINTEGER, token.kind);

        token = walker.findToken(new Position(1, 23), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.RPAREN, token.kind);

        // LINE 3 TESTS
        token = walker.findToken(new Position(2, 1), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.RPAREN, token.kind);

        token = walker.findToken(new Position(2, 4), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.RPAREN, token.kind);

        token = walker.findToken(new Position(2, 5), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.AS, token.kind);

        token = walker.findToken(new Position(2, 11), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.AS, token.kind);

        token = walker.findToken(new Position(2, 12), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.SELECT, token.kind);

        token = walker.findToken(new Position(2, 14), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.STAR, token.kind);

        token = walker.findToken(new Position(2, 18), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.STAR, token.kind);

        token = walker.findToken(new Position(2, 19), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.FROM, token.kind);

        token = walker.findToken(new Position(2, 38), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.ID, token.kind);

    }

    @Test
    public void testCreateViewOnly() {
        // 01234567890123456789012
        String stmt = "CREATE VIEW foobar ( ";

        DdlTokenAnalyzer analyser = new DdlTokenAnalyzer(stmt);
        DdlTokenWalker walker = new DdlTokenWalker(analyser.getTokens());

        // LINE 1 TESTS
        Token token = walker.findToken(new Position(0, 19), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.ID, token.kind);

        token = walker.findToken(new Position(0, 20), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.LPAREN, token.kind);

        token = walker.findToken(new Position(0, 21), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.LPAREN, token.kind);
    }

    @Test
    public void testComplexCreateView() {
        String stmt =
            // 01234567890123456789012
                "CREATE VIEW G1(\n" + "e1 integer primary key,\n" + "e2 varchar(10) unique,\n"
              + "e3 date not null unique,\n"
              + "e4 decimal(12,3) default 12.2 options (searchable 'unsearchable'),\n"
              + "e5 integer auto_increment INDEX OPTIONS (UUID 'uuid', NAMEINSOURCE 'nis', SELECTABLE 'NO'),\n"
              + "e6 varchar index default 'hello')\n" +
              // 01234567890123456789012345678901234567890
              "OPTIONS (CARDINALITY 12, UUID 'uuid2',  UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')"
            + " AS SELECT e1, e2, e3, e4, e5, e6 from  ";

        DdlTokenAnalyzer analyser = new DdlTokenAnalyzer(stmt);
        // analyser.printTokens();
        DdlTokenWalker walker = new DdlTokenWalker(analyser.getTokens());

        Token token = walker.findToken(new Position(5, 40), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.OPTIONS, token.kind);

        token = walker.findToken(new Position(7, 8), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.OPTIONS, token.kind);

    }

    @Test
    public void testFindParensToken() {
        // 01234567890123456789012
        String stmt = "CREATE VIEW winelist (\n" +
        // 01234567890123456789012345678901234567890123456789
                ") AS SELECT * FROM PostgresDB.winelist";

        DdlTokenAnalyzer analyser = new DdlTokenAnalyzer(stmt);
        DdlTokenWalker walker = new DdlTokenWalker(analyser.getTokens());

        // LINE 1 TESTS
        Token token = walker.findToken(new Position(0, 6), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertNull(token);

        token = walker.findToken(new Position(0, 7), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertEquals(SQLParserConstants.CREATE, token.kind);
    }

    @Test
    public void testFindCommaTokenAfterAliasTableSymbol() {
        // 01234567890123456789012
        String stmt = "CREATE VIEW winelist (\n" +
        // |
        // v
        // 01234567890123456789012345678901234567890123456789
                ") AS SELECT * FROM PostgresDB.winelist as t1, ";

        DdlTokenAnalyzer analyser = new DdlTokenAnalyzer(stmt);
        DdlTokenWalker walker = new DdlTokenWalker(analyser.getTokens());

        // LINE 1 TESTS
        Token token = walker.findToken(new Position(1, 45), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertNotNull(token);
        assertEquals(SQLParserConstants.COMMA, token.kind);
    }

    @Test
    public void testIsAfterFromClause() {
        // 0123456789 123456789 123456789 123456789 123456789 123456789 123456789
        String stmt = "CREATE VIEW winelist AS SELECT * FROM PostgresDB.winelist as t1 WHERE id > 2000";

        DdlTokenAnalyzer analyser = new DdlTokenAnalyzer(stmt);
        DdlTokenWalker walker = new DdlTokenWalker(analyser.getTokens());

        // LINE 1 TESTS
        Token token = walker.findToken(new Position(0, 63), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertNotNull(token);
        assertEquals(SQLParserConstants.AS, token.kind);

        token = walker.findToken(new Position(0, 64), DdlAnalyzerConstants.StatementType.CREATE_VIEW_TYPE);
        assertNotNull(token);
        assertEquals(SQLParserConstants.ID, token.kind);

        token = analyser.getTokenAt(new Position(0, 65));
        assertEquals(SQLParserConstants.WHERE, token.kind);
    }
}
