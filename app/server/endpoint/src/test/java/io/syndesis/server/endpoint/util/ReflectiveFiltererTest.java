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

import java.util.List;

import io.syndesis.common.model.ListResult;
import io.syndesis.server.endpoint.util.test.person.TestPerson;
import io.syndesis.server.endpoint.util.test.person.TestPersonInterface;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class ReflectiveFiltererTest {

    List<TestPersonInterface> data;

    @Before
    public void setup(){
        data = TestPerson.getTestData();
    }

    @Test
    public void filterNoQuery() {
        ReflectiveFilterer<TestPersonInterface> filter = new ReflectiveFilterer<>(TestPersonInterface.class, FilterOptionsParser.fromString(""));
        ListResult<TestPersonInterface> result = filter.apply(ListResult.of(data));
        assertThat(result).hasSize(4);
    }

    @Test
    public void filterWithQueryString() {
        ReflectiveFilterer<TestPersonInterface> filter = new ReflectiveFilterer<>(TestPersonInterface.class, FilterOptionsParser.fromString("FirstName=Werner"));
        ListResult<TestPersonInterface> result = filter.apply(ListResult.of(data));
        assertThat(result).hasSize(1);
    }

    @Test
    public void filterWithQueryNoMatchingResults() {
        ReflectiveFilterer<TestPersonInterface> filter = new ReflectiveFilterer<>(TestPersonInterface.class, FilterOptionsParser.fromString("FirstName=Pasquale"));
        ListResult<TestPersonInterface> result = filter.apply(ListResult.of(data));
        assertThat(result).hasSize(0);
    }

    @Test
    public void filterWithQueryPrimitive() {
        ReflectiveFilterer<TestPersonInterface> filter = new ReflectiveFilterer<>(TestPersonInterface.class, FilterOptionsParser.fromString("BirthYear=1901"));
        ListResult<TestPersonInterface> result = filter.apply(ListResult.of(data));
        assertThat(result).hasSize(1);
    }

    @Test
    public void filterWithQueryEnum() {
        ReflectiveFilterer<TestPersonInterface> filter = new ReflectiveFilterer<>(TestPersonInterface.class, FilterOptionsParser.fromString("country=UK"));
        ListResult<TestPersonInterface> result = filter.apply(ListResult.of(data));
        assertThat(result).hasSize(1);
    }

    @Test
    public void filterWithQueryBoolean() {
        ReflectiveFilterer<TestPersonInterface> filter = new ReflectiveFilterer<>(TestPersonInterface.class, FilterOptionsParser.fromString("nobel=true"));
        ListResult<TestPersonInterface> result = filter.apply(ListResult.of(data));
        assertThat(result).hasSize(2);
    }

    @Test
    public void filterWithQueryOptional() {
        ReflectiveFilterer<TestPersonInterface> filter = new ReflectiveFilterer<>(TestPersonInterface.class, FilterOptionsParser.fromString("university=Cambridge"));
        ListResult<TestPersonInterface> result = filter.apply(ListResult.of(data));
        assertThat(result).hasSize(1);
    }

    @Test
    public void filterWithQueryMulti() {
        ReflectiveFilterer<TestPersonInterface> filter = new ReflectiveFilterer<>(TestPersonInterface.class, FilterOptionsParser.fromString("nobel=true,country=US"));
        ListResult<TestPersonInterface> result = filter.apply(ListResult.of(data));
        assertThat(result).hasSize(1);
    }

    @Test
    public void invalidQueryField() {
        assertThatExceptionOfType(IllegalArgumentException.class)
            .isThrownBy(() -> {new ReflectiveFilterer<>(TestPersonInterface.class, FilterOptionsParser.fromString("MissingField=XYZ"));})
            .withMessage("Cannot find field MissingField in io.syndesis.server.endpoint.util.test.person.TestPersonInterface as field");
    }

}
