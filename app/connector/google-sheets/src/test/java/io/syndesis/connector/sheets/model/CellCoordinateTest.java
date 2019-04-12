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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 * @author Christoph Deppisch
 */
@RunWith(Parameterized.class)
public class CellCoordinateTest {

    private String cellId;
    private String columnName;
    private int rowIndexCheck;
    private int columnIndexCheck;

    public CellCoordinateTest(String columnName, String rowIndex, int rowIndexCheck, int columnIndexCheck) {
        this.cellId = columnName + rowIndex;
        this.columnName = columnName;
        this.rowIndexCheck = rowIndexCheck;
        this.columnIndexCheck = columnIndexCheck;
    }

    @Parameterized.Parameters
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

    @Test
    public void testFromCellId() {
        CellCoordinate coordinate = CellCoordinate.fromCellId(cellId);
        Assert.assertEquals(rowIndexCheck, coordinate.getRowIndex());
        Assert.assertEquals(columnIndexCheck, coordinate.getColumnIndex());
    }

    @Test
    public void testGetColumnName() {
        Assert.assertEquals(columnName, CellCoordinate.getColumnName(columnIndexCheck));

        String[] names = new String[columnIndexCheck];
        Arrays.fill(names, "Foo");
        Assert.assertEquals(columnName, CellCoordinate.getColumnName(columnIndexCheck, 0, names));

        names = new String[columnIndexCheck + 1];
        Arrays.fill(names, "Foo");
        Assert.assertEquals("Foo", CellCoordinate.getColumnName(columnIndexCheck, 0, names));
        Assert.assertEquals("Foo", CellCoordinate.getColumnName(columnIndexCheck, columnIndexCheck, names));
    }
}
