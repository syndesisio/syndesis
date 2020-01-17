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
import org.eclipse.lsp4j.Position;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.completion.DdlCompletionConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;
import io.syndesis.dv.lsp.parser.DdlTokenAnalyzer;
import io.syndesis.dv.lsp.parser.statement.CreateViewStatement;
import io.syndesis.dv.lsp.parser.statement.TokenContext;

public class DdlCompletionProvider extends CompletionItemBuilder implements DdlCompletionConstants {
    
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
                                words = analyzer.getNextWordsByKind(VIEW);;
                                items = SampleTableCompletionProvider.getCompletionItems();
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
                                items = generateCompletionItems(values);
                                break;
                            default: {
                                // TODO:  SHOW REAL SCHEMA DATA HERE
                                items.addAll(getItemLoader().getFunctionCompletionItems());
                            } break;
                        }
                    } break;
                    case SELECT_CLAUSE: {
                        items = getItemLoader().getFunctionCompletionItems();
                    } break;
                    case FROM_CLAUSE: {
                        items.addAll(SampleTableCompletionProvider.getCompletionItems());
                    } break;
                    case WHERE_CLAUSE: {
                        // TODO: 
                    } break;
                    case NONE_FOUND:
                    default: // RETURN ALL KEYWORDS
                }
            } else {
                words = analyzer.getNextWordsByKind(EMPTY_DDL);
                systemPrint("\n >>> Token = NULL found " + words.length + " words\n");
                CompletionItem createItem = 
                        createKeywordItem(DdlCompletionConstants.getLabel(CREATE, true), null, null);
                createItem.setPreselect(true);
                items.add(createItem);        
                items.add(getCreateCreateViewCompletionItem(1));
                items.add(getCreateViewCompletionItem(2));            
            }
            
            if( doPrintToConsole ) {
                analyzer.logReport();
            }
        } catch (Exception e) {
            System.out.print("\n TeiidDdlCompletionProvider.getCompletionItems() ERROR parsing DDL >> " + e.getMessage() + "\n");
            e.printStackTrace();
        }
        systemPrint("\n CompletionItems = " + items.size() + "\n");

        return items;
    }

    private void systemPrint(String str) {
        if( doPrintToConsole ) System.out.print(str);
    }

    
    public void printExceptions(List<DdlAnalyzerException> exceptions) {
        for( DdlAnalyzerException nextEx: exceptions ) {
            systemPrint(nextEx.getMessage());
        }
    }

    public CompletionItem getCreateCreateViewCompletionItem(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("CREATE VIEW");
        ci.setInsertText(
                "CREATE VIEW ");
        ci.setKind(CompletionItemKind.Keyword);
        ci.setData(data);
        ci.setPreselect(true);
        return ci;
    }

    public CompletionItem getCreateViewCompletionItem(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("CREATE VIEW ...");
        ci.setInsertText(
                "CREATE VIEW ${1:view_name} (\n) AS SELECT * FROM ${2:table_name};");
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDetail(" Create View statement including ....");
        ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        return ci;
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

    /**
     * 
     * @param label
     * @return    String[] array >>>>  
        String[0] label;
        String[1] detail;
        String[2] documentation;
        String[3] insertText;
        String[4] insertTextFormat;
     */
    public String[] getItemData(String label) {
        String[] result = KEYWORDS_ITEM_DATA.get(label.toUpperCase());

        if( result == null ) {
            result = DATATYPES_ITEM_DATA.get(label);
        }

        return result;
    }
}
