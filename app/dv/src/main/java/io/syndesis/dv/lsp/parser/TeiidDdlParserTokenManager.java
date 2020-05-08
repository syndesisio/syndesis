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

import java.io.IOException;

import org.teiid.query.parser.JavaCharStream;
import org.teiid.query.parser.SQLParserTokenManager;
import org.teiid.query.parser.Token;
import org.teiid.query.parser.TokenMgrError;

public class TeiidDdlParserTokenManager extends SQLParserTokenManager {

    static final int INVALID_TOKEN = -1;
    int tokenCount = 0;
    Token head;

    public TeiidDdlParserTokenManager(JavaCharStream stream) {
        super(stream);
    }

    void reinit() {
        tokenCount = 0;
        head = null;
    }

    @Override
    public Token getNextToken() {
        try {
            Token t = super.getNextToken();
            //if we're in the default lex state, keep track of a history of tokens
            //this logic is not perfect as deep lookaheads can ruin the positioning
            if (tokenCount == 0) {
                head = t;
            }
            tokenCount++;
            if (tokenCount > 10 && head.next != null) {
                head = head.next;
            }
            return t;
        } catch (TokenMgrError err) {
            Token t = new Token();
            t.kind = INVALID_TOKEN;
            t.beginColumn = this.input_stream.getBeginColumn();
            t.beginLine = this.input_stream.getBeginLine();
            t.endColumn = t.beginColumn;
            t.endLine = t.beginLine;
            t.image = this.input_stream.GetImage().substring(0, 1);
            try {
                //mark the char a consumed
                this.input_stream.readChar();
            } catch (IOException e) {
            }
            return t;
        }
    }
}
