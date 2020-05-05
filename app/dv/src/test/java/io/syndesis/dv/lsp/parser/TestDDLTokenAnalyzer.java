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

import static org.junit.Assert.*;

import org.junit.Test;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.STATEMENT_TYPE;

@SuppressWarnings("nls")
public class TestDDLTokenAnalyzer {
    public void printTokens(Token[] tkns, String headerMessage) {
        System.out.println(headerMessage);
        for (Token token : tkns) {
            System.out.println(" tkn ==>   " + token.image
                    + "\t @ ( " + 
                    token.beginLine + ", " + token.beginColumn + ")");
        }
    }
    @Test
    public void testForeignTable() throws Exception {

        String stmt = "CREATE FOREIGN TABLE G1(\n" +
                        "e1 integer primary key,\n" +
                        "e4 decimal(12,3) default 12.2 options (searchable 'unsearchable'),\n" +
                        "e5 integer auto_increment INDEX OPTIONS (UUID 'uuid', NAMEINSOURCE 'nis', SELECTABLE 'NO'),\n" +
                        "e6 varchar index default 'hello')\n" +
                        "OPTIONS (CARDINALITY 12, UUID 'uuid2',  UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')";
        

        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);
        printTokens(analyzer.getTokens(), "testForeignTable = nTokens  " + analyzer.getTokens().length);
        
        assertEquals(STATEMENT_TYPE.CREATE_FOREIGN_TABLE_TYPE, analyzer.getStatementType());
        assertEquals(64, analyzer.getTokens().length);
    }
    
    @Test
    public void testCreateVirtualView() throws Exception {

        String stmt = "CREATE VIRTUAL VIEW winelist(\n" +
                        "e1 integer primary key,\n" +
                        "e2 varchar(10) unique,\n" +
                        "e3 date not null unique,\n" +
                        "e4 decimal(12,3) default 12.2 options (searchable 'unsearchable'),\n" +
                        "e5 integer auto_increment INDEX OPTIONS (UUID 'uuid', SELECTABLE 'NO'),\n" +
                        "e6 varchar index default 'hello')\n" +
                        "OPTIONS (CARDINALITY 12, UUID 'uuid2',  UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')";
        
        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);
        printTokens(analyzer.getTokens(), "testCreateVirtualView nTokens  = " + analyzer.getTokens().length);
        
        assertEquals(STATEMENT_TYPE.CREATE_VIRTUAL_VIEW_TYPE, analyzer.getStatementType());
        assertEquals(74, analyzer.getTokens().length);
    }
    
    @Test
    public void testViewName() throws Exception {
        String stmt = "CREATE VIEW \"wineList xxx\" (\n" +
                        "e1 integer primary key,\n" +
                        "e6 varchar index default 'hello')\n" +
                        "OPTIONS (CARDINALITY 12, UUID 'uuid2',  UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')";

        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);
        printTokens(analyzer.getTokens(), "testViewName() nTokens = " + analyzer.getTokens().length);

        assertEquals(STATEMENT_TYPE.CREATE_VIEW_TYPE, analyzer.getStatementType());
        assertEquals(32, analyzer.getTokens().length);
    }
    
    @Test
    public void testCreateViewSimple() throws Exception {
        String stmt = "CREATE VIEW winelist( e4 decimal(12,3) default 12.2 options (searchable 'unsearchable') )";

        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);
        printTokens(analyzer.getTokens(), "testCreateViewWithDatatypes nTokens  = " + analyzer.getTokens().length);

        assertEquals(STATEMENT_TYPE.CREATE_VIEW_TYPE, analyzer.getStatementType());
        assertEquals(19, analyzer.getTokens().length);
    }
    
    @Test
    public void testCreateViewWithDatatypes() throws Exception {
        String stmt = "CREATE VIEW winelist(\n" +
                        "e1 integer primary key,\n" +
                        "e2 varchar(10) unique,\n" +
                        "e4 decimal(12,3) default 12.2 options (searchable 'unsearchable'))";
        
        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);
        printTokens(analyzer.getTokens(), "testCreateViewWithDatatypes nTokens  = " + analyzer.getTokens().length);

        assertEquals(STATEMENT_TYPE.CREATE_VIEW_TYPE, analyzer.getStatementType());
        assertEquals(31, analyzer.getTokens().length);
    }

}
