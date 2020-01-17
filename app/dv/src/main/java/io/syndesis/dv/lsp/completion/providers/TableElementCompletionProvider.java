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
package io.syndesis.dv.lsp.completion.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.CompletionItem;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.completion.DdlCompletionConstants;
import io.syndesis.dv.lsp.parser.statement.CreateViewStatement;
import io.syndesis.dv.lsp.parser.statement.TokenContext;

/**
 * Provides completion items for a table element based on context of the targeted token
 * 
 */
public class TableElementCompletionProvider extends CompletionItemBuilder implements DdlCompletionConstants {
    CreateViewStatement statement;

    public TableElementCompletionProvider(CreateViewStatement statement) {
        super();
        this.statement = statement;
    }

    public List<CompletionItem> getCompletionItems(TokenContext context) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();

        Token targetToken = context.getToken();

        /*
         * AUTO_INCREMENT, DEFAULT_KEYWORD, NOT, NULL, PRIMARY, KEY, INDEX, UNIQUE
         */
        switch (context.getContext()) {
            case TABLE_ELEMENT_OPTIONS: {
                items = getItemLoader().getTableElementOptionsCompletionItems();
            } break;
            
            case TABLE_ELEMENT_OPTION_SEARCHABLE: {
                items = getItemLoader().getSearchableValuesCompletionItems();
            } break;

            default: {
                switch (targetToken.kind) {
                    case NOT: {
                        String[] notWords = { "NULL" };
                        items.addAll(generateCompletionItems(notWords));
                    }
                        break;
                    case NULL: {
                        String[] nullWords = { getLabel(AUTO_INCREMENT), getLabel(DEFAULT_KEYWORD) };
                        items.addAll(generateCompletionItems(nullWords));
                    }
                        break;
                    case STRINGVAL:
                    case ID: {
                        items.addAll(getItemLoader().getDatatypesCompletionItems());
                    }
                        break;
                    default: {
                        return getItemLoader().getTableElementCompletionItems();
                    }
                }
            }
        }

        return items;
    }

    public boolean isDatatype(Token token, Token[] datatypeTokens) {
        for (int dType : DATATYPES) {
            if (token.kind == dType) {
                return true;
            }
        }

        // Check if token index is in the tableElement.getDatatypeTokens() list
        int tknIndex = statement.getTokenIndex(token);
        int firstIndex = statement.getTokenIndex(datatypeTokens[0]);
        int lastIndex = statement.getTokenIndex(datatypeTokens[datatypeTokens.length - 1]);
        return tknIndex >= firstIndex && tknIndex <= lastIndex;
    }

    public String getLabel(int keywordId) {
        String tokenImageStr = tokenImage[keywordId].toUpperCase();
        return tokenImageStr.substring(1, tokenImageStr.length()-1);
    }
}
