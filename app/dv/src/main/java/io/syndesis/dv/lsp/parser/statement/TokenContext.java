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

public class TokenContext implements DdlAnalyzerConstants {
    private final Position position;
    private final Token token;
    private CONTEXT context;
    private final AbstractStatementObject targetObject;

    public TokenContext(Position position, Token token, CONTEXT context, AbstractStatementObject statementObject) {
        super();
        this.position = position;
        this.token = token;
        this.context = context;
        this.targetObject = statementObject;
    }

    public Position getPosition() {
        return position;
    }

    public Token getToken() {
        return token;
    }

    public CONTEXT getContext() {
        return context;
    }

    public CONTEXT setContext(CONTEXT context) {
        return this.context = context;
    }

    public AbstractStatementObject getTargetObject() {
        return targetObject;
    }

    public String contextToString() {
        switch(context) {
            case PREFIX: return "PREFIX";
            case TABLE_BODY: return "TABLE_BODY";
            case TABLE_OPTIONS: return "TABLE_OPTIONS";
            case TABLE_ELEMENT: return "TABLE_ELEMENT";
            case TABLE_ELEMENT_OPTIONS: return "TABLE_ELEMENT_OPTIONS";
            case QUERY_EXPRESSION: return "QUERY_EXPRESSION";
            case SELECT_CLAUSE: return "SELECT_CLAUSE";
            case FROM_CLAUSE: return "FROM_CLAUSE";
            case WHERE_CLAUSE: return "WHERE_CLAUSE";
            default:  return "NONE_FOUND";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TokenContext: ");
        if( token != null ) {
            sb.append(token.image);
        } else {
            sb.append(" NONE ");
        }
        sb.append(" at (")
            .append(position.getLine())
            .append(", ")
            .append(position.getCharacter())
            .append(")");
        sb.append(" Context: ").append(contextToString());

        return sb.toString();
    }
}
