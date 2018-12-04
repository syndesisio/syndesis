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
    private int rowIndex;
    private int columnIndex;

    public CellCoordinateTest(String cellId, int rowIndex, int columnIndex) {
        this.cellId = cellId;
        this.rowIndex = rowIndex;
        this.columnIndex = columnIndex;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "A1", 0, 0},
            { "C5", 4, 2},
            { "B10", 9, 1},
            { "A", 0, 0},
            { "E", 0, 4}
        });
    }

    @Test
    public void testFromCellId() {
        CellCoordinate coordinate = CellCoordinate.fromCellId(cellId);
        Assert.assertEquals(rowIndex, coordinate.getRowIndex());
        Assert.assertEquals(columnIndex, coordinate.getColumnIndex());
    }
}
