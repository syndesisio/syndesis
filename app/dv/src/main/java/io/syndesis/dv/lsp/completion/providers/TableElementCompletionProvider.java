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
import java.util.Locale;

import org.eclipse.lsp4j.CompletionItem;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.statement.CreateViewStatement;
import io.syndesis.dv.lsp.parser.statement.TableElement;
import io.syndesis.dv.lsp.parser.statement.TokenContext;

/**
 * Provides completion items for a table element based on context of the
 * targeted token
 *
 */
public class TableElementCompletionProvider extends CompletionItemBuilder {
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
            }
                break;

            case TABLE_ELEMENT_OPTION_SEARCHABLE: {
                items = getItemLoader().getSearchableValuesCompletionItems();
            }
                break;

            default: {
                switch (targetToken.kind) {
                    case SQLParserConstants.NOT: {
                        String[] notWords = { "NULL" };
                        items.addAll(generateCompletionItems(notWords));
                    }
                        break;
                    case SQLParserConstants.NULL: {
                        items.addAll(getRemainingItems(targetToken));
                    }
                        break;
                    case SQLParserConstants.STRINGVAL:
                    case SQLParserConstants.ID: {
                        items.addAll(setItemsSortText(getItemLoader().getDatatypesCompletionItems(), "00001"));
                        items.addAll(getRemainingItems(targetToken));
                    }
                        break;
                    default: {
                        items.addAll(getRemainingItems(targetToken));
                        return items;
                    }
                }
            }
        }

        return items;
    }

    public boolean isDatatype(Token token, Token... datatypeTokens) {
        if (DdlAnalyzerConstants.DATATYPES.contains(token.kind)) {
            return true;
        }

        // Check if token index is in the tableElement.getDatatypeTokens() list
        int tknIndex = statement.getTokenIndex(token);
        int firstIndex = statement.getTokenIndex(datatypeTokens[0]);
        int lastIndex = statement.getTokenIndex(datatypeTokens[datatypeTokens.length - 1]);
        return tknIndex >= firstIndex && tknIndex <= lastIndex;
    }

    public String getLabel(int keywordId) {
        String tokenImageStr = SQLParserConstants.tokenImage[keywordId].toUpperCase(Locale.US);
        return tokenImageStr.substring(1, tokenImageStr.length() - 1);
    }

    private TableElement getTableElement(Token token) {
        if (statement.getTableBody() != null && statement.getTableBody().getTableElements() != null) {
            for (TableElement element : statement.getTableBody().getTableElements()) {
                if (element.isTokenInObject(token)) {
                    return element;
                }
            }
        }
        return null;
    }

    @SuppressWarnings({ "PMD.CyclomaticComplexity", "PMD.NPathComplexity" })
    private List<CompletionItem> getRemainingItems(Token targetToken) {
        List<CompletionItem> remainingItems = new ArrayList<CompletionItem>();
        List<CompletionItem> allItems = getItemLoader().getTableElementCompletionItems();
        TableElement element = getTableElement(targetToken);

        if (element != null) {
            for (CompletionItem item : allItems) {
                if (item.getLabel().equalsIgnoreCase(getLabel(SQLParserConstants.NOT))
                        && element.getNotNullTokens() == null) {
                    remainingItems.add(item);
                } else if (item.getLabel().equalsIgnoreCase(getLabel(SQLParserConstants.NULL))
                        && element.getNotNullTokens() == null) {
                    remainingItems.add(item);
                } else if (item.getLabel().equalsIgnoreCase(getLabel(SQLParserConstants.PRIMARY))
                        && !statement.getTableBody().hasPrimaryKey()) {
                    remainingItems.add(item);
                } else if (item.getLabel().equalsIgnoreCase(getLabel(SQLParserConstants.KEY))
                        && !statement.getTableBody().hasPrimaryKey()) {
                    remainingItems.add(item);
                } else if (item.getLabel().equalsIgnoreCase("PRIMARY KEY") && !statement.getTableBody().hasPrimaryKey()) {
                    remainingItems.add(item);
                } else if (item.getLabel().equalsIgnoreCase(getLabel(SQLParserConstants.AUTO_INCREMENT))
                        && element.getAutoIncrementToken() == null) {
                    remainingItems.add(item);
                } else if (item.getLabel().equalsIgnoreCase(getLabel(SQLParserConstants.DEFAULT))
                        && element.getDatatypeTokens() == null) {
                    remainingItems.add(item);
                } else if (item.getLabel().equalsIgnoreCase(getLabel(SQLParserConstants.INDEX))
                        && element.getIndexToken() == null) {
                    remainingItems.add(item);
                } else if (item.getLabel().equalsIgnoreCase(getLabel(SQLParserConstants.UNIQUE))
                        && element.getUniqueToken() == null) {
                    remainingItems.add(item);
                } else if (item.getLabel().equalsIgnoreCase(getLabel(SQLParserConstants.OPTIONS))
                        && element.getOptionClause() == null) {
                    remainingItems.add(item);
                } else if (item.getLabel().equalsIgnoreCase(getLabel(SQLParserConstants.FOREIGN))) {
                    remainingItems.add(item);
                }
            }
        }
        return remainingItems;
    }
}
