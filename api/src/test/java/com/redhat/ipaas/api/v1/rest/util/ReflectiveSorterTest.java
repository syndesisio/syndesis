/**
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.redhat.ipaas.api.v1.rest.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.Test;

import com.redhat.ipaas.api.v1.rest.util.ReflectiveSorter;
import com.redhat.ipaas.api.v1.rest.util.SortOptions;

import static org.junit.Assert.assertEquals;

/**
 * @author roland
 * @since 13/12/16
 */
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
        List reversed = Arrays.asList(expectedNames);
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

    @Test(expected = IllegalArgumentException.class)
    public void invalidType() {
        getTestData().sort(new ReflectiveSorter<>(TestPersonInterface.class, getOptions("blub", "asc")));
    }

    @Test(expected = IllegalArgumentException.class)
    public void invalidDirection() {
        getTestData().sort(new ReflectiveSorter<>(TestPersonInterface.class, getOptions("lastName", "blub")));
    }

    @Test
    public void noParams() {
        List<TestPersonInterface> toSort = getTestData();
        Function<List<TestPersonInterface>, List<TestPersonInterface>> operator = new ReflectiveSorter<>(TestPersonInterface.class, getOptions(null, null));
        operator.apply(toSort);

        String[] expectedNames = {
            "Schrödinger",
            "Heisenberg",
            "Feynman",
            "Maxwell",
        };

        for (int i = 0; i < expectedNames.length; i++) {
            assertEquals(toSort.get(i).getLastName(), expectedNames[i]);
        }

    }

    private SortOptions getOptions(String type, String direction) {
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

    private List<TestPersonInterface> getTestData() {
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

        private String firstName;
        private String lastName;
        private int birthYear;

        TestPerson(String firstName, String lastName, int birthYear) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.birthYear = birthYear;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public int getBirthYear() {
            return birthYear;
        }
    }
}
