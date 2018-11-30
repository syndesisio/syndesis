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

public class GoogleValueRange {

    private String spreadsheetId;
    private String range;
    private String values;

    private Integer rangeIndex;
    private Integer valueIndex;

    public String getSpreadsheetId() {
        return spreadsheetId;
    }

    /**
     * Sets the spreadsheetId.
     *
     * @param spreadsheetId
     */
    public void setSpreadsheetId(String spreadsheetId) {
        this.spreadsheetId = spreadsheetId;
    }

    public String getRange() {
        return range;
    }

    /**
     * Sets the range.
     *
     * @param range
     */
    public void setRange(String range) {
        this.range = range;
    }

    public String getValues() {
        return values;
    }

    /**
     * Sets the values.
     *
     * @param values
     */
    public void setValues(String values) {
        this.values = values;
    }

    public Integer getRangeIndex() {
        return rangeIndex;
    }

    /**
     * Specifies the rangeIndex.
     *
     * @param rangeIndex
     */
    public void setRangeIndex(Integer rangeIndex) {
        this.rangeIndex = rangeIndex;
    }

    public Integer getValueIndex() {
        return valueIndex;
    }

    /**
     * Specifies the valueIndex.
     *
     * @param valueIndex
     */
    public void setValueIndex(Integer valueIndex) {
        this.valueIndex = valueIndex;
    }

    @Override
    public String toString() {
        return String.format("%s [spreadsheetId=%s, range=%s, values=%s]", GoogleValueRange.class.getSimpleName(), spreadsheetId, range, values);
    }

}
