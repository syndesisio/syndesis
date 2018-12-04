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

package io.syndesis.connector.sheets.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Christoph Deppisch
 */
public class GooglePivotTable {

    private String spreadsheetId;

    private Integer sheetId;
    private Integer sourceSheetId;

    private String sourceRange;

    private String valueLayout = "HORIZONTAL";

    private String start;

    private List<PivotGroup> rowGroups = new ArrayList<>();
    private List<PivotGroup> columnGroups = new ArrayList<>();
    private List<ValueGroup> valueGroups = new ArrayList<>();

    /**
     * A single grouping (either row or column) in a pivot table.
     */
    public static class PivotGroup {
        private String label;
        private String sortOrder = "ASCENDING";
        private String sourceColumn;
        private boolean showTotals = true;

        private Integer valueGroupIndex;
        private String valueBucket;

        public String getLabel() {
            return label;
        }

        /**
         * Specifies the label.
         *
         * @param label
         */
        public void setLabel(String label) {
            this.label = label;
        }

        public String getSortOrder() {
            return sortOrder;
        }

        /**
         * Specifies the sortOrder.
         *
         * @param sortOrder
         */
        public void setSortOrder(String sortOrder) {
            this.sortOrder = sortOrder;
        }

        public String getSourceColumn() {
            return sourceColumn;
        }

        /**
         * Specifies the source column that this grouping is based on. The value is supposed to use
         * A1 notation where the column offset of the source range is calculated at runtime.
         *
         * @param sourceColumn
         */
        public void setSourceColumn(String sourceColumn) {
            this.sourceColumn = sourceColumn;
        }

        public boolean isShowTotals() {
            return showTotals;
        }

        /**
         * Specifies the showTotals. Defaults to true.
         *
         * @param showTotals
         */
        public void setShowTotals(boolean showTotals) {
            this.showTotals = showTotals;
        }

        public Integer getValueGroupIndex() {
            return valueGroupIndex;
        }

        /**
         * Specifies the information about which values in a pivot group should be used for sorting.
         * The offset in the GooglePivotTable.valueGroups list which the values in this grouping should be sorted by.
         *
         * @param valueGroupIndex
         */
        public void setValueGroupIndex(Integer valueGroupIndex) {
            this.valueGroupIndex = valueGroupIndex;
        }

        public String getValueBucket() {
            return valueBucket;
        }

        /**
         * Specifies the information about which values in a pivot group should be used for sorting.
         * Determines the bucket from which values are chosen to sort.
         *
         * @param valueBucket
         */
        public void setValueBucket(String valueBucket) {
            this.valueBucket = valueBucket;
        }
    }

    /**
     * The definition of how a value in a pivot table should be calculated.
     */
    public static class ValueGroup {
        private String name;
        private String function = "SUM";
        private String sourceColumn;
        private String formula;

        public String getFunction() {
            return function;
        }

        /**
         * Specifies the function.
         *
         * @param function
         */
        public void setFunction(String function) {
            this.function = function;
        }

        public String getSourceColumn() {
            return sourceColumn;
        }

        /**
         * Specifies the source column that this grouping is based on. The value is supposed to use
         * A1 notation where the column offset of the source range is calculated at runtime.
         *
         * This value is exclusive to formula field meaning that either source column or formula is to be used.
         * A specified formula overwrites this setting.
         *
         * @param sourceColumn
         */
        public void setSourceColumn(String sourceColumn) {
            this.sourceColumn = sourceColumn;
        }

        public String getName() {
            return name;
        }

        /**
         * Specifies the name.
         *
         * @param name
         */
        public void setName(String name) {
            this.name = name;
        }

        public String getFormula() {
            return formula;
        }

        /**
         * Specifies the formula as a custom formula to calculate the value.
         * The formula must start with an = character.
         *
         * This value is exclusive to source column meaning that either formula or source columns is to be used.
         * The formula setting will overwrite a source column setting.
         *
         * @param formula
         */
        public void setFormula(String formula) {
            this.formula = formula;
        }
    }

    public String getSpreadsheetId() {
        return spreadsheetId;
    }

    /**
     * Specifies the spreadsheetId.
     *
     * @param spreadsheetId
     */
    public void setSpreadsheetId(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }

    public Integer getSheetId() {
        return sheetId;
    }

    /**
     * Specifies the sheetId.
     *
     * @param sheetId
     */
    public void setSheetId(Integer sheetId) {
        this.sheetId = sheetId;
    }

    public Integer getSourceSheetId() {
        return sourceSheetId;
    }

    /**
     * Specifies the sourceSheetId.
     *
     * @param sourceSheetId
     */
    public void setSourceSheetId(Integer sourceSheetId) {
        this.sourceSheetId = sourceSheetId;
    }

    public String getSourceRange() {
        return sourceRange;
    }

    /**
     * Specifies the sourceRange.
     *
     * @param sourceRange
     */
    public void setSourceRange(String sourceRange) {
        this.sourceRange = sourceRange;
    }

    public List<ValueGroup> getValueGroups() {
        return valueGroups;
    }

    /**
     * Specifies the valueGroups.
     *
     * @param valueGroups
     */
    public void setValueGroups(List<ValueGroup> valueGroups) {
        this.valueGroups = valueGroups;
    }

    public List<PivotGroup> getColumnGroups() {
        return columnGroups;
    }

    /**
     * Specifies the columnGroups.
     *
     * @param columnGroups
     */
    public void setColumnGroups(List<PivotGroup> columnGroups) {
        this.columnGroups = columnGroups;
    }

    public List<PivotGroup> getRowGroups() {
        return rowGroups;
    }

    /**
     * Specifies the rowGroups.
     *
     * @param rowGroups
     */
    public void setRowGroups(List<PivotGroup> rowGroups) {
        this.rowGroups = rowGroups;
    }

    public String getValueLayout() {
        return valueLayout;
    }

    /**
     * Specifies the valueLayout.
     *
     * @param valueLayout
     */
    public void setValueLayout(String valueLayout) {
        this.valueLayout = valueLayout;
    }

    public String getStart() {
        return start;
    }

    /**
     * Specifies the start cell coordinate of the generated pivot table. This coordinate represents the upper left corner of
     * the newly created pivot table.
     *
     * @param start
     */
    public void setStart(String start) {
        this.start = start;
    }

    @Override
    public String toString() {
        return String.format("%s [spreadsheetId=%s, sheetId=%s, source=%s]",
            GooglePivotTable.class.getSimpleName(),
            spreadsheetId,
            sheetId,
            sourceRange);
    }

}
