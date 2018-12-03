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

import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.camel.util.ObjectHelper;

/**
 * @author Christoph Deppisch
 */
public class CellCoordinate {

    private int rowIndex;
    private int columnIndex;

    /**
     * Prevent direct instantiation
     */
    CellCoordinate() {
        super();
    }

    /**
     * Construct grid coordinate from given cell identifier representation in A1 form. For instance convert
     * cell id string "A1" to a coordinate with rowIndex=1, and columnIndex=1.
     *
     * @param cellId
     * @return
     */
    public static CellCoordinate fromCellId(String cellId) {
        CellCoordinate coordinate = new CellCoordinate();

        coordinate.setRowIndex(getRowIndex(cellId));
        coordinate.setColumnIndex(getColumnIndex(cellId));

        return coordinate;
    }

    protected static int getColumnIndex(String range) {
        char[] characters = range.toCharArray();
        return IntStream.range(0, characters.length)
            .mapToObj(i -> characters[i])
            .filter(c -> !Character.isDigit(c))
            .findFirst()
            .map(Character::toUpperCase)
            .map(Character::getNumericValue)
            .orElse(0) - Character.getNumericValue('A');
    }

    protected static int getRowIndex(String range) {
        char[] characters = range.toCharArray();
        String index = IntStream.range(0, characters.length)
            .mapToObj(i -> characters[i])
            .filter(Character::isDigit)
            .map(String::valueOf)
            .collect(Collectors.joining());

        if (ObjectHelper.isNotEmpty(index)) {
            return Integer.valueOf(index) - 1;
        }

        return 0;
    }

    public int getRowIndex() {
        return rowIndex;
    }

    /**
     * Specifies the rowIndex.
     *
     * @param rowIndex
     */
    public void setRowIndex(int rowIndex) {
        this.rowIndex = rowIndex;
    }

    public int getColumnIndex() {
        return columnIndex;
    }

    /**
     * Specifies the columnIndex.
     *
     * @param columnIndex
     */
    public void setColumnIndex(int columnIndex) {
        this.columnIndex = columnIndex;
    }
}
