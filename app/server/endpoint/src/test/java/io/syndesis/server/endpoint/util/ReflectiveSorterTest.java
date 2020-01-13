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
package io.syndesis.server.endpoint.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.integration.Integration;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.Assert.assertEquals;

public class ReflectiveSorterTest {

    @Test
    public void stringSort() {

        List<TestPersonInterface> toSort = getTestData();

        toSort.sort(new ReflectiveSorter<>(TestPersonInterface.class, getOptions("lastName", "asc")));
        String[] expectedNames = {
            "Feynman",
            "Heisenberg",
            "Maxwell",
            "Schrödinger"
        };

        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(toSort.get(i).getLastName(), expectedNames[i]);
        }

        toSort.sort(new ReflectiveSorter<>(TestPersonInterface.class, getOptions("lastName", "DESC")));
        List<String> reversed = Arrays.asList(expectedNames);
        Collections.reverse(reversed);

        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(toSort.get(i).getLastName(), expectedNames[i]);
        }
    }

    @Test
    public void intSort() {
        List<TestPersonInterface> toSort = getTestData();

        toSort.sort(new ReflectiveSorter<>(TestPersonInterface.class, getOptions("birthYear", null)));
        String[] expectedNames = {
            "Maxwell",
            "Schrödinger",
            "Heisenberg",
            "Feynman"
        };

        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(toSort.get(i).getLastName(), expectedNames[i]);
        }
    }

    @Test
    public void invalidType() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> getTestData().sort(new ReflectiveSorter<>(TestPersonInterface.class, getOptions("blub", "asc"))))
            .withMessage("Cannot find field blub in io.syndesis.server.endpoint.util.ReflectiveSorterTest$TestPersonInterface as int or String field");
    }

    @Test
    public void invalidDirection() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> getTestData().sort(new ReflectiveSorter<>(TestPersonInterface.class, getOptions("lastName", "blub"))))
            .withMessage("No enum constant io.syndesis.server.endpoint.util.SortOptions.SortDirection.BLUB");
    }

    @Test
    public void noParams() {
        ListResult<TestPersonInterface> toSort = new ListResult.Builder<TestPersonInterface>().items(getTestData()).totalCount(getTestData().size()).build();
        Function<ListResult<TestPersonInterface>, ListResult<TestPersonInterface>> operator =
            new ReflectiveSorter<>(TestPersonInterface.class, getOptions(null, null));
        ListResult<TestPersonInterface> sorted = operator.apply(toSort);

        String[] expectedNames = {
            "Schrödinger",
            "Heisenberg",
            "Feynman",
            "Maxwell",
        };

        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(sorted.getItems().get(i).getLastName(), expectedNames[i]);
        }
        assertEquals(getTestData().size(), sorted.getTotalCount());

    }

    /**
     * @see https://github.com/syndesisio/syndesis/issues/7471
     */
    @Test
    public void shouldSortIntegrationsByVersion() {
        final Integration v1 = new Integration.Builder().version(1).build();
        final Integration v2 = new Integration.Builder().version(2).build();
        final Integration v3 = new Integration.Builder().version(3).build();
        final ListResult<Integration> integrations = ListResult.of(v3, v1, v2);

        final ReflectiveSorter<Integration> sorter = new ReflectiveSorter<>(Integration.class, getOptions("version", null));

        assertThat(sorter.apply(integrations)).containsExactly(v1, v2, v3);
    }

    private static SortOptions getOptions(String type, String direction) {
        return new SortOptions() {
            @Override
            public String getSortField() {
                return type;
            }

            @Override
            public SortDirection getSortDirection() {
                return direction != null ? SortDirection.valueOf(direction.toUpperCase()) : SortDirection.ASC;
            }
        };
    }

    private static List<TestPersonInterface> getTestData() {
        return Arrays.asList(

            new TestPerson( "Erwin", "Schrödinger", 1887),
            new TestPerson( "Werner", "Heisenberg", 1901),
            new TestPerson("Richard", "Feynman", 1918),
            new TestPerson( "James Clerk", "Maxwell", 1831)

                            );
    }

    interface TestPersonInterface extends TestPersonBase {
        String getFirstName();
        int getBirthYear();
    }

    interface TestPersonBase {
        String getLastName();
    }

    static class TestPerson implements TestPersonInterface {

        private final String firstName;
        private final String lastName;
        private final int birthYear;

        TestPerson(String firstName, String lastName, int birthYear) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthYear = birthYear;
        }

        @Override
        public String getFirstName() {
            return firstName;
        }

        @Override
        public String getLastName() {
            return lastName;
        }

        @Override
        public int getBirthYear() {
            return birthYear;
        }
    }
}
