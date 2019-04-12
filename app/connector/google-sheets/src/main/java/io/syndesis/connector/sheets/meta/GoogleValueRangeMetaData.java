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

package io.syndesis.connector.sheets.meta;

import java.util.Arrays;

/**
 * @author Christoph Deppisch
 */
public class GoogleValueRangeMetaData {

    private String spreadsheetId;
    private String headerRow;
    private String[] columnNames = new String[0];
    private String range;
    private String majorDimension;

    private boolean split;

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

    public String[] getColumnNames() {
        return Arrays.copyOf(columnNames, columnNames.length);
    }

    /**
     * Specifies the columnNames.
     *
     * @param columnNames
     */
    public void setColumnNames(String ... columnNames) {
        this.columnNames = Arrays.copyOf(columnNames, columnNames.length);
    }

    public String getHeaderRow() {
        return headerRow;
    }

    /**
     * Specifies the headerRow.
     *
     * @param headerRow
     */
    public void setHeaderRow(String headerRow) {
        this.headerRow = headerRow;
    }

    public String getRange() {
        return range;
    }

    /**
     * Specifies the range.
     *
     * @param range
     */
    public void setRange(String range) {
        this.range = range;
    }

    public String getMajorDimension() {
        return majorDimension;
    }

    /**
     * Specifies the majorDimension.
     *
     * @param majorDimension
     */
    public void setMajorDimension(String majorDimension) {
        this.majorDimension = majorDimension;
    }

    public boolean isSplit() {
        return split;
    }

    /**
     * Specifies the split.
     *
     * @param split
     */
    public void setSplit(boolean split) {
        this.split = split;
    }
}
