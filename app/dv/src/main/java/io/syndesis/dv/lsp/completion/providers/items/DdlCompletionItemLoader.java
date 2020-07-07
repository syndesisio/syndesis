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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import org.apache.tomcat.util.json.JSONParser;
import org.apache.tomcat.util.json.ParseException;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.InsertTextFormat;
import org.teiid.core.types.DataTypeManager;
import org.teiid.language.SQLConstants;
import org.teiid.query.parser.SQLParserConstants;

import com.google.common.collect.ImmutableMap;

import io.syndesis.dv.lsp.completion.providers.CompletionItemBuilder;

@SuppressWarnings("PMD.GodClass")
public final class DdlCompletionItemLoader extends CompletionItemBuilder {
    private static final DdlCompletionItemLoader INSTANCE = new DdlCompletionItemLoader();
    private static final String FUNCTION_JSON_FILE = "./functionCompletionItems.json";
    private static final String DATATYPES_JSON_FILE = "./datatypesCompletionItems.json";
    private static final String TABLE_ELEMENT_JSON_FILE = "./tableElementCompletionItems.json";
    private static final String TABLE_ELEMENT_OPTIONS_JSON_FILE = "./tableElementOptionsCompletionItems.json";
    private static final String TABLE_BODY_OPTIONS_JSON_FILE = "./tableBodyOptionsCompletionItems.json";

    private List<CompletionItem> functionItems;
    private List<CompletionItem> datatypeItems;
    private List<CompletionItem> tableElementItems;
    private List<CompletionItem> searchableValueItems;
    private List<CompletionItem> tableElementOptionsItems;
    private List<CompletionItem> tableBodyOptionsItems;
    private List<CompletionItem> createStatementTemplateItems;
    private List<CompletionItem> queryExpressionItems;
    private List<CompletionItem> queryExpressionKeywordItems;

    private static final String[] KINDS = { "TEXT", "METHOD", "FUNCTION", "CONSTRUCTOR", "FIELD", "VARIABLE", "CLASS",
            "INTERFACE", "MODULE", "PROPERTY", "UNIT", "VALUE", "ENUM", "KEYWORD", "SNIPPET", "COLOR", "FILE",
            "REFERENCE", "FOLDER", "ENUMMEMBER", "CONSTANT", "STRUCT", "EVENT", "OPERATOR", "TYPEPARAMETER" };

    private static final ImmutableMap<String, CompletionItemKind> STRING_TO_KIND_MAP = ImmutableMap
            .<String, CompletionItemKind>builder().put(KINDS[0], CompletionItemKind.Text)
            .put(KINDS[1], CompletionItemKind.Method).put(KINDS[2], CompletionItemKind.Function)
            .put(KINDS[3], CompletionItemKind.Constructor).put(KINDS[4], CompletionItemKind.Field)
            .put(KINDS[5], CompletionItemKind.Variable).put(KINDS[6], CompletionItemKind.Class)
            .put(KINDS[7], CompletionItemKind.Interface).put(KINDS[8], CompletionItemKind.Module)
            .put(KINDS[9], CompletionItemKind.Property).put(KINDS[10], CompletionItemKind.Unit)
            .put(KINDS[11], CompletionItemKind.Value).put(KINDS[12], CompletionItemKind.Enum)
            .put(KINDS[13], CompletionItemKind.Keyword).put(KINDS[14], CompletionItemKind.Snippet)
            .put(KINDS[15], CompletionItemKind.Color).put(KINDS[16], CompletionItemKind.File)
            .put(KINDS[17], CompletionItemKind.Reference).put(KINDS[18], CompletionItemKind.Folder)
            .put(KINDS[19], CompletionItemKind.EnumMember).put(KINDS[20], CompletionItemKind.Constant)
            .put(KINDS[21], CompletionItemKind.Operator).put(KINDS[22], CompletionItemKind.Struct)
            .put(KINDS[23], CompletionItemKind.Event).put(KINDS[24], CompletionItemKind.TypeParameter).build();

    private DdlCompletionItemLoader() {
        // singleton
    }

    public static DdlCompletionItemLoader getInstance() {
        return INSTANCE;
    }

    @SuppressWarnings("unchecked")
    private List<CompletionItem> loadItemsFromFile(String fileName) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();

        try (InputStream stream = this.getClass().getResourceAsStream(fileName)) {
            JSONParser parser = new JSONParser(stream);

            // A JSON object. Key value pairs are unordered. JSONObject supports
            // java.util.Map interface.
            LinkedHashMap<String, Object> jsonObject = parser.object();

            // A JSON array. JSONObject supports java.util.List interface.
            ArrayList<?> completionItemArray = (ArrayList<?>) jsonObject.get("items");

            if (completionItemArray != null) {
                for (Object item : completionItemArray) {
                    LinkedHashMap<String, Object> itemInfo = (LinkedHashMap<String, Object>) item;
                    CompletionItem newItem = new CompletionItem();
                    newItem.setLabel((String) itemInfo.get("label"));
                    newItem.setKind(STRING_TO_KIND_MAP.get(((String) itemInfo.get("kind")).toUpperCase(Locale.US)));

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

        } catch (IOException | ParseException e) {
            throw new IllegalArgumentException("Unable to parse given file: " + fileName, e);
        }

        return items;
    }

    private void addItemByCategory(String category, CompletionItem item) {
        if ("SEARCHABLE_VALUE".equalsIgnoreCase(category)) {
            if (searchableValueItems == null) {
                searchableValueItems = new ArrayList<CompletionItem>();
            }
            if ("SEARCHABLE_VALUE".equalsIgnoreCase(item.getLabel())) {
                item.setLabel("SEARCHABLE");
            }
            searchableValueItems.add(item);
        }
    }

    public List<CompletionItem> getFunctionCompletionItems() {
        if (functionItems == null) {
            functionItems = loadItemsFromFile(FUNCTION_JSON_FILE);
        }
        return functionItems;
    }

    public List<CompletionItem> getTableElementCompletionItems() {
        if (tableElementItems == null) {
            tableElementItems = loadItemsFromFile(TABLE_ELEMENT_JSON_FILE);
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
            tableElementOptionsItems = loadItemsFromFile(TABLE_ELEMENT_OPTIONS_JSON_FILE);
        }
    }

    public List<CompletionItem> getDatatypesCompletionItems() {
        if (datatypeItems == null) {
            datatypeItems = loadItemsFromFile(DATATYPES_JSON_FILE);
        }
        return datatypeItems;
    }

    public List<CompletionItem> getTableBodyOptionsCompletionItems() {
        if (tableBodyOptionsItems == null) {
            tableBodyOptionsItems = loadItemsFromFile(TABLE_BODY_OPTIONS_JSON_FILE);
        }
        return tableBodyOptionsItems;
    }

    public List<CompletionItem> getCreateStatementTemplateCompletionItems() {
        if (createStatementTemplateItems == null) {
            createStatementTemplateItems = new ArrayList<CompletionItem>();
            createStatementTemplateItems.add(getCreateViewCompletionItem(1, "1040"));
            createStatementTemplateItems.add(getCreateViewInnerJoinCompletionItem(2, "1060"));
            createStatementTemplateItems.add(getCreateViewJoinCompletionItem(3, "1080"));
            createStatementTemplateItems.add(getCreateViewUnionCompletionItem(4, "1100"));
        }
        return createStatementTemplateItems;
    }

    public List<CompletionItem> getQueryExpressionKeywordItems() {
        if (queryExpressionKeywordItems == null) {
            queryExpressionKeywordItems = new ArrayList<CompletionItem>();

            for (int i = 0; i < SQLParserConstants.tokenImage.length; i++) {
                String image = SQLParserConstants.tokenImage[i];
                if (!image.startsWith("\"") || !image.endsWith("\"")) {
                    continue;
                }
                image = image.substring(1, image.length() - 1);
                // newer teiid versions won't require upper
                String upper = image.toUpperCase(Locale.US);
                if (DataTypeManager.DefaultDataTypes.OBJECT.equalsIgnoreCase(upper)
                        || DataTypeManager.getDataTypeClass(image) != DataTypeManager.DefaultDataClasses.OBJECT
                        || !(SQLConstants.getReservedWords().contains(upper)
                                || SQLConstants.getNonReservedWords().contains(upper))) {
                    // it's a datatype keyword, or not a keyword (it's a token)
                    continue;
                }
                queryExpressionKeywordItems.add(createKeywordItemFromItemData(getKeywordLabel(i, true)));
            }
        }
        return queryExpressionKeywordItems;
    }

    public CompletionItem cloneQueryExpressionKeywordItem(String targetLabel) {
        return cloneCompletionItem(getQueryExpressionKeywordItems(), targetLabel);
    }

    /**
     * Clones a {@link CompletionItem} for the given label from the supplied items
     *
     * @param targetLabel  - label of the item to select
     * @param items - list of items to search on
     * @return a completion item
     */
    private static CompletionItem cloneCompletionItem(List<CompletionItem> items, String targetLabel) {
        for (CompletionItem item : items) {
            if (item.getLabel().equalsIgnoreCase(targetLabel)) {
                CompletionItem clone = new CompletionItem(targetLabel);
                clone.setAdditionalTextEdits(item.getAdditionalTextEdits());
                clone.setCommand(item.getCommand());
                clone.setData(item.getData());
                clone.setDetail(item.getDetail());
                clone.setFilterText(item.getFilterText());
                clone.setInsertText(item.getInsertText());
                clone.setInsertTextFormat(item.getInsertTextFormat());
                clone.setDocumentation(item.getDocumentation());
                clone.setDeprecated(item.getDeprecated());
                clone.setKind(item.getKind());
                clone.setSortText(item.getSortText());
                clone.setPreselect(item.getPreselect());
                clone.setTextEdit(item.getTextEdit());
                return clone;
            }
        }
        return null;
    }

    public List<CompletionItem> getQueryExpressionItems() {
        if (queryExpressionItems == null) {
            queryExpressionItems = new ArrayList<CompletionItem>();
            queryExpressionItems.addAll(getDatatypesCompletionItems());
            queryExpressionItems.addAll(getQueryExpressionKeywordItems());
        }
        return queryExpressionItems;
    }

    public CompletionItem getCreateViewCompletionItem(int data, String sortValue) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("CREATE VIEW...");
        ci.setInsertText(QueryExpressionHelper.CREATE_VIEW_INSERT_TEXT);
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDetail("Simple CREATE VIEW statement");
        ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        ci.setSortText(sortValue);
        return ci;
    }

    public CompletionItem getCreateViewInnerJoinCompletionItem(int data, String sortValue) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("CREATE VIEW with INNER JOIN");
        ci.setInsertText(QueryExpressionHelper.CREATE_VIEW_INNER_JOIN_INSERT_TEXT);
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDetail(" CREATE VIEW with inner join");
        ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        ci.setSortText(sortValue);
        return ci;
    }

    public CompletionItem getCreateViewJoinCompletionItem(int data, String sortValue) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("CREATE VIEW with JOIN");
        ci.setInsertText(QueryExpressionHelper.CREATE_VIEW_LEFT_OUTER_JOIN_INSERT_TEXT);
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDetail(" CREATE VIEW with left join");
        ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        ci.setSortText(sortValue);
        return ci;
    }

    public CompletionItem getCreateViewUnionCompletionItem(int data, String sortValue) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel("CREATE VIEW with UNION");
        ci.setInsertText(QueryExpressionHelper.CREATE_VIEW_UNION_INSERT_TEXT);
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setDetail(" Union of two tables from single source");
        ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        ci.setData(data);
        ci.setPreselect(true);
        ci.setSortText(sortValue);
        return ci;
    }
}
