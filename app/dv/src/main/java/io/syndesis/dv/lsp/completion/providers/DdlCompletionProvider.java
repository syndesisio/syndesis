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
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.TeiidDdlWorkspaceService;
import io.syndesis.dv.lsp.completion.DdlCompletionConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;
import io.syndesis.dv.lsp.parser.statement.CreateViewStatement;
import io.syndesis.dv.lsp.parser.statement.TokenContext;
import io.syndesis.dv.server.endpoint.MetadataService;

public class DdlCompletionProvider extends CompletionItemBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(DdlCompletionProvider.class);

    private static final boolean DO_PRINT_TO_CONSOLE = false;
    private final TeiidDdlWorkspaceService workspaceService;
    private final MetadataService metadataService;

    public DdlCompletionProvider(MetadataService metadataService, TeiidDdlWorkspaceService workspaceService) {
        super();
        this.workspaceService = workspaceService;
        this.metadataService = metadataService;
    }

    public DdlCompletionProvider() {
        this.workspaceService = null;
        this.metadataService = null;
    }

    @SuppressWarnings("PMD.CyclomaticComplexity")
    public List<CompletionItem> getCompletionItems(String statement, Position position) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();

        try {
            DdlTokenAnalyzer analyzer = new DdlTokenAnalyzer(statement);
            CreateViewStatement createStatement = new CreateViewStatement(analyzer);
            TokenContext tokenContext = createStatement.getTokenContext(position);
            systemPrint("\n DdlCompletionProvider.getCompletionItems() CONTEXT == " + tokenContext.contextToString());
            if (workspaceService != null) {
                tokenContext.setVirtualizationId(workspaceService.getCurrentVirtualizationName());
            }

            MetadataItemProvider metadataItemProvider = new MetadataItemProvider(createStatement, this.metadataService,
                    this.workspaceService);

            Token previousToken = tokenContext.getToken();

            if (previousToken != null) {
                systemPrint("\nlast full token = " + previousToken.image + " At Position : [" + position.getLine()
                        + ", " + position.getCharacter() + "]");
                systemPrint("\n >>> CONTEXT:    " + tokenContext.contextToString());

                // Each analysis with a cursor location should provide a TokenContext which contains a reference to the
                // statement clause or object the cursor is within.
                // Based on that context, the previous token KIND is used to identify what available
                // completion items will be returned

                switch (tokenContext.getContext()) {
                    // Context is the prefix to the statement "CREATE VIEW xxxxx" statement
                    case PREFIX: {
                        handlePrefixItems(tokenContext, items, analyzer);
                    }
                        break;

                    // Context is the Table body surrounded by (....) and before OPTIONS() or AS
                    case TABLE_BODY: {
                        items.addAll(
                                new TableBodyCompletionProvider().getCompletionItems(tokenContext));
                    }
                        break;
                    case TABLE_ELEMENT:
                    case TABLE_ELEMENT_OPTIONS:
                    case TABLE_ELEMENT_OPTION_SEARCHABLE: {
                        items.addAll(new TableElementCompletionProvider(createStatement).getCompletionItems(tokenContext));
                    }
                        break;
                    case TABLE_OPTIONS: {
                        items.addAll(
                                new TableBodyCompletionProvider().getCompletionItems(tokenContext));
                    }
                        break;
                    // Context is the Table body surrounded by (....) and before OPTIONS() or AS
                    case WITH_CLAUSE:
                    case WITH_LIST_ELEMENT:
                    case QUERY_EXPRESSION:
                    case SELECT_CLAUSE:
                    case SELECT_COLUMN:
                    case TABLE_SYMBOL:
                    case TABLE_SYMBOL_AS:
                    case TABLE_SYMBOL_ID:
                    case TABLE_NAME:
                    case TABLE_ALIAS:
                    case FUNCTION:
                    case FROM_CLAUSE:
                    case FROM_CLAUSE_ALIAS:
                    case FROM_CLAUSE_AS:
                    case FROM_CLAUSE_AS_OR_WHERE:
                    case FROM_CLAUSE_ID:
                    case FROM_CLAUSE_START:
                    case WHERE_CLAUSE:
                    case WHERE_CLAUSE_START:
                    case WHERE_CLAUSE_TABLE_ALIAS: {
                        items.addAll(
                                new QueryExpressionItemProvider(metadataItemProvider).getCompletionItems(tokenContext));
                    }
                    break;
                case NONE_FOUND:
                default: // RETURN ALL KEYWORDS
                }
            } else {
                Token targetTkn = analyzer.getTokenFor(position);
                if (targetTkn == null || createStatement.getTokenIndex(targetTkn) < 3) {
                    items.addAll(getItemLoader().getCreateStatementTemplateCompletionItems());
                }
            }

            if (DO_PRINT_TO_CONSOLE) {
                analyzer.logReport();
            }
        } catch (Exception e) {
            LOGGER.error(
                    "\n TeiidDdlCompletionProvider.getCompletionItems() ERROR parsing DDL >> " + e.getMessage() + "\n",
                    e);
        }
        systemPrint("\n CompletionItems = " + items.size() + "\n");

        return items;
    }

    private void handlePrefixItems(TokenContext tokenContext, List<CompletionItem> items, DdlTokenAnalyzer analyzer) {
        switch (tokenContext.getToken().kind) {
        case SQLParserConstants.CREATE:
            String[] words = analyzer.getNextWordsByKind(SQLParserConstants.CREATE);
            items.addAll(generateCompletionItems(words));
            break;
        case SQLParserConstants.VIEW:
            // TODO: A View should already be named for our primary use-case
            // Not sure if we'll have any items here to return
            break;
        case SQLParserConstants.ID:
            items.add(generateCompletionItem(DdlCompletionConstants.getLabel(SQLParserConstants.LPAREN, false), null,
                    null, null));
            break;
        default: // TODO: THROW ERROR???
        }
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private static void systemPrint(String str) {
        if (DO_PRINT_TO_CONSOLE) {
            System.out.print(str);
        }
    }

    public void printExceptions(List<DdlAnalyzerException> exceptions) {
        for (DdlAnalyzerException nextEx : exceptions) {
            systemPrint(nextEx.getMessage());
        }
    }
}
