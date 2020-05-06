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

import static org.junit.Assert.*;

import org.junit.Test;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.STATEMENT_TYPE;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;


@SuppressWarnings("nls")
public class TestCreateViewStatement {
    public void printTokens(Token[] tkns, String headerMessage) {
        System.out.println(headerMessage);
        for (Token token : tkns) {
            System.out.println(" tkn ==>   " + token.image
                    + "\t @ ( " + 
                    token.beginLine + ", " + token.beginColumn + ")");
        }
    }
    
    public CreateViewStatement createStatatement(String stmt) {
        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);

    	return new CreateViewStatement(analyzer);
    }
    
    @Test
    public void testCreateViewStatement() throws Exception {

        String stmt = 
//        	     01234567890123456789012345678901234567890123456789
        		"CREATE VIEW wineList (\n" +
//        	     01234567890123456789012345678901234567890123456789
                "e1 integer primary key OPTIONS (UPDATEABLE 'false', FOO 'BAR'),\n" +
//               01234567890123456789012345678901234567890123456789
                "e6 varchar index default 'hello',\n" +
//               01234567890123456789012345678901234567890123456789
				"e7 decimal(10,2) NOT NULL unique)\n" +
//               01234567890123456789012345678901234567890123456789
                "OPTIONS (CARDINALITY 12, UPDATABLE 'true', FOO 'BAR', ANNOTATION 'Test Table')\n" +
//               01234567890123456789012345678901234567890123456789
                "AS SELECT * FROM winelist WHERE e1 > '10';";
        

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(STATEMENT_TYPE.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(58, cvs.analyzer.getTokens().length);
        
        assertEquals("wineList", cvs.getViewName());

        assertTrue(cvs.getExceptions().isEmpty());
        
        TableBody tb = cvs.getTableBody();
        
        for(TableElement element: tb.getTableElements()) {
        	System.out.println(element);
        }
    }
    
    @Test
    public void testMissingViewName() throws Exception {
        String stmt = 
//        	     01234567890123456789012345678901234567890123456789
        		"CREATE VIEW (\n" +
//        	     01234567890123456789012345678901234567890123456789
                "e1 integer primary key,\n" +
//               01234567890123456789012345678901234567890123456789
                "e6 varchar index default 'hello')\n" +
//               01234567890123456789012345678901234567890123456789
                "AS SELECT * FROM winelist WHERE e1 > '10'";
        

        CreateViewStatement cvs = createStatatement(stmt);

        assertEquals(STATEMENT_TYPE.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(23, cvs.analyzer.getTokens().length);

        assertEquals(2, cvs.getExceptions().size());
    }
    
    @Test
    public void testNoTableBody() throws Exception {
        String stmt = 
//               01234567890123456789012345678901234567890123456789
                "CREATE VIEW (\n" +
//               01234567890123456789012345678901234567890123456789
                ")\nAS SELECT * FROM winelist WHERE e1 > '10'";

        CreateViewStatement cvs = createStatatement(stmt);
        
        assertEquals(STATEMENT_TYPE.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(13, cvs.analyzer.getTokens().length);

        assertEquals(2, cvs.getExceptions().size());
    }
}