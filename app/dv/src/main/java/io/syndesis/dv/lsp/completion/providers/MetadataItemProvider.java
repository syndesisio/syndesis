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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.teiid.query.parser.SQLParserConstants;
import org.teiid.query.parser.Token;

import io.syndesis.dv.lsp.TeiidDdlWorkspaceService;
import io.syndesis.dv.lsp.completion.providers.items.MetadataCompletionItem;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.Context;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.MetadataType;
import io.syndesis.dv.lsp.parser.statement.AbstractStatementObject;
import io.syndesis.dv.lsp.parser.statement.CreateViewStatement;
import io.syndesis.dv.lsp.parser.statement.FromClause;
import io.syndesis.dv.lsp.parser.statement.TableSymbol;
import io.syndesis.dv.lsp.parser.statement.TokenContext;
import io.syndesis.dv.server.endpoint.MetadataService;
import io.syndesis.dv.server.endpoint.RestSourceColumn;
import io.syndesis.dv.server.endpoint.RestSourceSchema;
import io.syndesis.dv.server.endpoint.RestSourceTable;
import io.syndesis.dv.server.endpoint.RestViewSourceInfo;

@SuppressWarnings("PMD.GodClass")
public class MetadataItemProvider extends CompletionItemBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataItemProvider.class);

    private static final boolean DO_PRINT_TO_CONSOLE = false;
    CreateViewStatement statement;
    MetadataService metadataService;
    TeiidDdlWorkspaceService workspaceService;

    public MetadataItemProvider(CreateViewStatement statement, MetadataService metadataService,
            TeiidDdlWorkspaceService workspaceService) {
        super();
        this.statement = statement;
        this.metadataService = metadataService;
        this.workspaceService = workspaceService;
    }

    public List<CompletionItem> getCompletionItems(TokenContext context) {
        String virtualizationName = context.getVirtualizationId();
        return getCompletionItems(context, virtualizationName);
    }

    public List<MetadataCompletionItem> getAllMetadataCompletionItems(String virtualizationName, String viewName) {
        RestViewSourceInfo schemaInfo = getRuntimeMetadata(virtualizationName);

        if (schemaInfo != null) {
            return createItems(schemaInfo, virtualizationName, viewName);
        }

        return Collections.emptyList();
    }

    public List<CompletionItem> getCompletionItems(TokenContext context, String virtualizationName) {
        List<MetadataCompletionItem> allMetadataItems = this.getAllMetadataCompletionItems(virtualizationName,
                this.statement.getViewName());

        List<CompletionItem> returnItems = new ArrayList<CompletionItem>();

        AbstractStatementObject targetObject = context.getTargetObject();

        if (targetObject instanceof TableSymbol && context.getToken().kind == SQLParserConstants.COMMA) {
            // Add all items that are of types:
            // SchemaName.TableName
            // TableName
            List<MetadataType> allowableTypes = new ArrayList<MetadataType>();
            allowableTypes.add(MetadataType.TABLE);
            allowableTypes.add(MetadataType.SCHEMA_TABLE);
            List<CompletionItem> filteredItems = filterCompletionItems(allMetadataItems, allowableTypes, false);
            returnItems.addAll(filteredItems);
        } else {
            switch (context.getContext()) {
                case SELECT_CLAUSE:
                case SELECT_COLUMN:
                case FUNCTION: {
                    processSelectClause(allMetadataItems, returnItems);
                }
                    break;
                case FROM_CLAUSE:
                case TABLE_SYMBOL: {
                    processFromClause(context, allMetadataItems, returnItems);
                }
                    break;
                case WHERE_CLAUSE_TABLE_ALIAS:
                case WHERE_CLAUSE: {
                    processWhereClause(context, allMetadataItems, returnItems);
                }
                    break;
                case QUERY_EXPRESSION: {
                    returnItems.addAll(filterCompletionItems(allMetadataItems, null, false));
                }
                    break;
                case TABLE_ALIAS: {
                    processForTableAlias(context, allMetadataItems, returnItems);
                }
                    break;
                default:
                    break;
            }
        }

        logDebug("  RETURNING [ " + returnItems.size() + " ] METADATA ITEMS for CONTEXT = " + context.contextToString());
        if (DO_PRINT_TO_CONSOLE) {
            for (CompletionItem item : returnItems) {
                logDebug(" >>  ITEM = " + item.getLabel());
            }
        }
        return returnItems;
    }

    private void processFromClause(TokenContext context, List<MetadataCompletionItem> allMetadataItems,
            List<CompletionItem> returnItems) {
        AbstractStatementObject targetObject = context.getTargetObject();
        Token targetToken = context.getToken();
        if (targetObject instanceof FromClause) {
            // Add all items that are of types:
            // SchemaName.TableName
            // TableName
            List<MetadataType> allowableTypes = new ArrayList<MetadataType>();
            allowableTypes.add(MetadataType.TABLE);
            allowableTypes.add(MetadataType.SCHEMA_TABLE);
            List<CompletionItem> filteredItems = filterCompletionItems(allMetadataItems, allowableTypes, false);
            returnItems.addAll(filteredItems);
        } else if (targetObject instanceof TableSymbol) {
            // TableSymbol to = (TableSymbol)targetObject;
            if (targetToken.kind == SQLParserConstants.ID || targetToken.kind == SQLParserConstants.STRINGVAL) {
                String[] values = { "AS" };
                returnItems.addAll(generateCompletionItems(values));
            } else if (targetToken.kind == SQLParserConstants.COMMA) {
                List<MetadataType> allowableTypes = new ArrayList<MetadataType>();
                allowableTypes.add(MetadataType.TABLE);
                allowableTypes.add(MetadataType.SCHEMA_TABLE);
                List<CompletionItem> filteredItems = filterCompletionItems(allMetadataItems, allowableTypes, false);
                returnItems.addAll(filteredItems);
            }
        }
    }

    private void processSelectClause(List<MetadataCompletionItem> allMetadataItems, List<CompletionItem> returnItems) {
        List<MetadataType> allowableTypes = new ArrayList<MetadataType>();
        allowableTypes.add(MetadataType.COLUMN);
        allowableTypes.add(MetadataType.TABLE_COLUMN);
        List<CompletionItem> filteredItems = filterCompletionItems(allMetadataItems, allowableTypes, true);
        returnItems.addAll(filteredItems);
    }

    public List<CompletionItem> getOnlyColumnMetadataItems(String virtualizationName) {
        List<MetadataType> allowableTypes = new ArrayList<MetadataType>();
        allowableTypes.add(MetadataType.COLUMN);
        return filterCompletionItems(
                this.getAllMetadataCompletionItems(virtualizationName, this.statement.getViewName()), allowableTypes,
                true);
    }

    private void processWhereClause(TokenContext context, List<MetadataCompletionItem> allMetadataItems,
            List<CompletionItem> returnItems) {
        if (context.getContext() == Context.WHERE_CLAUSE_TABLE_ALIAS) {
            String aliasTableName = context.getToken().image;
            logDebug("  MIP >>>  CONTEXT = TABLE_ALIAS   name = " + aliasTableName);
            List<CompletionItem> filteredItems = filterCompletionItemsForTableAlias(allMetadataItems, aliasTableName);
            returnItems.addAll(filteredItems);
        } else {
            List<MetadataType> allowableTypes = new ArrayList<MetadataType>();
            allowableTypes.add(MetadataType.COLUMN);
            allowableTypes.add(MetadataType.TABLE_COLUMN);
            returnItems.addAll(filterCompletionItems(allMetadataItems, allowableTypes, true));
        }
    }

    private void processForTableAlias(TokenContext context, List<MetadataCompletionItem> allMetadataItems,
            List<CompletionItem> returnItems) {
        // We've identified that the focused token is a '.' PERIOD character proceeded
        // by a defined table alias
        String aliasTableName = context.getToken().image;
        logDebug("  MIP >>>  CONTEXT = TABLE_ALIAS   name = " + aliasTableName);
        List<CompletionItem> filteredItems = filterCompletionItemsForTableAlias(allMetadataItems, aliasTableName);
        returnItems.addAll(filteredItems);
    }

    private static String getFQName(RestSourceSchema schema, RestSourceTable table, RestSourceColumn column) {
        return new StringBuilder().append(schema.getName()).append('.').append(table.getName()).append('.')
                .append(column.getName()).toString();
    }

    private static String getFQName(RestSourceSchema schema, RestSourceTable table) {
        return new StringBuilder().append(schema.getName()).append('.').append(table.getName()).toString();
    }

    private static String getFQName(RestSourceTable table, RestSourceColumn column) {
        return new StringBuilder().append(table.getName()).append('.').append(column.getName()).toString();
    }

    public static List<MetadataCompletionItem> createItems(RestViewSourceInfo schemaInfo, String virtualizationName,
            String viewName) {
        List<MetadataCompletionItem> items = new ArrayList<MetadataCompletionItem>();

        logDebug("  MetadataItemProvider.createItems() for >>  virtualizationName = " + virtualizationName
                + "  viewName = " + viewName);

        if (schemaInfo == null) {
            logDebug("No metadata items returned. SCHEMA INFO == NULL");
            return items;
        }

        Set<String> allLabels = new HashSet<String>();

        for (RestSourceSchema nextSchema : schemaInfo.getSchemas()) {

            CompletionItem schemaItem = new CompletionItem(nextSchema.getName());
            schemaItem.setKind(CompletionItemKind.Text);
            schemaItem.setSortText(SORT_WEIGHT_1080);
            MetadataCompletionItem metadataCompetionItem = new MetadataCompletionItem(schemaItem, MetadataType.SCHEMA,
                    nextSchema.getName(), null, null);
            addItem(items, allLabels, metadataCompetionItem);

            for (RestSourceTable nextTable : nextSchema.getTables()) {

                // We don't want to return items for the targeted View Definition
                // 1) Check the nextTable name == viewName
                // 2) Check the virtualizationName with the
                // 3) If they both match, then call break; to skip generation

                boolean doNextTable = true;

                if (virtualizationName != null && virtualizationName.equalsIgnoreCase(nextSchema.getName())) {
                    doNextTable = !nextTable.getName().equalsIgnoreCase(viewName);
                }

                if (doNextTable) {
                    CompletionItem tableItem = new CompletionItem(nextTable.getName());
                    tableItem.setKind(CompletionItemKind.Text);
                    metadataCompetionItem = new MetadataCompletionItem(tableItem, MetadataType.TABLE,
                            nextSchema.getName(), nextTable.getName(), null);
                    setSortText(metadataCompetionItem);
                    addItem(items, allLabels, metadataCompetionItem);

                    CompletionItem schemaTableItem = new CompletionItem(getFQName(nextSchema, nextTable));
                    schemaTableItem.setKind(CompletionItemKind.Text);
                    schemaTableItem.setSortText(SORT_WEIGHT_1080);
                    metadataCompetionItem = new MetadataCompletionItem(schemaTableItem, MetadataType.SCHEMA_TABLE,
                            nextSchema.getName(), nextTable.getName(), null);
                    setSortText(metadataCompetionItem);
                    addItem(items, allLabels, metadataCompetionItem);

                    for (RestSourceColumn nextColumn : nextTable.getColumns()) {
                        CompletionItem columnItem = new CompletionItem(nextColumn.getName());
                        columnItem.setKind(CompletionItemKind.Text);
                        columnItem.setSortText(SORT_WEIGHT_1050);
                        metadataCompetionItem = new MetadataCompletionItem(columnItem, MetadataType.COLUMN,
                                nextSchema.getName(), nextTable.getName(), nextColumn.getName());
                        setSortText(metadataCompetionItem);
                        addItem(items, allLabels, metadataCompetionItem);

                        CompletionItem tableColumnItem = new CompletionItem(getFQName(nextTable, nextColumn));
                        tableColumnItem.setKind(CompletionItemKind.Text);
                        tableColumnItem.setSortText(SORT_WEIGHT_1080);
                        metadataCompetionItem = new MetadataCompletionItem(tableColumnItem, MetadataType.TABLE_COLUMN,
                                nextSchema.getName(), nextTable.getName(), nextColumn.getName());
                        setSortText(metadataCompetionItem);
                        addItem(items, allLabels, metadataCompetionItem);

                        CompletionItem schemaTableColumnItem = new CompletionItem(
                                getFQName(nextSchema, nextTable, nextColumn));
                        schemaTableColumnItem.setKind(CompletionItemKind.Text);
                        schemaTableColumnItem.setSortText(SORT_WEIGHT_1100);
                        metadataCompetionItem = new MetadataCompletionItem(schemaTableColumnItem,
                                MetadataType.SCHEMA_TABLE_COLUMN, nextSchema.getName(), nextTable.getName(),
                                nextColumn.getName());
                        setSortText(metadataCompetionItem);
                        addItem(items, allLabels, metadataCompetionItem);
                    }
                }
            }
        }
        logDebug("  RETURNING [ " + items.size() + " ] MetadataCompletionItems from createItems() ");
        return items;
    }

    private static void setSortText(MetadataCompletionItem metaItem) {
        if (metaItem.getMetadataType() == MetadataType.COLUMN) {
            metaItem.getCompletionItem().setSortText(SORT_WEIGHT_1050);
        } else if (metaItem.getMetadataType() == MetadataType.TABLE) {
            metaItem.getCompletionItem().setSortText(SORT_WEIGHT_1060);
        } else if (metaItem.getMetadataType() == MetadataType.SCHEMA) {
            metaItem.getCompletionItem().setSortText(SORT_WEIGHT_1090);
        } else if (metaItem.getMetadataType() == MetadataType.TABLE_COLUMN) {
            metaItem.getCompletionItem().setSortText(SORT_WEIGHT_1070);
        } else if (metaItem.getMetadataType() == MetadataType.SCHEMA_TABLE) {
            metaItem.getCompletionItem().setSortText(SORT_WEIGHT_1080);
        } else if (metaItem.getMetadataType() == MetadataType.SCHEMA_TABLE_COLUMN) {
            metaItem.getCompletionItem().setSortText(SORT_WEIGHT_1100);
        }
    }

    private static void addItem(List<MetadataCompletionItem> items, Set<String> allLabels,
            MetadataCompletionItem newItem) {
        if (!allLabels.contains(newItem.getCompletionItem().getLabel())) {
            allLabels.add(newItem.getCompletionItem().getLabel());
            items.add(newItem);
            logDebug("  addItem() : \n" + newItem);
        } else {
            // Note that we can only return 1 item with each label/string
            // However different tables might have same column names
            // So if duplicate label, tag the item as cached duplicate
            // This can be used to check items later for aliased tables
            if (newItem.getMetadataType() == MetadataType.COLUMN) {
                newItem.setCachedDuplicate(true);
                items.add(newItem);
                logDebug("  addItem()  Cached Duplicate: \n" + newItem);
            }
        }
    }

    /**
     * This method filters the metadata items based on: 1) allowable types 2)
     * statement context based on position and relevant token(s) within the
     * statement 3)
     *
     * @param metadataItems - items to filter
     * @param allowableTypes - filter
     * @param checkSchemaScope - filter columns that aren't referenced in the
     *                         FromClause
     * @return the list of filtered items
     */
    private List<CompletionItem> filterCompletionItems(List<MetadataCompletionItem> metadataItems,
            List<MetadataType> allowableTypes, boolean checkSchemaScope) {
        List<CompletionItem> filteredItems = new ArrayList<CompletionItem>();

        for (MetadataCompletionItem nextMI : metadataItems) {
            // if (!nextMI.isCachedDuplicate()) {
                if ((allowableTypes == null || allowableTypes.contains(nextMI.getMetadataType()))) {
                    if (checkSchemaScope && isSchemaTableInScope(nextMI)) {
                        filteredItems.add(nextMI.getCompletionItem());
                    } else if (!checkSchemaScope) {
                        filteredItems.add(nextMI.getCompletionItem());
                    }
                }
            // }
        }
        logDebug("  RETURNING [ " + filteredItems.size() + " ] METADATA ITEMS from filterCompletionItems() ");
        return filteredItems;
    }

    /**
     * This method filters the metadata items based on: 1) allowable types 2)
     * statement context based on position and relevant token(s) within the
     * statement 3)
     *
     * @param metadataItems - items to filter
     * @param aliasTableName - filter
     * @return the list of filtered items.
     */
    public List<CompletionItem> filterCompletionItemsForTableAlias(List<MetadataCompletionItem> metadataItems,
            String aliasTableName) {
        List<CompletionItem> filteredItems = new ArrayList<CompletionItem>();
        logDebug("  ==== START ===== filterCompletionItemsForTableAlias() ==========");
        logDebug("    >>>  TABLE_ALIAS   name = " + aliasTableName);
        // Find metadata table FQN from TableSymbol
        for (TableSymbol nextTS : this.statement.getQueryExpression().getFromClause().getTableSymbols()) {
            logDebug("   >>> TableSymbol schemaName = " + nextTS.getSchemaName() + " tableName = "
                    + nextTS.getTableName());
            if (nextTS.isAliased() && nextTS.getAlias().equalsIgnoreCase(aliasTableName)) {
                String schemaName = nextTS.getSchemaName();
                String tableName = nextTS.getTableName();
                logDebug("   >>> CHECKING FOR COLUMN ITEMS for schemaName = " + schemaName + "  and tableName = "
                        + tableName);
                for (MetadataCompletionItem nextMI : metadataItems) {
                    if (isColumnMetadataForAliasedTable(nextMI, schemaName, tableName)) {
                        filteredItems.add(nextMI.getCompletionItem());
                    }
                }
            }
        }
        logDebug("  ====  END  RETURNING [ " + filteredItems.size() + " ]  ITEMS ============================");
        return filteredItems;
    }

    public boolean isColumnMetadataForAliasedTable(MetadataCompletionItem metadataItem, String sName, String tName) {
        // Only return COLUMN types for SchemaName.TableName or TableName

        if (metadataItem.getMetadataType() == MetadataType.COLUMN) {
            logDebug("   >>> Checking columns for aliased table: " + sName + "." + tName);
            // Schema
            if (sName != null && metadataItem.getSchemaName() != null
                    && metadataItem.getSchemaName().equalsIgnoreCase(sName)) {
                logDebug("   ######## COLUMN MATCHES:  " + metadataItem.getColumnName());
                return metadataItem.getTableName() != null && metadataItem.getTableName().equalsIgnoreCase(tName);
            }
        }

        return false;
    }

    private boolean isSchemaTableInScope(MetadataCompletionItem item) {
        if (item.getTableName() == null) {
            return false;
        }

        FromClause fromClause = statement.getQueryExpression().getFromClause();

        boolean result = false;
        for (TableSymbol tableSymbol : fromClause.getTableSymbols()) {
            if (item.getSchemaName() != null && tableSymbol.getSchemaName() != null) {
                result = tableSymbol.getSchemaName().equalsIgnoreCase(item.getSchemaName())
                        && tableSymbol.getTableName().equalsIgnoreCase(item.getTableName());
            } else if (tableSymbol.getTableName() != null) {
                result = tableSymbol.getTableName().equalsIgnoreCase(item.getTableName());
            }

            if (result) {
                break;
            }
        }
        return result;
    }

    private RestViewSourceInfo getRuntimeMetadata(String virtualizationName) {
        if (virtualizationName != null && this.metadataService != null) {
            try {
                return this.metadataService.getRuntimeMetadata(virtualizationName);
            } catch (Exception e) {
                LOGGER.error("MetadataItemProvider.getRuntimeMetadata() ERROR accessing runtime metadata", e);
            }
        } else {
            LOGGER.error("MetadataItemProvider.getRuntimeMetadata() virtualizationName == NULL");
        }

        return null;
    }

    @SuppressWarnings("PMD.SystemPrintln")
    private static void logDebug(String msg) {
        if (DO_PRINT_TO_CONSOLE) {
            LOGGER.info(msg);
        } else if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(msg);
        }
    }
}
