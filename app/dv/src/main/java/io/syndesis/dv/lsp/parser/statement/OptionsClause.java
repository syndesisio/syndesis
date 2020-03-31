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

import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class OptionsClause extends AbstractStatementObject {
    private Token[] optionsTokens;
    private List<Token[]> nameValueTokenList;

    public OptionsClause(DdlTokenAnalyzer analyzer) {
        super(analyzer);
    }

    @Override
    protected void parseAndValidate() {
        nameValueTokenList = new ArrayList<Token[]>();

        // minimum 3 tokens.... OPTIONS()
        if( optionsTokens.length < 3 ) {
            // System.out.println("OPTIONS ERROR: INVALID ... should be OPTIONS() ");
            // TODO: ADD EXCEPTION
            return;
        }
        // First token has to be OPTIONS
        if( !tokenIsOfKind(getFirstTknIndex(), OPTIONS)) {
            // TODO: ADD EXCEPTION
            return;
        }
        // Second token has to be '('
        if( !tokenIsOfKind(getFirstTknIndex() + 1, LPAREN)) {
            // System.out.println("OPTIONS ERROR: left '(' missing ");
            // TODO: ADD EXCEPTION
            return;
        }
        // Last token has to be a ')'
        if( !tokenIsOfKind(getLastTknIndex(), RPAREN)) {
            // System.out.println("OPTIONS ERROR: right ')' missing ");
            // TODO: ADD EXCEPTION
            return;
        }

        // Now parse based on COMMA TOKEN
        int iTkn = 2;
        List<Token> nextOption = new ArrayList<Token>();
        for(int i=iTkn; i<optionsTokens.length-1; i++) {
            Token nextTkn = optionsTokens[i];
            if( nextTkn.kind == COMMA) {
                // We found a comma, so add nextOption (tkn array) to the nameValueTokenList
                Token[] nextNameValueArray = nextOption.toArray(new Token[0]);
                nameValueTokenList.add(nextNameValueArray);
                validateNameValueArray(nextNameValueArray);
                nextOption.clear();
            } else {
                nextOption.add(nextTkn);
                if( i == optionsTokens.length-2) {
                    Token[] nextNameValueArray = nextOption.toArray(new Token[0]);
                    nameValueTokenList.add(nextNameValueArray);
                    validateNameValueArray(nextNameValueArray);
                }
            }
        }
    }

    private void validateNameValueArray(Token[] nextNameValueArray) {
        // Should be 2 tokens
        if( nextNameValueArray.length == 1 ) {
            Token firstToken = nextNameValueArray[0];
            Token lastToken = nextNameValueArray[nextNameValueArray.length-1];
            this.analyzer.addException(
                    firstToken,
                    lastToken,
                    "OPTION missing value at " + positionToString(getBeginPosition(firstToken)));
            return;
        }
        if( nextNameValueArray[0].kind != ID ) {
            Token firstToken = nextNameValueArray[0];
            this.analyzer.addException(
                    firstToken,
                    firstToken,
                    "OPTION ID is invalid at " + positionToString(getBeginPosition(firstToken)));
            return;
        }
        if( nextNameValueArray[1].kind != STRINGVAL && nextNameValueArray[1].kind != UNSIGNEDINTEGER) {
            Token firstToken = nextNameValueArray[1];
            this.analyzer.addException(
                    firstToken,
                    firstToken,
                    "Option value is not valid at " + positionToString(getBeginPosition(firstToken)));
            return;
        }
        if( nextNameValueArray.length > 2 ) {
            Token firstExtraToken = nextNameValueArray[2];
            Token lastExtraToken = nextNameValueArray[nextNameValueArray.length-1];
            this.analyzer.addException(
                    firstExtraToken,
                    lastExtraToken,
                    "OPTIONS contains invalid tokens start at: " + positionToString(getBeginPosition(firstExtraToken)));
            return;
        }
    }

    public Token[] getOptionsTokens() {
        return optionsTokens;
    }

    private boolean tokenIsOfKind(int index, int kind) {
        return this.getAnalyzer().getTokens()[index].kind == kind;
    }

    public void setOptionsTokens(Token[] optionsTokens) {
        this.optionsTokens = optionsTokens;
        setFirstTknIndex(this.getAnalyzer().getTokenIndex(this.optionsTokens[0]));
        setLastTknIndex(this.getAnalyzer().getTokenIndex(this.optionsTokens[optionsTokens.length-1]));
        this.parseAndValidate();
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        return null;
    }
}
