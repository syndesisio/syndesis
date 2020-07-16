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

import com.google.common.base.Splitter;

import io.syndesis.dv.lsp.Messages;
import io.syndesis.dv.lsp.codeactions.QuickFixFactory;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

/*
 * Select column is a column name defined as
 * 1) a constant (string, integer, etc...)
 * 2) a columnName
 * 3) a tableName.columnName
 * 4) an aliased name of the form: tableName.columnName AS foobar
 * 5) an aliased function:  concat(name1, name2) AS foobar
 *    - where a function can be a complex function
 *    - EXAMPLE: concat(name1, concat(' ', name2)) AS foobar
 */
@SuppressWarnings({"PMD.GodClass"})
public class SelectColumn extends AbstractStatementObject {
    private Token nameToken;
    private Token aliasNameToken; // OPTIONAL
    private Token literalValueToken; // OPTIONAL

    private final SelectClause selectClause;
    private boolean incomplete;

    public SelectColumn(DdlTokenAnalyzer analyzer, SelectClause selectClause) {
        super(analyzer);
        this.selectClause = selectClause;
    }

    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.NPathComplexity", "PMD.ExcessiveMethodLength"}) // TODO refactor
    protected void parseAndValidate() {
        boolean requiresAlias = false;

        // LOOKING FOR : column1 AS c1
        if( hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.STRINGVAL, SQLParserConstants.UNSIGNEDINTEGER)) {
            incrementIndex();
            this.literalValueToken = getCurrentToken();
            setFirstTknIndex(currentIndex());
            setLastTknIndex(currentIndex());
        } else if ( hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.ID)) {
            incrementIndex();
            this.nameToken = getCurrentToken();
            setFirstTknIndex(currentIndex());
            setLastTknIndex(currentIndex());
        }

         // CHECK IF unfinished aliased ID (i.e. t2. , as opposed to t2.xyz)
        if ( hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.PERIOD)) {
            incrementIndex();
            setLastTknIndex(currentIndex());
            DdlAnalyzerException exception = this.analyzer.addException(
                    this.nameToken,
                    this.nameToken,
                    Messages.getString(Messages.Error.INCOMPLETE_SCHEMA_REF, this.nameToken.image));
            exception.setErrorCode(
                    QuickFixFactory.DiagnosticErrorId.INCOMPLETE_SCHEMA_REF.getErrorCode());
        } else if(hasNextIndex() && isNextTokenOfKind(currentIndex(),
                SQLParserConstants.SLASH,
                SQLParserConstants.PLUS,
                SQLParserConstants.MINUS,
                SQLParserConstants.STAR
                )) {
            incrementIndex(2);
            // Now check for next token
            requiresAlias = true;
        }
        // Check for alias (AS) token
        if (hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.AS)) {
            incrementIndex();
            if (hasNextIndex()) {
                incrementIndex();
                // get the alias name and save the actual name to definition token
                if( isTokenOfKind(currentIndex(), SQLParserConstants.ID, SQLParserConstants.STRINGVAL)) {
                    Token aliasNameTkn = getCurrentToken();
                    this.nameToken = getNameToken();
                    this.aliasNameToken = aliasNameTkn;
                    setLastTknIndex(currentIndex());
                } else {
                    Token tmpTkn = getCurrentToken();
                    this.analyzer.addException(tmpTkn, tmpTkn, "Missing or invalid alias for column name [" + tmpTkn.image + "]");
                            setLastTknIndex(currentIndex());
                    }
                }

                if (hasNextIndex() && isNextTokenOfKind(currentIndex(), SQLParserConstants.COMMA, SQLParserConstants.SEMICOLON)) {
                    incrementIndex();
                }
                setLastTknIndex(currentIndex());
            } else {
                // If alias is required, then add exception that AS was expected
                if( requiresAlias ) {
                    Token tmpTkn = getCurrentToken();
                    this.analyzer.addException(getFirstToken(), tmpTkn, "Alias required for column expression");
                }
                if (hasNextIndex() && isNextTokenOfKind(currentIndex(),
                        SQLParserConstants.COMMA,
                        SQLParserConstants.SEMICOLON)) {
                    incrementIndex();
                    setLastTknIndex(currentIndex());
                } else {
                    // Have not found valid next/expected token
                    // if != FROM or != COMMA or != ID/STRINGVAL/UNSIGNEDINTEGER
                    if (hasNextIndex() && !isNextTokenOfKind(currentIndex(),
                            SQLParserConstants.FROM,
                            SQLParserConstants.ID,
                            SQLParserConstants.STRINGVAL,
                            SQLParserConstants.UNSIGNEDINTEGER,
                            SQLParserConstants.RPAREN)) {
                            Token nextTkn = getToken(nextIndex());
                            this.analyzer.addException(
                                    nextTkn,
                                    nextTkn,
                                    Messages.getString(Messages.Error.INVALID_TOKEN, nextTkn.image))
                            .setErrorCode(QuickFixFactory.DiagnosticErrorId.INVALID_TOKEN.getErrorCode());
                            incrementIndex();
                            if (this.nameToken != null && hasNextIndex() && !isNextTokenOfKind(currentIndex(),
                                    SQLParserConstants.FROM)) {
                                this.analyzer.addException(
                                        this.nameToken,
                                        this.nameToken,
                                        Messages.getString(Messages.Error.INVALID_COLUMN_MISSING_COMMA, this.nameToken.image))
                                .setErrorCode(QuickFixFactory.DiagnosticErrorId.INVALID_COLUMN_MISSING_COMMA.getErrorCode());
                            }
                    } else if (hasNextIndex() && !isNextTokenOfKind(currentIndex(),
                            SQLParserConstants.FROM, SQLParserConstants.RPAREN)) {
                        this.analyzer.addException(
                                this.nameToken,
                                this.nameToken,
                                Messages.getString(Messages.Error.INVALID_COLUMN_MISSING_COMMA, this.nameToken.image))
                        .setErrorCode(QuickFixFactory.DiagnosticErrorId.INVALID_COLUMN_MISSING_COMMA.getErrorCode());
                    }
                }

            }
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInObject = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInObject) {
            Token tkn = this.analyzer.getTokenFor(position);

            // Check for last token == COMMA since comma is part of the table element token
            if (tkn.kind == SQLParserConstants.COMMA
                    && getTokens().get(getLastTknIndex()).kind == SQLParserConstants.COMMA
                    && getTokenIndex(tkn) == getLastTknIndex()) {
                // return the From Clause context
                return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.SELECT_CLAUSE, this);
            } else if (tkn.kind == SQLParserConstants.PERIOD) {
                Token aliasNamePrefixToken = getTokens().get(getTokenIndex(tkn) - 1);
                // check previous token and look for valid table alias
                for (TableSymbol nextSymbol : getSelectClause().getQueryExpression().getFromClause()
                        .getTableSymbols()) {
                    if (nextSymbol.isAliased() && nextSymbol.getAlias().equalsIgnoreCase(aliasNamePrefixToken.image)) {
                        return new TokenContext(position, aliasNamePrefixToken,
                                DdlAnalyzerConstants.Context.TABLE_ALIAS, this);
                    }
                }
            }

            return new TokenContext(position, tkn, DdlAnalyzerConstants.Context.SELECT_COLUMN, this);
        }

        return null;
    }

    public String getTableName() {
        String fullNameToken = getNameToken().image;

        List<String> parts = Splitter.on('.').splitToList(fullNameToken);
        if (parts.size() == 2) {
            return parts.get(0);
        }

        return null;
    }

    public String getColumnName() {
        String fullNameToken = getNameToken().image;

        List<String> parts = Splitter.on('.').splitToList(fullNameToken);
        if (parts.size() == 2) {
            return parts.get(1);
        }

        return parts.get(0);
    }

    public Token getNameToken() {
        return nameToken;
    }

    public void setAliasNameToken(Token tkn) {
        this.aliasNameToken = tkn;
    }

    public Token getAliasNameToken() {
        return aliasNameToken;
    }

    public Token getLiteralValueToken() {
        return literalValueToken;
    }

    public SelectClause getSelectClause() {
        return selectClause;
    }

    public boolean isIncomplete() {
        return incomplete;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(75);

        for (int i=getFirstTknIndex(); i<getLastTknIndex()+1; i++) {
            append(getToken(i), sb);
            if (i < getLastTknIndex()-1) {
                sb.append(' ');
            }
        }

        return sb.toString();
    }
}
