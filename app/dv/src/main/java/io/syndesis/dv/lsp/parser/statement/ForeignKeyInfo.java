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

import java.util.List;

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class ForeignKeyInfo extends AbstractStatementObject {
    private final Token fkToken;
    private final TableBody tableBody;

    public ForeignKeyInfo(DdlTokenAnalyzer analyzer, Token fkToken, TableBody tableBody) {
        super(analyzer);
        this.fkToken = fkToken;
        this.tableBody = tableBody;
    }

    @Override
    protected void parseAndValidate() {
        // first should be PRIMARY
        int currentIndex = getTokenIndex(fkToken);
        setFirstTknIndex(currentIndex);

        boolean isIncomplete = false;
        currentIndex++;
        // Check for NULL and look for KEY token
        if (hasAnotherToken(getTokens(), currentIndex)) {
            Token thisToken = getTokens().get(currentIndex);
            if (thisToken.kind == SQLParserConstants.KEY) {
                if (isNextTokenOfKind(this.getTokens(), currentIndex, SQLParserConstants.LPAREN)) {
                    List<Token> bracketedTkns =
                            getBracketedTokens(getTokens(), currentIndex + 1,
                                    SQLParserConstants.LPAREN, SQLParserConstants.RPAREN);
                    if (bracketedTkns.isEmpty()) {
                        setLastTknIndex(currentIndex + 1);
                        logIncompleteForeignKeyException(fkToken, getLastToken());
                    } else {
                        // Update currentIndex
                        currentIndex = currentIndex + bracketedTkns.size();
                    }
                    // Jump ahead to end of (...)
                    if (hasAnotherToken(getTokens(), currentIndex)) {
                        currentIndex++;
                        if( currentIndex < this.tableBody.getLastTknIndex()) {
                            thisToken = getTokens().get(currentIndex);
                            if (thisToken.kind == SQLParserConstants.REFERENCES) {
                                currentIndex++;
                                Token tRefTkn = getTokens().get(currentIndex);
                                if (tRefTkn.kind != SQLParserConstants.ID && tRefTkn.kind != SQLParserConstants.STRINGVAL) {
                                    this.analyzer.addException(fkToken, tRefTkn, Messages.getString(Messages.Error.MISSING_FK_TABLE_REF));
                                    currentIndex--;
                                    isIncomplete = true;
                                }
                                if (isNextTokenOfKind(this.getTokens(), currentIndex, SQLParserConstants.LPAREN)) {
                                    bracketedTkns = getBracketedTokens(getTokens(), currentIndex + 1, SQLParserConstants.LPAREN,
                                        SQLParserConstants.RPAREN);
                                    if (bracketedTkns.isEmpty()) {
                                        setLastTknIndex(currentIndex + 1);
                                        isIncomplete = true;
                                    }else if (bracketedTkns.size() == 2) {
                                        setLastTknIndex(currentIndex + bracketedTkns.size());
                                        isIncomplete = true;
                                    } else {
                                        validateKeyColumns(bracketedTkns);
                                        currentIndex++;
                                        setLastTknIndex(currentIndex + bracketedTkns.size()-1);
                                    }
                                } else {
                                    setLastTknIndex(currentIndex);
                                    isIncomplete = true;
                                }
                            } else {
                                setLastTknIndex(this.tableBody.getLastTknIndex()-1);
                                logIncompleteForeignKeyException(
                                        fkToken,this.tableBody.getTokens().get(this.tableBody.getTokens().size()-2));
                            }
                        } else {
                            setLastTknIndex(currentIndex-1);
                            isIncomplete = true;
                        }
                    } else {
                        setLastTknIndex(currentIndex);
                        isIncomplete = true;
                    }
                } else {
                    setLastTknIndex(currentIndex);
                    isIncomplete = true;
                }
            } else {
                setLastTknIndex(currentIndex);
                isIncomplete = true;
            }
        }

        if( isIncomplete ) {
            logIncompleteForeignKeyException(fkToken, getLastToken());
        }
    }

    private void validateKeyColumns(List<Token> tokens) {
        int count = 1;
        int tknsLeft = tokens.size();
        for(Token tkn: tokens) {
            if( count != 1 && count < tokens.size()) {
                if(count % 2 == 0) { // ODD count should be COMMA
                    // Check tkn
                    if (tkn.kind != SQLParserConstants.STRINGVAL
                        && tkn.kind != SQLParserConstants.ID) {
                         this.analyzer.addException(
                                 tkn, tkn, Messages.getString(Messages.Error.INVALID_FOREIGN_KEY_COLUMN, tkn.image));
                    }
                } else if (tknsLeft > 1 && tkn.kind != SQLParserConstants.COMMA) {
                    DdlAnalyzerException exception =
                            this.analyzer.addException(
                                tkn, tkn, Messages.getString(Messages.Error.MISSING_COMMA_SEPARATOR));
                        exception.setErrorCode(QuickFixFactory.DiagnosticErrorId.MISSING_COMMA_SEPARATOR.getErrorCode());
                }
            }
            count++;
            tknsLeft--;
        }
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        // TODO Auto-generated method stub
        return null;
    }

    private void logIncompleteForeignKeyException(Token startToken, Token endToken) {
        this.analyzer.addException(startToken, endToken, Messages.getString(Messages.Error.INCOMPLETE_FOREIGN_KEY))
        .setErrorCode(Messages.Error.INCOMPLETE_FOREIGN_KEY.name());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(75);

        for (int i=getFirstTknIndex(); i<getLastTknIndex()+1; i++) {
            append(getToken(i), sb);
            if (i < getLastTknIndex()) {
                sb.append(' ');
            }
        }

        return sb.toString();
    }
}
