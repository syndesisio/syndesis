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
import org.teiid.query.parser.SQLParserConstants;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.Context;
import io.syndesis.dv.lsp.parser.statement.TokenContext;

public class QueryExpressionItemProvider extends CompletionItemBuilder {

    private final MetadataItemProvider metadataItemProvider;

    public QueryExpressionItemProvider(MetadataItemProvider metadataItemProvider) {
        super();
        this.metadataItemProvider = metadataItemProvider;
    }

    @SuppressWarnings("PMD.MissingBreakInSwitch") // TODO refactor
    public List<CompletionItem> getCompletionItems(TokenContext tokenContext) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        switch (tokenContext.getContext()) {
            case FUNCTION:
            case SELECT_COLUMN:
            case SELECT_CLAUSE:
                items.addAll(getSelectClauseItems(tokenContext));
                break;
            case TABLE_ALIAS:
                items.addAll(getTableAliasItems(tokenContext));
                break;
            case TABLE_SYMBOL:
            case TABLE_SYMBOL_ID:
            case FROM_CLAUSE:
            case FROM_CLAUSE_START:
            case FROM_CLAUSE_AS_OR_WHERE:
            case FROM_CLAUSE_ALIAS:
            case FROM_CLAUSE_AS:
            case FROM_CLAUSE_ID:
                items.addAll(getFromClauseItems(tokenContext));
                break;
            case WHERE_CLAUSE:
            case WHERE_CLAUSE_START:
            case WHERE_CLAUSE_TABLE_ALIAS:
                items.addAll(getWhereClauseItems(tokenContext));
                break;
            case QUERY_EXPRESSION:
                items.addAll(getQueryExpressionItems(tokenContext));
                break;
            case NONE_FOUND:
            default:
                items.addAll(getItemLoader().getQueryExpressionKeywordItems());
                break;
        }

        return items;
    }

    private List<CompletionItem> getQueryExpressionItems(TokenContext tokenContext) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        if (tokenContext.getToken().kind == SQLParserConstants.AS) {
            String[] values = { "SELECT" };
            items.addAll(generateCompletionItems(values));
        } else {
            items.addAll(metadataItemProvider.getCompletionItems(tokenContext));
            items.addAll(getItemLoader().getFunctionCompletionItems());
            items.addAll(getItemLoader().getQueryExpressionKeywordItems());
        }
        return items;
    }

    private List<CompletionItem> getSelectClauseItems(TokenContext tokenContext) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        items.addAll(metadataItemProvider.getCompletionItems(tokenContext));

        items.addAll(getItemLoader().getFunctionCompletionItems());
        items.addAll(getItemLoader().getQueryExpressionKeywordItems());
        return items;
    }

    private List<CompletionItem> getTableAliasItems(TokenContext tokenContext) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        items.addAll(metadataItemProvider.getCompletionItems(tokenContext));
        return items;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity") // TODO refactor
    private List<CompletionItem> getFromClauseItems(TokenContext tokenContext) {
        List<CompletionItem> items = metadataItemProvider.getCompletionItems(tokenContext);

        switch (tokenContext.getContext()) {
        case FROM_CLAUSE_ID:
        case FROM_CLAUSE_AS_OR_WHERE: {
            CompletionItem asItem = getItemLoader().cloneQueryExpressionKeywordItem("AS");
            asItem.setSortText("800");
            items.add(asItem);
            CompletionItem whereItem = getItemLoader().cloneQueryExpressionKeywordItem("WHERE");
            whereItem.setSortText("900");
            items.add(whereItem);
        } break;
        case FROM_CLAUSE_START: {
            CompletionItem fromItem = getItemLoader().cloneQueryExpressionKeywordItem("FROM");
            fromItem.setSortText("900");
            items.add(fromItem);
        } break;
        case FROM_CLAUSE_ALIAS: {
            CompletionItem fromItem = getItemLoader().cloneQueryExpressionKeywordItem("WHERE");
            fromItem.setSortText("900");
            items.add(fromItem);
        } break;
        case WITH_CLAUSE:
        case WITH_LIST_ELEMENT:
        case FROM_CLAUSE_AS:
        case FROM_CLAUSE:
        case COLUMN_NAME:
        case FUNCTION:
        case NONE_FOUND:
        case PREFIX:
        case QUERY_EXPRESSION:
        case SELECT_CLAUSE:
        case SELECT_CLAUSE_START:
        case SELECT_COLUMN:
        case TABLE_ALIAS:
        case TABLE_BODY:
        case TABLE_ELEMENT:
        case TABLE_ELEMENT_OPTIONS:
        case TABLE_ELEMENT_OPTION_SEARCHABLE:
        case TABLE_NAME:
        case TABLE_OPTIONS:
        case TABLE_SYMBOL:
        case TABLE_SYMBOL_AS:
        case TABLE_SYMBOL_ID:
        case WHERE_CLAUSE:
        case WHERE_CLAUSE_START:
        case WHERE_CLAUSE_TABLE_ALIAS:
        default:
            break;

        }

        return items;
    }

    private List<CompletionItem> getWhereClauseItems(TokenContext tokenContext) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();
        items.addAll(metadataItemProvider.getCompletionItems(tokenContext));
        if (tokenContext.getContext() != Context.WHERE_CLAUSE_TABLE_ALIAS) {
            items.addAll(getItemLoader().getQueryExpressionKeywordItems());
        } else if (tokenContext.getContext() == Context.WHERE_CLAUSE_START) {
            CompletionItem whereItem = getItemLoader().cloneQueryExpressionKeywordItem("WHERE");
            whereItem.setSortText("900");
            items.add(whereItem);
        }
        return items;
    }
}
