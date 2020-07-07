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
import java.util.Locale;

import org.eclipse.lsp4j.Position;
import org.teiid.language.SQLConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public abstract class AbstractStatementObject {
    DdlTokenAnalyzer analyzer;
    int firstTknIndex;
    int lastTknIndex;

    protected AbstractStatementObject(DdlTokenAnalyzer analyzer) {
        this.analyzer = analyzer;
    }

    public List<Token> getTokens() {
        return this.analyzer.getTokens();
    }

    public DdlTokenAnalyzer getAnalyzer() {
        return this.analyzer;
    }

    protected abstract void parseAndValidate();

    protected abstract TokenContext getTokenContext(Position position);

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

    protected boolean isWithin(int firstTknIndex, int lastTknIndex, Position target) {
        // get token for target position
        Token token = analyzer.getTokenFor(target);
        int tokenIndex = getTokenIndex(token);
        if (tokenIndex >= firstTknIndex && tokenIndex <= lastTknIndex) {
            return this.analyzer.isPositionInToken(target, token);
        }
        return false;
    }

    protected boolean isBetween(int firstTknIndex, int lastTknIndex, Position target) {
        // get token for target position
        Token token = analyzer.getTokenFor(target);
        int tokenIndex = getTokenIndex(token);

        return tokenIndex >= firstTknIndex && tokenIndex <= lastTknIndex;
    }

    protected boolean isWithinSingleToken(int firstTknIndex, int lastTknIndex, Position target) {
        Token token = analyzer.getTokenFor(target);
        int tokenIndex = getTokenIndex(token);

        return firstTknIndex == lastTknIndex && tokenIndex == firstTknIndex;
    }

    public boolean isTokenInObject(Token token) {
        int index = getTokenIndex(token);
        return index >= getFirstTknIndex() && index <= getLastTknIndex();
    }

    public boolean isFirstToken(Token token) {
        return getTokenIndex(token) == getFirstTknIndex();
    }

    public boolean isLastToken(Token token) {
        return getTokenIndex(token) == getLastTknIndex();
    }

    public boolean isPositionInNextToken(Position pos, Token targetToken) {
        int currentIndex = getTokenIndex(targetToken);
        if (currentIndex < getTokens().size() - 1) {
            Token nextTkn = getTokens().get(currentIndex + 1);
            return getAnalyzer().isPositionInToken(pos, nextTkn);
        }
        return false;
    }

    public int getTokenIndex(Token token) {
        if (token == null) {
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

    protected boolean hasAnotherToken(List<Token> tkns, int currentIndex) {
        return currentIndex + 2 < tkns.size();
    }

    protected boolean isNextTokenOfKind(List<Token> tkns, int currentIndex, int kind) {
        return hasAnotherToken(tkns, currentIndex) && tkns.get(currentIndex + 1).kind == kind;
    }

    public List<Token> getBracketedTokens(List<Token> tkns, int startTokenId, int bracketStartKind,
            int bracketEndKind) {
        int numUnmatchedParens = 0;

        for (int iTkn = 0; iTkn < getTokens().size(); iTkn++) {
            if (iTkn < startTokenId) {
                continue;
            }
            Token token = tkns.get(iTkn);
            if (token.kind == bracketStartKind) {
                numUnmatchedParens++;
            }
            if (token.kind == bracketEndKind) {
                numUnmatchedParens--;
            }

            if (numUnmatchedParens == 0) {
                List<Token> bracketedTokens = new ArrayList<Token>(tkns.size());
                bracketedTokens.add(tkns.get(startTokenId));
                for (int jTkn = startTokenId + 1; jTkn < iTkn; jTkn++) {
                    bracketedTokens.add(tkns.get(jTkn));
                }
                bracketedTokens.add(tkns.get(startTokenId + bracketedTokens.size()));
                return bracketedTokens;
            }
        }
        return tkns;
    }

    public String positionToString(Position position) {
        return "Line " + (position.getLine() + 1) + " Column " + (position.getCharacter() + 1);
    }

    protected boolean isReservedKeywordToken(Token tkn) {
        return SQLConstants.isReservedWord(tkn.image.toUpperCase(Locale.US));
    }

    protected boolean isNonReservedKeywordToken(Token tkn) {
        return SQLConstants.getNonReservedWords().contains(tkn.image.toUpperCase(Locale.US));
    }

    @SuppressWarnings("PMD.SystemPrintln")
    protected void printTokens(Token[] tkns, String headerMessage) {
        System.out.println(headerMessage);
        for (Token token : tkns) {
            System.out.println("  >> Token = " + token.image + "\n\t   >> KIND = " + token.kind
                    + "\n\t   >> begins at ( " + token.beginLine + ", " + token.beginColumn + " )"
                    + "\n\t   >> ends   at ( " + token.endLine + ", " + token.endColumn + " )");
        }
    }
}
