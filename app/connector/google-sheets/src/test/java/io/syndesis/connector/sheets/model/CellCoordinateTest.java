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

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

public class CellCoordinateTest {

    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "A", "1", 0, 0},
            { "C", "5", 4, 2},
            { "B", "10", 9, 1},
            { "A", "", 0, 0},
            { "E", "", 0, 4},
            { "Z", "1", 0, 25},
            { "AA", "1", 0, 26},
            { "AZ", "10", 9, 51},
            { "BA", "1", 0, 52},
            { "CC", "1", 0, 80},
            { "ZZ", "1", 0, 27 * 26 - 1}
        });
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testFromCellId(final String columnName, final String rowIndex, final int rowIndexCheck, final int columnIndexCheck) {
        String cellId = columnName + rowIndex;
        CellCoordinate coordinate = CellCoordinate.fromCellId(cellId);
        Assertions.assertEquals(rowIndexCheck, coordinate.getRowIndex());
        Assertions.assertEquals(columnIndexCheck, coordinate.getColumnIndex());
    }

    @ParameterizedTest
    @MethodSource("data")
    public void testGetColumnName(final String columnName, final String rowIndex, final int rowIndexCheck, final int columnIndexCheck) {
        Assertions.assertEquals(columnName, CellCoordinate.getColumnName(columnIndexCheck));

        String[] names = new String[columnIndexCheck];
        Arrays.fill(names, "Foo");
        Assertions.assertEquals(columnName, CellCoordinate.getColumnName(columnIndexCheck, 0, names));

        names = new String[columnIndexCheck + 1];
        Arrays.fill(names, "Foo");
        Assertions.assertEquals("Foo", CellCoordinate.getColumnName(columnIndexCheck, 0, names));
        Assertions.assertEquals("Foo", CellCoordinate.getColumnName(columnIndexCheck, columnIndexCheck, names));
    }
}
