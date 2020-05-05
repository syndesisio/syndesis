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
import static org.junit.Assert.assertNull;

import org.eclipse.lsp4j.Position;
import org.junit.Test;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;
import io.syndesis.dv.lsp.parser.DdlTokenWalker;

@SuppressWarnings("nls")
public class TestDdlTokenWalker implements DdlAnalyzerConstants {
	
	

	@Test
    public void testFindToken() throws Exception {
        //             01234567890123456789012
        String stmt = "CREATE VIEW winelist (\n" + 
        //     01234567890123456789012345678901234567890123456789
      		  "   winename string(255), price decimal(2, 15), vendor string(255) \n" + 
      		  ") AS SELECT * FROM PostgresDB.winelist";

        DdlTokenAnalyzer analyser = new DdlTokenAnalyzer(stmt);
        DdlTokenWalker walker = new DdlTokenWalker(analyser.getTokens());

        // LINE 1 TESTS
        Token token = walker.findToken(new Position(0, 0), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertNull(token);

        token = walker.findToken(new Position(0, 1), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertNull(token);

        token = walker.findToken(new Position(0, 5), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertNull(token);

        token = walker.findToken(new Position(0, 6), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertNull(token);

        token = walker.findToken(new Position(0, 7), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(CREATE, token.kind);

        token = walker.findToken(new Position(0, 8), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(CREATE, token.kind);

        token = walker.findToken(new Position(0, 11), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(CREATE, token.kind);

        token = walker.findToken(new Position(0, 12), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(VIEW, token.kind);

        token = walker.findToken(new Position(0, 19), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(VIEW, token.kind);

        token = walker.findToken(new Position(0, 20), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(VIEW, token.kind);

        token = walker.findToken(new Position(0, 21), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(ID, token.kind);

        token = walker.findToken(new Position(0, 22), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(LPAREN, token.kind);

        // LINE 2 TESTS
        token = walker.findToken(new Position(1, 3), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(LPAREN, token.kind);

        token = walker.findToken(new Position(1, 5), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(LPAREN, token.kind);

        token = walker.findToken(new Position(1, 7), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(LPAREN, token.kind);

        token = walker.findToken(new Position(1, 12), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(ID, token.kind);

        token = walker.findToken(new Position(1, 14), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(ID, token.kind);

		token = walker.findToken(new Position(1, 18), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(STRING, token.kind);

		token = walker.findToken(new Position(1, 19), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(LPAREN, token.kind);

		token = walker.findToken(new Position(1, 21), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(LPAREN, token.kind);

		token = walker.findToken(new Position(1, 22), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(UNSIGNEDINTEGER, token.kind);

		token = walker.findToken(new Position(1, 23), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(RPAREN, token.kind);

        // LINE 3 TESTS
		token = walker.findToken(new Position(2, 1), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(RPAREN, token.kind);

		token = walker.findToken(new Position(2, 4), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(RPAREN, token.kind);

		token = walker.findToken(new Position(2, 5), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(AS, token.kind);

		token = walker.findToken(new Position(2, 11), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(AS, token.kind);

		token = walker.findToken(new Position(2, 12), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(SELECT, token.kind);

		token = walker.findToken(new Position(2, 14), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(STAR, token.kind);

		token = walker.findToken(new Position(2, 18), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(STAR, token.kind);

		token = walker.findToken(new Position(2, 19), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(FROM, token.kind);

		token = walker.findToken(new Position(2, 38), STATEMENT_TYPE.CREATE_VIEW_TYPE);
		assertEquals(ID, token.kind);

    }

	@Test
    public void testCreateViewOnly() throws Exception {
        //             01234567890123456789012
        String stmt = "CREATE VIEW foobar ( ";

        DdlTokenAnalyzer analyser = new DdlTokenAnalyzer(stmt);
        DdlTokenWalker walker = new DdlTokenWalker(analyser.getTokens());

        // LINE 1 TESTS
        Token token = walker.findToken(new Position(0, 19), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(ID, token.kind);

        token = walker.findToken(new Position(0, 20), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(LPAREN, token.kind);

        token = walker.findToken(new Position(0, 21), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(LPAREN, token.kind);
	}

	@Test
    public void testComplexCreateView() throws Exception {
		String stmt = 
              // 01234567890123456789012
				"CREATE VIEW G1(\n" +
                "e1 integer primary key,\n" +
                "e2 varchar(10) unique,\n" +
                "e3 date not null unique,\n" +
                "e4 decimal(12,3) default 12.2 options (searchable 'unsearchable'),\n" +
                "e5 integer auto_increment INDEX OPTIONS (UUID 'uuid', NAMEINSOURCE 'nis', SELECTABLE 'NO'),\n" +
                "e6 varchar index default 'hello')\n" +
              // 01234567890123456789012345678901234567890
                "OPTIONS (CARDINALITY 12, UUID 'uuid2',  UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')" +
                " AS SELECT e1, e2, e3, e4, e5, e6 from  ";

        DdlTokenAnalyzer analyser = new DdlTokenAnalyzer(stmt);
        // analyser.printTokens();
        DdlTokenWalker walker = new DdlTokenWalker(analyser.getTokens());
        
        Token token = walker.findToken(new Position(5, 40), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(OPTIONS, token.kind);

        token = walker.findToken(new Position(7, 8), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(OPTIONS, token.kind);

	}
	
    @Test
    public void testFindParensToken() throws Exception {
        //             01234567890123456789012
        String stmt = "CREATE VIEW winelist (\n" + 
        //     01234567890123456789012345678901234567890123456789
                ") AS SELECT * FROM PostgresDB.winelist";

        DdlTokenAnalyzer analyser = new DdlTokenAnalyzer(stmt);
        DdlTokenWalker walker = new DdlTokenWalker(analyser.getTokens());

        // LINE 1 TESTS
        Token token = walker.findToken(new Position(0, 6), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertNull(token);

        token = walker.findToken(new Position(0, 7), STATEMENT_TYPE.CREATE_VIEW_TYPE);
        assertEquals(CREATE, token.kind);
    }
}
