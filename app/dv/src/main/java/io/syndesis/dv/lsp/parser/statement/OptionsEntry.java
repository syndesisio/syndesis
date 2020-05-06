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

import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;

public class OptionsEntry extends AbstractStatementObject {
    Token idToken;
    Token valueToken;

    public OptionsEntry(DdlTokenAnalyzer analyzer) {
        super(analyzer);
    }

    public Token getIdToken() {
        return idToken;
    }

    public void setIdToken(Token idToken) {
        this.idToken = idToken;
    }

    public Token getValueToken() {
        return valueToken;
    }

    public void setValueToken(Token valueToken) {
        this.valueToken = valueToken;
    }

    @Override
    protected void parseAndValidate() {
        // TODO Auto-generated method stub
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        // TODO Auto-generated method stub
        return null;
    }
}
