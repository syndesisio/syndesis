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

import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;

public class TokenContext {
    private final Position position;
    private final Token token;
    private DdlAnalyzerConstants.Context context;
    private final AbstractStatementObject targetObject;
    private String virtualizationId;

    public TokenContext(Position position, Token token, DdlAnalyzerConstants.Context context,
            AbstractStatementObject statementObject) {
        this(position, token, context, statementObject, null);
    }

    public TokenContext(Position position, Token token, DdlAnalyzerConstants.Context context,
            AbstractStatementObject statementObject, String virtualizationId) {
        super();
        this.position = position;
        this.token = token;
        this.context = context;
        this.targetObject = statementObject;
        this.virtualizationId = virtualizationId;
    }

    public Position getPosition() {
        return this.position;
    }

    public Token getToken() {
        return this.token;
    }

    public DdlAnalyzerConstants.Context getContext() {
        return context;
    }

    public DdlAnalyzerConstants.Context setContext(DdlAnalyzerConstants.Context context) {
        return this.context = context;
    }

    public AbstractStatementObject getTargetObject() {
        return this.targetObject;
    }

    public String getVirtualizationId() {
        return this.virtualizationId;
    }

    public void setVirtualizationId(String id) {
        this.virtualizationId = id;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity") // TODO refactor
    public String contextToString() {
        switch (context) {
            case PREFIX:
                return "PREFIX";
            case TABLE_BODY:
                return "TABLE_BODY";
            case TABLE_OPTIONS:
                return "TABLE_OPTIONS";
            case TABLE_ELEMENT:
                return "TABLE_ELEMENT";
            case TABLE_ELEMENT_OPTIONS:
                return "TABLE_ELEMENT_OPTIONS";
            case TABLE_ELEMENT_OPTION_SEARCHABLE:
                return "TABLE_ELEMENT_OPTION_SEARCHABLE";
            case QUERY_EXPRESSION:
                return "QUERY_EXPRESSION";
            case SELECT_CLAUSE:
                return "SELECT_CLAUSE";
            case SELECT_CLAUSE_START:
                return "SELECT_CLAUSE_START";
            case SELECT_COLUMN:
                return "SELECT_COLUMN";
            case FUNCTION:
                return "FUNCTION";
            case TABLE_ALIAS:
                return "TABLE_ALIAS";
            case COLUMN_NAME:
                return "COLUMN_NAME";
            case FROM_CLAUSE:
                return "FROM_CLAUSE";
            case FROM_CLAUSE_ALIAS:
                return "FROM_CLAUSE_ALIAS";
            case FROM_CLAUSE_AS:
                return "FROM_CLAUSE_AS";
            case FROM_CLAUSE_START:
                return "FROM_CLAUSE_START";
            case FROM_CLAUSE_AS_OR_WHERE:
                return "FROM_CLAUSE_AS_OR_WHERE";
            case FROM_CLAUSE_ID:
                return "FROM_CLAUSE_ID";
            case TABLE_SYMBOL:
                return "TABLE_SYMBOL";
            case TABLE_SYMBOL_ID:
                return "TABLE_SYMBOL_ID";
            case TABLE_SYMBOL_AS:
                return "TABLE_SYMBOL_AS";
            case TABLE_NAME:
                return "TABLE_NAME";
            case WHERE_CLAUSE:
                return "WHERE_CLAUSE";
            case WHERE_CLAUSE_START:
                return "WHERE_CLAUSE_START";
            case WHERE_CLAUSE_TABLE_ALIAS:
                return "WHERE_CLAUSE_TABLE_ALIAS";
            default:
                return "NONE_FOUND";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(75);
        sb.append("TokenContext: ");
        if (token != null) {
            sb.append(token.image);
        } else {
            sb.append(" NONE ");
        }
        sb.append(" at (").append(position.getLine()).append(", ").append(position.getCharacter()).append(") Context: ")
                .append(contextToString());

        return sb.toString();
    }
}
