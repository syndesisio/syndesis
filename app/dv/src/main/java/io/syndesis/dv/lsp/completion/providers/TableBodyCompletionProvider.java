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
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.completion.DdlCompletionConstants;
import io.syndesis.dv.lsp.parser.statement.CreateViewStatement;
import io.syndesis.dv.lsp.parser.statement.TokenContext;

/**
 * Provides completion items for a table body based on context of the targeted
 * token
 *
 */
public class TableBodyCompletionProvider extends CompletionItemBuilder implements DdlCompletionConstants {
    CreateViewStatement statement;

    public TableBodyCompletionProvider(CreateViewStatement statement) {
        super();
        this.statement = statement;
    }

    public List<CompletionItem> getCompletionItems(TokenContext context) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();

        Token targetToken = context.getToken();

        switch (context.getContext()) {
            case TABLE_OPTIONS: {
                items = getItemLoader().getTableBodyOptionsCompletionItems();
            } break;

            default: {
                switch (targetToken.kind) {
                    case RPAREN: {
                        items.add(getAs(1));
                        items.add(getQueryExpressionSnippet(2));
                        String details = "OPTIONS properties are comma separated key-value pairs in the form: OPTION(...  KEY 'some value')";
                        items.add(generateCompletionItem("OPTIONS", null, details, "OPTIONS()"));
                    } break;
                    default: {
                        // 1) COLUMN DEFINITION snippet
                        items.add(getColumnCompletionItem(0));
                        // 2) PRIMARY KEY snippet
                        items.add(getPrimaryKeyCompletionItem(0));
                    }
                }
            }
        }

        return items;
    }

    public CompletionItem getColumnCompletionItem(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("column definition");
        ci.setInsertText("${1:column_name} ${2:datatype}");
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        return ci;
    }

    public CompletionItem getPrimaryKeyCompletionItem(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("PRIMARY KEY (column, ...)");
        ci.setInsertText("PRIMARY KEY (${1:column})");
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        return ci;
    }

    public CompletionItem getQueryExpressionSnippet(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("AS SELECT * FROM ...");
        ci.setInsertText(" AS SELECT * FROM ${4:table_name};");
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        return ci;
    }

    public CompletionItem getAs(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("AS");
        ci.setKind(CompletionItemKind.Keyword);
        ci.setData(data);
        ci.setPreselect(true);
        return ci;
    }

    public String getLabel(int keywordId) {
        String tokenImageStr = tokenImage[keywordId].toUpperCase();
        return tokenImageStr.substring(1, tokenImageStr.length() - 1);
    }
}
