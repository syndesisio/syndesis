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

import org.eclipse.lsp4j.CompletionItem;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants.MetadataType;

/**
 * Metadata completion items contain a true {@link CompletionItem} as well as
 * additional information to handle referencing metadata via the
 * schema.table.column references.
 *
 * These references may be of the form schema.table.column, table.column or
 * column. So the actual full metadata can be referenced/checked if needed
 */
public class MetadataCompletionItem {
    private final CompletionItem completionItem;
    private final MetadataType metadataType;
    private String schemaName;
    private String tableName;
    private String columnName;
    private boolean isCachedDuplicate;

    public MetadataCompletionItem(CompletionItem completionItem, MetadataType metadataType) {
        super();
        this.completionItem = completionItem;
        this.metadataType = metadataType;
    }

    public MetadataCompletionItem(CompletionItem completionItem, MetadataType metadataType, String schemaName,
            String tableName, String columnName) {
        this(completionItem, metadataType);
        this.schemaName = schemaName;
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public CompletionItem getCompletionItem() {
        return completionItem;
    }

    public MetadataType getMetadataType() {
        return metadataType;
    }

    public String getSchemaName() {
        return schemaName;
    }

    public String getTableName() {
        return tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public boolean isCachedDuplicate() {
        return isCachedDuplicate;
    }

    public void setCachedDuplicate(boolean isCachedDuplicate) {
        this.isCachedDuplicate = isCachedDuplicate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(200);
        sb.append(this.completionItem.getLabel()).append("\n\tmetadataType = ")
        .append(this.metadataType.name()).append("\n\tschema = ").append(this.schemaName)
        .append("\n\ttable  = ").append(this.tableName).append("\n\tcolumn = ").append(this.columnName);
        return sb.toString();
    }
}
