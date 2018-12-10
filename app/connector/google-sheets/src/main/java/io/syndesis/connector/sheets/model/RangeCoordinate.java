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

/**
 * @author Christoph Deppisch
 */
public final class RangeCoordinate extends CellCoordinate {

    private int rowStartIndex;
    private int rowEndIndex;

    private int columnStartIndex;
    private int columnEndIndex;

    /**
     * Prevent direct instantiation
     */
    private RangeCoordinate() {
        super();
    }

    /**
     * Construct range coordinates from range string representation in A1 form. For instance convert
     * range string "A1:C2" to a coordinate with rowStartIndex=1, rowEndIndex=2, columnStartIndex=1, columnEndIndex=3.
     *
     * Supports missing range ends with "A5" resulting in rowStartIndex=5, rowEndIndex=6, columnStartIndex=1, columnEndIndex=2.
     * @param range
     * @return
     */
    public static RangeCoordinate fromRange(String range) {
        RangeCoordinate coordinate = new RangeCoordinate();

        if (range.contains(":")) {
            String[] coordinates = range.split(":", -1);

            coordinate.setRowStartIndex(getRowIndex(coordinates[0]));
            coordinate.setColumnStartIndex(getColumnIndex(coordinates[0]));
            coordinate.setRowEndIndex(getRowIndex(coordinates[1]) + 1);
            coordinate.setColumnEndIndex(getColumnIndex(coordinates[1]) + 1);
        } else {
            CellCoordinate cellCoordinate = CellCoordinate.fromCellId(range);
            coordinate.setRowIndex(cellCoordinate.getRowIndex());
            coordinate.setColumnIndex(cellCoordinate.getColumnIndex());
            coordinate.setRowStartIndex(cellCoordinate.getRowIndex());
            coordinate.setColumnStartIndex(cellCoordinate.getColumnIndex());
            coordinate.setRowEndIndex(cellCoordinate.getRowIndex() + 1);
            coordinate.setColumnEndIndex(cellCoordinate.getColumnIndex() + 1);
        }

        return coordinate;
    }

    public int getRowStartIndex() {
        return rowStartIndex;
    }

    /**
     * Specifies the rowStartIndex.
     *
     * @param rowStartIndex
     */
    public void setRowStartIndex(int rowStartIndex) {
        this.rowStartIndex = rowStartIndex;
    }

    public int getRowEndIndex() {
        return rowEndIndex;
    }

    /**
     * Specifies the rowEndIndex.
     *
     * @param rowEndIndex
     */
    public void setRowEndIndex(int rowEndIndex) {
        this.rowEndIndex = rowEndIndex;
    }

    public int getColumnStartIndex() {
        return columnStartIndex;
    }

    /**
     * Specifies the columnStartIndex.
     *
     * @param columnStartIndex
     */
    public void setColumnStartIndex(int columnStartIndex) {
        this.columnStartIndex = columnStartIndex;
    }

    public int getColumnEndIndex() {
        return columnEndIndex;
    }

    /**
     * Specifies the columnEndIndex.
     *
     * @param columnEndIndex
     */
    public void setColumnEndIndex(int columnEndIndex) {
        this.columnEndIndex = columnEndIndex;
    }
}
