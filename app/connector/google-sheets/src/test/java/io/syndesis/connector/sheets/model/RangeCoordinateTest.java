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
public class RangeCoordinateTest {

    private String range;
    private int rowStartIndex;
    private int rowEndIndex;
    private int columnStartIndex;
    private int columnEndIndex;

    public RangeCoordinateTest(String range, int rowStartIndex, int rowEndIndex, int columnStartIndex, int columnEndIndex) {
        this.range = range;
        this.rowStartIndex = rowStartIndex;
        this.rowEndIndex = rowEndIndex;
        this.columnStartIndex = columnStartIndex;
        this.columnEndIndex = columnEndIndex;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
            { "A1:A5", 0, 5, 0, 1},
            { "A1:D9", 0, 9, 0, 4},
            { "A1:Z1", 0, 1, 0, 26},
            { "Z1:Z100", 0, 100, 25, 26},
            { "A1", 0, 1, 0, 1},
            { "C5", 4, 5, 2, 3},
            { "A10", 9, 10, 0, 1},
            { "A", 0, 1, 0, 1},
            { "E", 0, 1, 4, 5}
        });
    }

    @Test
    public void testFromRange() {
        RangeCoordinate coordinate = RangeCoordinate.fromRange(range);
        Assert.assertEquals(rowStartIndex, coordinate.getRowStartIndex());
        Assert.assertEquals(rowEndIndex, coordinate.getRowEndIndex());
        Assert.assertEquals(columnStartIndex, coordinate.getColumnStartIndex());
        Assert.assertEquals(columnEndIndex, coordinate.getColumnEndIndex());

    }
}
