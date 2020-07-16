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

import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.StatementType;

public class TestStatementUtils {

    boolean allowPrint = false;
    
    public boolean allowPrint() {
        return allowPrint;
    }

    public void setAllowPrint(boolean allowPrint) {
        this.allowPrint = allowPrint;
    }

    public void printTokens(Token[] tkns, String headerMessage) {
        System.out.println(headerMessage);
        for (Token token : tkns) {
            System.out.println(
                    " tkn ==>   " + token.image + "\t @ ( " + token.beginLine + ", " + token.beginColumn + ")");
        }
    }

    public void printExceptions(CreateViewStatement cvs) {
        if (allowPrint) {
            if( cvs.getExceptions().isEmpty() ) {
                System.out.println("  NO EXCEPTIONS FOUND");
            } else {
                System.out.println("\n" + cvs.getAnalyzer().getReport());
            }
        }
    }
    
    public void printBeforeAndAfter(CreateViewStatement cvs, String testMethodName) {
        if (allowPrint) {
            System.out.println("-------------------------------------------------");
            System.out.println("Test: " + testMethodName);
            System.out.println("---------  DDL INPUT ----------");
            System.out.println(cvs.getStatement());
            System.out.println("-------------------------------");
            System.out.println("\n======== STATEMENT OUTPUT =====");
            System.out.println(cvs.toString());
            System.out.println("===============================");
            printExceptions(cvs);
            System.out.println("\n");
        }
    }

    public CreateViewStatement createStatement(String stmt) {
        DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(stmt);

        return new CreateViewStatement(analyzer);
    }
    
    public CreateViewStatement parseAndBasicAssertStatement(String ddl, int numTokens) {
        CreateViewStatement cvs = createStatement(ddl);
        assertBasicStatement(cvs, numTokens);
        return cvs;
    }
    
    public void assertBasicStatement(CreateViewStatement cvs, int numTokens) {
        assertEquals(StatementType.CREATE_VIEW_TYPE, cvs.analyzer.getStatementType());
        assertEquals(numTokens, cvs.analyzer.getTokens().size());
    }
    
    public boolean assertException(CreateViewStatement cvs, String expectedMsg) {
        for(DdlAnalyzerException ex: cvs.getExceptions()) {
            if( expectedMsg.contentEquals(ex.getMessage()) ) {
                return true;
            }
        }
        return false;
    }
    
    public boolean containsErrorCode(CreateViewStatement cvs, String errorCode) {
        for(DdlAnalyzerException ex: cvs.getExceptions()) {
            if( ex.getErrorCode() != null ) {
                if ( errorCode.contentEquals(ex.getErrorCode())) {
                    return true;
                }
            }
        }
        return false;
    }

}
