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
package io.syndesis.dv.lsp.parser;

import java.util.List;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.StatementType;

import org.junit.Test;
import org.teiid.query.parser.Token;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("nls")
public class TestDDLTokenAnalyzer {
    public void printTokens(List<Token> list, String headerMessage) {
        System.out.println(headerMessage);
        for (Token token : list) {
            System.out.println(
                    " tkn ==>   " + token.image + "\t @ ( " + token.beginLine + ", " + token.beginColumn + ")");
        }
    }

    @Test
    public void testForeignTable() {

        String stmt = "CREATE FOREIGN TABLE G1(\n" + "e1 integer primary key,\n"
                + "e4 decimal(12,3) default 12.2 options (searchable 'unsearchable'),\n"
                + "e5 integer auto_increment INDEX OPTIONS (UUID 'uuid', NAMEINSOURCE 'nis', SELECTABLE 'NO'),\n"
                + "e6 varchar index default 'hello')\n"
                + "OPTIONS (CARDINALITY 12, UUID 'uuid2',  UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')";

        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);

        assertEquals(StatementType.CREATE_FOREIGN_TABLE_TYPE, analyzer.getStatementType());
        assertEquals(64, analyzer.getTokens().size());
    }

    @Test
    public void testCreateVirtualView() {

        String stmt = "CREATE VIRTUAL VIEW winelist(\n" + "e1 integer primary key,\n" + "e2 varchar(10) unique,\n"
                + "e3 date not null unique,\n" + "e4 decimal(12,3) default 12.2 options (searchable 'unsearchable'),\n"
                + "e5 integer auto_increment INDEX OPTIONS (UUID 'uuid', SELECTABLE 'NO'),\n"
                + "e6 varchar index default 'hello')\n"
                + "OPTIONS (CARDINALITY 12, UUID 'uuid2',  UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')";

        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);

        assertEquals(StatementType.CREATE_VIRTUAL_VIEW_TYPE, analyzer.getStatementType());
        assertEquals(74, analyzer.getTokens().size());
    }

    @Test
    public void testViewName() {
        String stmt = "CREATE VIEW \"wineList xxx\" (\n" + "e1 integer primary key,\n"
                + "e6 varchar index default 'hello')\n"
                + "OPTIONS (CARDINALITY 12, UUID 'uuid2',  UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')";

        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, analyzer.getStatementType());
        assertEquals(32, analyzer.getTokens().size());
    }

    @Test
    public void testCreateViewSimple() {
        String stmt = "CREATE VIEW winelist( e4 decimal(12,3) default 12.2 options (searchable 'unsearchable') )";

        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, analyzer.getStatementType());
        assertEquals(19, analyzer.getTokens().size());
    }

    @Test
    public void testCreateViewWithDatatypes() {
        String stmt = "CREATE VIEW winelist(\n" + "e1 integer primary key,\n" + "e2 varchar(10) unique,\n"
                + "e4 decimal(12,3) default 12.2 options (searchable 'unsearchable'))";

        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, analyzer.getStatementType());
        assertEquals(31, analyzer.getTokens().size());
    }

    @Test
    public void testViewWithForeignKey() {
        String stmt = "CREATE VIEW foo (c1, c2, FOREIGN KEY (c1, c2) REFERENCES otherTable(a1, a2)) AS SELECT c1, c2 FROM bar;";

        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);

        assertEquals(StatementType.CREATE_VIEW_TYPE, analyzer.getStatementType());
        assertEquals(31, analyzer.getTokens().size());
        assertEquals("FOREIGN", analyzer.getToken(8).image);
        assertEquals("a2", analyzer.getToken(20).image);
        assertEquals(")", analyzer.getToken(21).image);
        assertEquals(")", analyzer.getToken(22).image);
    }

}
