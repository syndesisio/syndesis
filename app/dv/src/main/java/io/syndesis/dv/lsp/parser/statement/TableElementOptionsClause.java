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

public class TableElementOptionsClause extends OptionsClause {

    public TableElementOptionsClause(DdlTokenAnalyzer analyzer) {
        super(analyzer);
    }

    @Override
    protected TokenContext getTokenContext(Position position) {
        boolean isInOptions = isBetween(getFirstTknIndex(), getLastTknIndex(), position);
        if (isInOptions) {
            Token tkn = this.analyzer.getTokenFor(position);
            if (tkn.kind == ID && tkn.image.equalsIgnoreCase("SEARCHABLE")) {
                return new TokenContext(position, tkn, CONTEXT.TABLE_ELEMENT_OPTION_SEARCHABLE, this);
            }
            return new TokenContext(position, tkn, CONTEXT.TABLE_ELEMENT_OPTIONS, this);
        }
        return null;
    }

}
