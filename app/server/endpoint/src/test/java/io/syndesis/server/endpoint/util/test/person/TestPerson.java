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
package io.syndesis.server.endpoint.util.test.person;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class TestPerson implements TestPersonInterface {

    enum Country{
        UK,
        US,
        AT,
        DE
    }

    private final String firstName;
    private final String lastName;
    private final Optional<String> university;
    private final int birthYear;
    private final Country country;
    private final boolean nobel;

    TestPerson(String firstName, String lastName, int birthYear, boolean nobel, Country country, String university) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthYear = birthYear;
        this.nobel = nobel;
        this.country = country;
        this.university = Optional.ofNullable(university);
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

    @Override
    public boolean isNobel() {
        return nobel;
    }

    @Override
    public Country getCountry() {
        return country;
    }

    @Override
    public Optional<String> getUniversity() {
        return university;
    }

    public static List<TestPersonInterface> getTestData() {
        return Arrays.asList(
            new TestPerson( "Erwin", "Schrödinger", 1887, false, Country.AT, null),
            new TestPerson( "Werner", "Heisenberg", 1901, true, Country.DE, null),
            new TestPerson("Richard", "Feynman", 1918, true, Country.US, null),
            new TestPerson( "James Clerk", "Maxwell", 1831, false, Country.UK, "Cambridge")
        );
    }
}
