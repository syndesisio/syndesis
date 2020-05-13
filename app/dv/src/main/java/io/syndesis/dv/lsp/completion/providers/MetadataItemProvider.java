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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.dv.lsp.TeiidDdlWorkspaceService;
import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.Context;
import io.syndesis.dv.lsp.parser.statement.CreateViewStatement;
import io.syndesis.dv.lsp.parser.statement.TokenContext;
import io.syndesis.dv.server.endpoint.MetadataService;
import io.syndesis.dv.server.endpoint.RestSourceColumn;
import io.syndesis.dv.server.endpoint.RestSourceSchema;
import io.syndesis.dv.server.endpoint.RestSourceTable;
import io.syndesis.dv.server.endpoint.RestViewSourceInfo;

public class MetadataItemProvider extends CompletionItemBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(MetadataItemProvider.class);

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
        List<CompletionItem> items = new ArrayList<CompletionItem>();

        String virtName = context.getVirtualizationId();

        if( context.getContext() == Context.SELECT_CLAUSE && virtName != null ) {
            items = this.getCompletionItemsForVirtualization(virtName, this.statement.getViewName());
        } else if( context.getContext() == Context.FROM_CLAUSE && virtName != null) {
            items = this.getCompletionItemsForVirtualization(virtName, this.statement.getViewName());
        } else if( context.getContext() == Context.WHERE_CLAUSE && virtName != null) {
            items = this.getCompletionItemsForVirtualization(virtName, this.statement.getViewName());
        } else if( context.getContext() == Context.QUERY_EXPRESSION && virtName != null) {
            items = this.getCompletionItemsForVirtualization(virtName, this.statement.getViewName());
        }
//        switch (context.getContext()) {
//        case SELECT_CLAUSE: {
//            if (context.getVirtualizationId() != null) {
//                items = this.getCompletionItemsForVirtualization(context.getVirtualizationId(),
//                        this.statement.getViewName());
//            }
//        }
//            break;
//
//        case FROM_CLAUSE: {
//            if (context.getVirtualizationId() != null) {
//                items = this.getCompletionItemsForVirtualization(context.getVirtualizationId(),
//                        this.statement.getViewName());
//            }
//        }
//            break;
//
//        case WHERE_CLAUSE: {
//            if (context.getVirtualizationId() != null) {
//                items = this.getCompletionItemsForVirtualization(context.getVirtualizationId(),
//                        this.statement.getViewName());
//            }
//        }
//            break;
//
//        case QUERY_EXPRESSION: {
//            if (context.getVirtualizationId() != null) {
//                items = this.getCompletionItemsForVirtualization(context.getVirtualizationId(),
//                        this.statement.getViewName());
//            }
//        }
//            break;
//
//        default: {
//        }
//        }

        return items;
    }

    public List<CompletionItem> getCompletionItemsForVirtualization(String virtualizationName, String viewName) {
        if (virtualizationName != null) {
            RestViewSourceInfo schemaInfo = getRuntimeMetadata(virtualizationName);

            return createItems(schemaInfo, virtualizationName, viewName);
        }
        return null;
    }

    private  static String getFQName(RestSourceSchema schema, RestSourceTable table, RestSourceColumn column) {
        return new StringBuilder().append(schema.getName()).append('.').append(table.getName()).append('.')
                .append(column.getName()).toString();
    }

    private  static String getFQName(RestSourceSchema schema, RestSourceTable table) {
        return new StringBuilder().append(schema.getName()).append('.').append(table.getName()).toString();
    }

    private  static List<CompletionItem> createItems(RestViewSourceInfo schemaInfo, String virtualizationName,
            String viewName) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();

        logDebug("  MetadataItemProvider.createItems() for >>  virtualizationName = " + virtualizationName
                    + "  viewName = " + viewName);

        if (schemaInfo == null) {
            return items;
        }

        Set<String> allLabels = new HashSet<String>();

        for (RestSourceSchema nextSchema : schemaInfo.getSchemas()) {

            CompletionItem schemaItem = new CompletionItem();
            schemaItem.setLabel(nextSchema.getName());
            schemaItem.setKind(CompletionItemKind.Text);
            addItem(items, allLabels, schemaItem);
            logDebug("         >>  ITEM = " + schemaItem.getLabel());

            for (RestSourceTable nextTable : nextSchema.getTables()) {

                // We don't want to return items for the targeted View Definition
                // 1) Check the nextTable name == viewName
                // 2) Check the virtualizationName with the
                // 3) If they both match, then call break; to skip generation

                boolean doNextTable = true;

                if (virtualizationName.equalsIgnoreCase(nextSchema.getName())) {
                    doNextTable = !nextTable.getName().equalsIgnoreCase(viewName);
                }

                if (doNextTable) {
                    CompletionItem tableItem = new CompletionItem();
                    tableItem.setLabel(nextTable.getName());
                    tableItem.setKind(CompletionItemKind.Text);
                    logDebug("         >>  ITEM = " + tableItem.getLabel());
                    addItem(items, allLabels, tableItem);

                    CompletionItem schemaTableItem = new CompletionItem();
                    schemaTableItem.setLabel(getFQName(nextSchema, nextTable));
                    schemaTableItem.setKind(CompletionItemKind.Text);
                    logDebug("         >>  ITEM = " + schemaTableItem.getLabel());
                    addItem(items, allLabels, schemaTableItem);

                    for (RestSourceColumn nextColumn : nextTable.getColumns()) {
                        CompletionItem newItem = new CompletionItem();
                        newItem.setLabel(getFQName(nextSchema, nextTable, nextColumn));
                        newItem.setKind(CompletionItemKind.Text);
                        logDebug("         >>  ITEM = " + newItem.getLabel());
                        addItem(items, allLabels, newItem);

                        CompletionItem columnItem = new CompletionItem();
                        columnItem.setLabel(nextColumn.getName());
                        columnItem.setKind(CompletionItemKind.Text);
                        logDebug("         >>  ITEM = " + columnItem.getLabel());
                        addItem(items, allLabels, columnItem);
                    }
                }
            }
        }

        return items;
    }

    private  static void addItem(List<CompletionItem> items, Set<String> allLabels, CompletionItem newItem) {
        if (!allLabels.contains(newItem.getLabel())) {
            allLabels.add(newItem.getLabel());
            items.add(newItem);
        }
    }

    private RestViewSourceInfo getRuntimeMetadata(String virtualizationName) {
        if (this.metadataService != null) {
            try {
                return this.metadataService.getRuntimeMetadata(virtualizationName);
            } catch (Exception e) {
                LOGGER.error("MetadataItemProvider.getRuntimeMetadata() ERROR accessing runtime metadata", e);
            }
        }
        return null;
    }

    private static void logDebug(String msg) {
        if(LOGGER.isDebugEnabled()) {
            LOGGER.debug(msg);
        }
    }
}
