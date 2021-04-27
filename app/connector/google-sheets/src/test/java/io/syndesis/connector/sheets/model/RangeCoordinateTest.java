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

public class RangeCoordinateTest {

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

    @ParameterizedTest
    @MethodSource("data")
    public void testFromRange(final String range, final int rowStartIndex, final int rowEndIndex, final int columnStartIndex, final int columnEndIndex) {
        RangeCoordinate coordinate = RangeCoordinate.fromRange(range);
        Assertions.assertEquals(rowStartIndex, coordinate.getRowStartIndex());
        Assertions.assertEquals(rowEndIndex, coordinate.getRowEndIndex());
        Assertions.assertEquals(columnStartIndex, coordinate.getColumnStartIndex());
        Assertions.assertEquals(columnEndIndex, coordinate.getColumnEndIndex());

    }
}
