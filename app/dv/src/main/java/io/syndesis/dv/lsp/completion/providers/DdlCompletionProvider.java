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
import org.eclipse.lsp4j.Position;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.completion.DdlCompletionConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;
import io.syndesis.dv.lsp.parser.statement.CreateViewStatement;
import io.syndesis.dv.lsp.parser.statement.TokenContext;

public class DdlCompletionProvider extends CompletionItemBuilder implements DdlCompletionConstants {

    private static final Logger LOGGER = LoggerFactory.getLogger(DdlCompletionProvider.class);

    private boolean doPrintToConsole = false;

    public List<CompletionItem> getCompletionItems(String statement, Position position) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();

        try {
            DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(statement);

            CreateViewStatement createStatement = new CreateViewStatement(analyzer);

            TokenContext tokenContext = createStatement.getTokenContext(position);
            Token previousToken = tokenContext.getToken();

            String[] words = null;

            if( previousToken != null ) {
                systemPrint("\nlast full token = " + previousToken.image + " At Position : " + position);
                systemPrint("\n >>> CONTEXT:    " + tokenContext.contextToString());

                // Each analysis with a cursor location should provide a TokenContext which contains a reference to the
                // statement clause or object the cursor is within.
                // Based on that context, the previous token KIND is used to identify what available
                // completion items will be returned

                switch(tokenContext.getContext()) {
                    // Context is the prefix to the statement "CREATE VIEW xxxxx" statement
                    case PREFIX: {
                        switch(previousToken.kind) {
                            case CREATE:
                                words = analyzer.getNextWordsByKind(CREATE);
                                items.addAll(generateCompletionItems(words));
                                break;
                            case VIEW:
                                // TODO:  A View should already be named for our primary use-case
                                // Not sure if we'll have any items here to return
                                break;
                            case ID:
                                items.add(generateCompletionItem(DdlCompletionConstants.getLabel(LPAREN, false), null,  null,  null));
                                break;
                            default: // TODO: THROW ERROR???
                        }
                    } break;

                    // Context is the Table body surrounded by (....) and before OPTIONS() or AS
                    case TABLE_BODY: {
                        items.addAll(new TableBodyCompletionProvider(createStatement).getCompletionItems(tokenContext));
                    } break;
                    case TABLE_ELEMENT:
                    case TABLE_ELEMENT_OPTIONS:
                    case TABLE_ELEMENT_OPTION_SEARCHABLE: {
                        items.addAll(new TableElementCompletionProvider(createStatement).getCompletionItems(tokenContext));
                    } break;
                    case TABLE_OPTIONS: {
                        items.addAll(new TableBodyCompletionProvider(createStatement).getCompletionItems(tokenContext));
                    } break;
                    // Context is the Table body surrounded by (....) and before OPTIONS() or AS
                    case QUERY_EXPRESSION: {
                        switch(previousToken.kind) {
                            case AS:
                                String[] values = {"SELECT"};
                                items.addAll(generateCompletionItems(values));
                                break;
                            default: {
                                // TODO:  SHOW REAL SCHEMA DATA HERE
                                items.addAll(getItemLoader().getFunctionCompletionItems());
                                items.addAll(getItemLoader().getQueryExpressionKeywordItems());
                            } break;
                        }
                    } break;
                    case SELECT_CLAUSE: {
                        items.addAll(getItemLoader().getFunctionCompletionItems());
                        items.addAll(getItemLoader().getQueryExpressionKeywordItems());
                    } break;
                    case FROM_CLAUSE: {
                        // TODO:  Add symbols for applicable database schema symbols here
                        items.addAll(getItemLoader().getQueryExpressionKeywordItems());
                    } break;
                    case WHERE_CLAUSE: {
                        // TODO:  Add symbols for applicable database schema symbols here
                        items.addAll(getItemLoader().getQueryExpressionKeywordItems());
                    } break;
                    case NONE_FOUND:
                    default: // RETURN ALL KEYWORDS
                }
            } else {
                Token targetTkn = analyzer.getTokenFor(position);
                if( targetTkn == null || createStatement.getTokenIndex(targetTkn) < 3) {
                    items.addAll(getItemLoader().getCreateStatementTemplateCompletionItems());
                }
            }

            if( doPrintToConsole ) {
                analyzer.logReport();
            }
        } catch (Exception e) {
            LOGGER.error("\n TeiidDdlCompletionProvider.getCompletionItems() ERROR parsing DDL >> " + e.getMessage() + "\n", e);
        }
        systemPrint("\n CompletionItems = " + items.size() + "\n");

        return items;
    }

    private void systemPrint(String str) {
        if( doPrintToConsole ) {
            System.out.print(str);
        }
    }

    public void printExceptions(List<DdlAnalyzerException> exceptions) {
        for( DdlAnalyzerException nextEx: exceptions ) {
            systemPrint(nextEx.getMessage());
        }
    }
}
