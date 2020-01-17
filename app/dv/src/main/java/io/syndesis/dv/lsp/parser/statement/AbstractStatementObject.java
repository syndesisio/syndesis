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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public abstract class AbstractStatementObject implements DdlAnalyzerConstants  {
    DdlTokenAnalyzer analyzer;
    int firstTknIndex;
    int lastTknIndex;
    
//    protected final List<DdlAnalyzerException> exceptions = new ArrayList<DdlAnalyzerException>();

    public AbstractStatementObject(DdlTokenAnalyzer analyzer) {
        super();
        this.analyzer = analyzer;
    }

    public Token[] getTokens() {
        return this.analyzer.getTokens();
    }
    
    public DdlTokenAnalyzer getAnalyzer() {
        return this.analyzer;
    }

    protected abstract void parseAndValidate();

    protected abstract TokenContext getTokenContext(Position position);

    public DdlAnalyzerException checkAllBrackets(int leftBracketKind, int rightBracketKind) {
        int numUnmatchedParens = 0;
        DdlAnalyzerException exception = null;
        Token[] tkns = getTokens();
        for (int iTkn = 0; iTkn < tkns.length; iTkn++) {
            Token token = tkns[iTkn];
            if (token.kind == leftBracketKind) {
                numUnmatchedParens++;
            }
            if (token.kind == rightBracketKind) {
                numUnmatchedParens--;
            }

            // If the ## goes < 0 throw exception because they should be correctly nested
            // VALID: ( () () )
            // INVALID ( )) () (
            // ^ would occur here
            if (exception == null && numUnmatchedParens < 0) {
                exception = new DdlAnalyzerException("Bracket at location " //$NON-NLS-1$
                        + this.analyzer.getPositionString(token) + " does not properly match previous bracket"); //$NON-NLS-1$
            }
            if (exception != null)
                break;
        }

        if (numUnmatchedParens != 0) {
            exception = new DdlAnalyzerException("Missing or mismatched brackets"); //$NON-NLS-1$
        }

        return exception;
    }

    protected Token findTokenByKind(int kind) {
        for (Token tkn : getTokens()) {
            if (tkn.kind == kind) {
                return tkn;
            }
        }

        return null;
    }

    protected Position getBeginPosition(Token token) {
        return new Position(token.beginLine, token.beginColumn);
    }

    protected Position getEndPosition(Token token) {
        return new Position(token.endLine, token.endColumn);
    }

    protected boolean isBetween(int firstTknIndex, int lastTknIndex, Position target) {
        // get token for target position
        Token token = analyzer.getTokenFor(target);
        int tokenIndex = getTokenIndex(token);

        return tokenIndex >= firstTknIndex && tokenIndex <= lastTknIndex;
    }

    public int getTokenIndex(Token token) {
        if (token == null)  {
            return 0;
        }

        int index = 0;
        for (Token tkn : getTokens()) {
            if (token.equals(tkn)) {
                return index;
            }
            index++;
        }
        return -1;
    }

    protected int getFirstTknIndex() {
        return firstTknIndex;
    }

    protected void setFirstTknIndex(int firstTknIndex) {
        this.firstTknIndex = firstTknIndex;
    }

    protected int getLastTknIndex() {
        return lastTknIndex;
    }

    protected void setLastTknIndex(int lastTknIndex) {
        this.lastTknIndex = lastTknIndex;
    }
    
    protected boolean hasAnotherToken(Token[] tkns, int currentIndex) {
        return currentIndex+2 < tkns.length;
    }

    protected boolean isNextTokenOfKind(Token[] tkns, int currentIndex, int kind) {
        return hasAnotherToken(tkns, currentIndex) && tkns[currentIndex+1].kind == kind;
    }
    
    public Token[] getBracketedTokens(Token[] tkns, int startTokenId, int bracketStartKind, int bracketEndKind) {
        int numUnmatchedParens = 0;

        for(int iTkn = 0; iTkn<getTokens().length; iTkn++) {
            if( iTkn < startTokenId) {
                continue;
            }
            Token token = tkns[iTkn];
            if( token.kind == bracketStartKind) {
                numUnmatchedParens++;
            }
            if( token.kind == bracketEndKind) {
                numUnmatchedParens--;
            }

            if( numUnmatchedParens == 0) {
                List<Token> bracketedTokens = new ArrayList<Token>(tkns.length);
                bracketedTokens.add(tkns[startTokenId]);
                for(int jTkn = startTokenId+1; jTkn < iTkn; jTkn++) {
                    bracketedTokens.add(tkns[jTkn]);
                }
                bracketedTokens.add(tkns[startTokenId + bracketedTokens.size()]);
                return bracketedTokens.toArray(new Token[0]);
            }
        }

//        Token startTkn = tkns[startTokenId];
        
        return tkns;
//        throw new DdlAnalyzerException(
//                "Brackets do not match for bracket type '" + startTkn.image +
//                "' at position (" + startTkn.beginLine + ", " + startTkn.beginColumn + ")");
    }
    
    public String positionToString(Position position) {
        return "Line " + (position.getLine()+1) + " Column " + (position.getCharacter()+1);
    }

    protected void printTokens(Token[] tkns, String headerMessage) {
        System.out.println(headerMessage);
        for (Token token : tkns) {
            System.out.println("  >> Token = " + token.image + "\n\t   >> KIND = " + token.kind
                    + "\n\t   >> begins at ( " + token.beginLine + ", " + token.beginColumn + " )"
                    + "\n\t   >> ends   at ( " + token.endLine + ", " + token.endColumn + " )");
        }
    }
}
