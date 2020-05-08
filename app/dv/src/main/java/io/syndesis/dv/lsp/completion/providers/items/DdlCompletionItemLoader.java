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
package io.syndesis.dv.lsp.completion.providers.items;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.tomcat.util.json.JSONParser;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;

import com.google.common.collect.ImmutableMap;

import io.syndesis.dv.lsp.completion.providers.CompletionItemBuilder;

public class DdlCompletionItemLoader extends CompletionItemBuilder {
    private static final DdlCompletionItemLoader INSTANCE = new DdlCompletionItemLoader();
    private String functionJsonFile = "./functionCompletionItems.json";
    private String datatypesJsonFile = "./datatypesCompletionItems.json";
    private String tableElementJsonFile = "./tableElementCompletionItems.json";
    private String tableElementOptionsJsonFile = "./tableElementOptionsCompletionItems.json";
    private String tableBodyOptionsJsonFile = "./tableBodyOptionsCompletionItems.json";

    private List<CompletionItem> functionItems;
    private List<CompletionItem> datatypeItems;
    private List<CompletionItem> tableElementItems;
    private List<CompletionItem> searchableValueItems;
    private List<CompletionItem> tableElementOptionsItems;
    private List<CompletionItem> tableBodyOptionsItems;
    private List<CompletionItem> createStatementTemplateItems;
    private List<CompletionItem> queryExpressionItems;
    private List<CompletionItem> queryExpressionKeywordItems;

    private static String[] kinds = { "TEXT", "METHOD", "FUNCTION", "CONSTRUCTOR", "FIELD", "VARIABLE", "CLASS",
            "INTERFACE", "MODULE", "PROPERTY", "UNIT", "VALUE", "ENUM", "KEYWORD", "SNIPPET", "COLOR", "FILE",
            "REFERENCE", "FOLDER", "ENUMMEMBER", "CONSTANT", "STRUCT", "EVENT", "OPERATOR", "TYPEPARAMETER" };

    public static DdlCompletionItemLoader getInstance() {
        return INSTANCE;
    }

    public DdlCompletionItemLoader() {
    }

    private static Map<String, CompletionItemKind> stringToKindMap = ImmutableMap.<String, CompletionItemKind>builder()
            .put(kinds[0], CompletionItemKind.Text).put(kinds[1], CompletionItemKind.Method)
            .put(kinds[2], CompletionItemKind.Function).put(kinds[3], CompletionItemKind.Constructor)
            .put(kinds[4], CompletionItemKind.Field).put(kinds[5], CompletionItemKind.Variable)
            .put(kinds[6], CompletionItemKind.Class).put(kinds[7], CompletionItemKind.Interface)
            .put(kinds[8], CompletionItemKind.Module).put(kinds[9], CompletionItemKind.Property)
            .put(kinds[10], CompletionItemKind.Unit).put(kinds[11], CompletionItemKind.Value)
            .put(kinds[12], CompletionItemKind.Enum).put(kinds[13], CompletionItemKind.Keyword)
            .put(kinds[14], CompletionItemKind.Snippet).put(kinds[15], CompletionItemKind.Color)
            .put(kinds[16], CompletionItemKind.File).put(kinds[17], CompletionItemKind.Reference)
            .put(kinds[18], CompletionItemKind.Folder).put(kinds[19], CompletionItemKind.EnumMember)
            .put(kinds[20], CompletionItemKind.Constant).put(kinds[21], CompletionItemKind.Operator)
            .put(kinds[22], CompletionItemKind.Struct).put(kinds[23], CompletionItemKind.Event)
            .put(kinds[24], CompletionItemKind.TypeParameter).build();

    @SuppressWarnings("unchecked")
    private List<CompletionItem> loadItemsFromFile(String fileName) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();

        try {
            InputStream stream = this.getClass().getResourceAsStream(fileName);

            JSONParser parser = new JSONParser(stream);

            // A JSON object. Key value pairs are unordered. JSONObject supports
            // java.util.Map interface.
            LinkedHashMap<String, Object> jsonObject = (LinkedHashMap<String, Object>) parser.object();

            // A JSON array. JSONObject supports java.util.List interface.
            ArrayList<?> completionItemArray = (ArrayList<?>) jsonObject.get("items");

            if (completionItemArray != null) {
                for (Object item : completionItemArray) {
                    LinkedHashMap<String, Object> itemInfo = (LinkedHashMap<String, Object>) item;
                    CompletionItem newItem = new CompletionItem();
                    newItem.setLabel((String) itemInfo.get("label"));
                    newItem.setKind(stringToKindMap.get(((String) itemInfo.get("kind")).toUpperCase()));

                    String detail = (String) itemInfo.get("detail");
                    if (detail != null) {
                        newItem.setDetail(detail);
                    }

                    String documentation = (String) itemInfo.get("documentation");
                    if (documentation != null) {
                        newItem.setDocumentation(documentation);
                    }

                    newItem.setDeprecated(Boolean.parseBoolean((String) itemInfo.get("deprecated")));
                    newItem.setPreselect(Boolean.parseBoolean((String) itemInfo.get("preselect")));

                    String sortText = (String) itemInfo.get("sortText");
                    if (sortText != null) {
                        newItem.setSortText(sortText);
                    }

                    String insertText = (String) itemInfo.get("insertText");
                    if (insertText != null) {
                        insertText = insertText.replaceAll("\\n", "\n");
                        insertText = insertText.replaceAll("\\t", "\t");
                        newItem.setInsertText(insertText);
                    }

                    String insertTextFormat = (String) itemInfo.get("insertTextFormat");
                    if (insertTextFormat != null) {
                        if (newItem.getKind().equals(CompletionItemKind.Snippet)) {
                            newItem.setInsertTextFormat(InsertTextFormat.Snippet);
                        } else {
                            newItem.setInsertTextFormat(InsertTextFormat.PlainText);
                        }
                    }
                    // TODO: Implement quick fixes
//                  newItem.setTextEdit((TextEdit)itemInfo.get("textEdit"));
//                  newItem.setAdditionalTextEdits((List<TextEdit>)itemInfo.get("additionalTextEdits"));
//                  newItem.setCommitCharacters((List<String>)itemInfo.get("commitCharacters"));

                    String category = (String) itemInfo.get("category");
                    if (category != null && !category.isEmpty()) {
                        addItemByCategory(category, newItem);
                    } else {
                        items.add(newItem);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return items;
    }

    private void addItemByCategory(String category, CompletionItem item) {
        if (category.equalsIgnoreCase("SEARCHABLE_VALUE")) {
            if (searchableValueItems == null) {
                searchableValueItems = new ArrayList<CompletionItem>();
            }
            if (item.getLabel().equalsIgnoreCase("SEARCHABLE_VALUE")) {
                item.setLabel("SEARCHABLE");
            }
            searchableValueItems.add(item);
        }
    }

    public List<CompletionItem> getFunctionCompletionItems() {
        if (functionItems == null) {
            functionItems = loadItemsFromFile(functionJsonFile);
        }
        return functionItems;
    }

    public List<CompletionItem> getTableElementCompletionItems() {
        if (tableElementItems == null) {
            tableElementItems = loadItemsFromFile(tableElementJsonFile);
        }
        return tableElementItems;
    }

    public List<CompletionItem> getSearchableValuesCompletionItems() {
        loadTableElementOptionsItems();
        return searchableValueItems;
    }

    public List<CompletionItem> getTableElementOptionsCompletionItems() {
        loadTableElementOptionsItems();
        return tableElementOptionsItems;
    }

    private void loadTableElementOptionsItems() {
        if (tableElementOptionsItems == null) {
            tableElementOptionsItems = loadItemsFromFile(tableElementOptionsJsonFile);
        }
    }

    public List<CompletionItem> getDatatypesCompletionItems() {
        if (datatypeItems == null) {
            datatypeItems = loadItemsFromFile(datatypesJsonFile);
        }
        return datatypeItems;
    }

    public List<CompletionItem> getTableBodyOptionsCompletionItems() {
        if (tableBodyOptionsItems == null) {
            tableBodyOptionsItems = loadItemsFromFile(tableBodyOptionsJsonFile);
        }
        return tableBodyOptionsItems;
    }

    public List<CompletionItem> getCreateStatementTemplateCompletionItems() {
        if (createStatementTemplateItems == null) {
            createStatementTemplateItems = new ArrayList<CompletionItem>();
            createStatementTemplateItems.add(getCreateViewCompletionItem(1));
            createStatementTemplateItems.add(getCreateViewInnerJoinCompletionItem(2));
            createStatementTemplateItems.add(getCreateViewJoinCompletionItem(3));
        }
        return createStatementTemplateItems;
    }

    public List<CompletionItem> getQueryExpressionKeywordItems() {
        if (queryExpressionKeywordItems == null) {
            queryExpressionKeywordItems = new ArrayList<CompletionItem>();
            queryExpressionKeywordItems = generateCompletionItems(getKeywordLabels(NON_DATATYPE_KEYWORDS, true));
        }
        return queryExpressionKeywordItems;
    }

    public List<CompletionItem> getQueryExpressionItems() {
        if (queryExpressionItems == null) {
            queryExpressionItems = new ArrayList<CompletionItem>();
            queryExpressionItems.addAll(getDatatypesCompletionItems());
            queryExpressionItems.addAll(getQueryExpressionKeywordItems());
        }
        return queryExpressionItems;
    }

    public CompletionItem getCreateViewCompletionItem(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("CREATE VIEW...");
        ci.setInsertText(QueryExpressionHelper.CREATE_VIEW_INSERT_TEXT);
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDetail("Simple CREATE VIEW statement");
        ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        ci.setSortText("00003");
        return ci;
    }

    public CompletionItem getCreateViewInnerJoinCompletionItem(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("CREATE VIEW with INNER JOIN");
        ci.setInsertText(QueryExpressionHelper.CREATE_VIEW_INNER_JOIN_INSERT_TEXT);
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDetail(" CREATE VIEW with inner join");
        ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        ci.setSortText("00002");
        return ci;
    }

    public CompletionItem getCreateViewJoinCompletionItem(int data) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("CREATE VIEW with JOIN");
        ci.setInsertText(QueryExpressionHelper.CREATE_VIEW_LEFT_OUTER_JOIN_INSERT_TEXT);
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDetail(" CREATE VIEW with left join");
        ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        ci.setSortText("00001");
        return ci;
    }
}
