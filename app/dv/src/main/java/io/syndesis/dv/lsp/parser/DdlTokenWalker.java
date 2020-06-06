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

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import java.util.List;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.StatementType;

public class DdlTokenWalker implements SQLParserConstants {
    private final List<Token> tokens;

    public DdlTokenWalker(List<Token> list) {
        this.tokens = list;
    }

    /*
     * Uses 0 based line/column positions for cursor and token values
     */
    @SuppressWarnings("PMD.NPathComplexity") // TODO refactor
    public Token findToken(Position position, StatementType statementType) {
        int line = position.getLine();
        int column = position.getCharacter();

        if (line == 0 && column == 0) {
            return null;
        }

        TokenCursorLocator previousLocator = null; // last token where cursor is or was (if in blank space)

        if (!tokens.isEmpty()) {
            previousLocator = new TokenCursorLocator(tokens.get(0), 0, line, column);

            // If cursor isn't past the first token, return null;
            if (previousLocator.isCursorBefore()) {
                return null;
            }
            // If cursor isn't past the first token, return null;
            if (previousLocator.isCursorInside()) {
                return null;
            }

            // Case where the cursor is located at least 1 character after the first token
            // (open space)
            if (tokens.size() == 1 && previousLocator.isCursorFreeFromToken()) {
                return previousLocator.getToken();
            }

            boolean isLastToken = false;
            TokenCursorLocator nextLocator = null;

            TokenCursorLocator lastFreeLocator = null;
            if (previousLocator != null) {
                lastFreeLocator = new TokenCursorLocator(previousLocator);
            }

            for (int iTkn = 1; iTkn < tokens.size(); iTkn++) {
                // Check if last token, else create nextLocator
                nextLocator = new TokenCursorLocator(tokens.get(iTkn), iTkn, line, column);
                if (iTkn == tokens.size() - 1) {
                    isLastToken = true;
                }

                // if still before or inside next locator, send back previousToken
                if (nextLocator.isCursorInside()) {
                    return previousLocator.getToken();
                } else if (previousLocator.isCursorAfter() && nextLocator.isCursorBefore()) {

                    if (iTkn == 1) {
                        // This is were we haven't gotten at least 1 space beyond the first token
                        return null;
                    }

                    if (previousLocator.isCursorFreeFromToken()) {
                        return previousLocator.getToken();
                    }

                    return lastFreeLocator.getToken();
                }

                previousLocator = new TokenCursorLocator(nextLocator);
                if (previousLocator.isCursorFreeFromToken()) {
                    lastFreeLocator = new TokenCursorLocator(previousLocator);
                }

                if (isLastToken) {
                    return previousLocator.getToken();
                }
            }
        }

        return null;
    }

    static class TokenCursorLocator {
        Token token;
        int tokenIndex;
        int line;
        int column;

        TokenCursorLocator(Token token, int index, int line, int column) {
            this.token = token;
            this.tokenIndex = index;
            this.line = line;
            this.column = column;
        }

        TokenCursorLocator(TokenCursorLocator locator) {
            super();
            this.token = locator.getToken();
            this.tokenIndex = locator.getTokenIndex();
            this.line = locator.line;
            this.column = locator.column;
        }

        public boolean isCursorInside() {
            return line >= token.beginLine && line <= token.endLine && column >= token.beginColumn
                    && column <= token.endColumn;
        }

        public boolean isCursorBefore() {
            return line < token.beginLine || (line == token.beginLine && column < token.beginColumn);
        }

        public boolean isCursorAfter() {
            return line > token.endLine || (line == token.endLine && column > token.endColumn);
        }

        public boolean isCursorAtFirstColumnInToken() {
            return line >= token.beginLine && line <= token.endLine && column == token.beginColumn;
        }

        public boolean isCursorRightAfterLastColumnInToken() {
            return line >= token.beginLine && line <= token.endLine && column == token.endColumn + 1;
        }

        /*
         * Return if cursor is after last token and not in next token Example
         * "CREATE VIEW abcdefg" NO ^ YES ^
         *
         * Example "CREATE  VIEW abcdefg" NO ^ YES ^
         */
        public boolean isCursorFreeFromToken() {
            return line > token.endLine || isSingleChar() || (line == token.endLine && column > token.endColumn + 1);
        }

        public boolean isWithinLines() {
            return line >= token.beginLine && line <= token.endLine;
        }

        public Token getToken() {
            return this.token;
        }

        public int getTokenIndex() {
            return this.tokenIndex;
        }

        public boolean isSingleChar() {
            return token.beginLine == token.endLine && token.beginColumn == token.endColumn;
        }

        public boolean isParen() {
            return token.kind == LPAREN || token.kind == RPAREN;
        }

        @Override
        @SuppressWarnings("PMD.InsufficientStringBufferDeclaration") // false positive
        public String toString() {
            StringBuilder sb = new StringBuilder(500).append(token.image).append("\n\t CURSOR: (").append(this.line)
                    .append(", ").append(this.column).append(")").append("\n\t SPAN  : (").append(this.token.beginLine)
                    .append(", ").append(this.token.beginColumn + ")").append(" >>> (").append(this.token.endLine)
                    .append(", ").append(this.token.endColumn).append(") ").append("\n\t   isCursorBefore()        = ")
                    .append(isCursorBefore()).append("\n\t   isCursorInside()        = ").append(isCursorInside())
                    .append("\n\t   isCursorAfter()         = ").append(isCursorAfter())
                    .append("\n\t   isCursorFreeFromToken() = ").append(isCursorFreeFromToken());

            return sb.toString();
        }
    }
}
