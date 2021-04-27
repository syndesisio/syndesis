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

package io.syndesis.common.util.json;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import com.fasterxml.jackson.databind.ObjectReader;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class JsonUtilsTest {

    @Test
    public void isJson() {
        assertThat(JsonUtils.isJson(null)).isFalse();
        assertThat(JsonUtils.isJson("")).isFalse();
        assertThat(JsonUtils.isJson("{}")).isTrue();
        assertThat(JsonUtils.isJson("{\"foo\": \"bar\"}")).isTrue();
        assertThat(JsonUtils.isJson("  {\"foo\": \"bar\"}  ")).isTrue();
        assertThat(JsonUtils.isJson("\n{\"foo\": \"bar\"}\n")).isTrue();
        assertThat(JsonUtils.isJson("[]")).isTrue();
        assertThat(JsonUtils.isJson("[{\"foo\": \"bar\"}]")).isTrue();
        assertThat(JsonUtils.isJson("  [{\"foo\": \"bar\"}]  ")).isTrue();
        assertThat(JsonUtils.isJson("\n[{\"foo\": \"bar\"}]\n")).isTrue();
    }

    @Test
    public void isJsonObject() {
        assertThat(JsonUtils.isJsonObject(null)).isFalse();
        assertThat(JsonUtils.isJsonObject("")).isFalse();
        assertThat(JsonUtils.isJsonObject("{}")).isTrue();
        assertThat(JsonUtils.isJsonObject("{\"foo\": \"bar\"}")).isTrue();
        assertThat(JsonUtils.isJsonObject("  {\"foo\": \"bar\"}  ")).isTrue();
        assertThat(JsonUtils.isJsonObject("\n{\"foo\": \"bar\"}\n")).isTrue();
        assertThat(JsonUtils.isJsonObject("[]")).isFalse();
    }

    @Test
    public void isJsonArray() {
        assertThat(JsonUtils.isJsonArray(null)).isFalse();
        assertThat(JsonUtils.isJsonArray("")).isFalse();
        assertThat(JsonUtils.isJsonArray("{}")).isFalse();
        assertThat(JsonUtils.isJsonArray("[]")).isTrue();
        assertThat(JsonUtils.isJsonArray("[{\"foo\": \"bar\"}]")).isTrue();
        assertThat(JsonUtils.isJsonArray("  [{\"foo\": \"bar\"}]  ")).isTrue();
        assertThat(JsonUtils.isJsonArray("\n[{\"foo\": \"bar\"}]\n")).isTrue();
    }

    @Test
    public void arrayToJsonBeans() throws IOException {
        ObjectReader reader = JsonUtils.reader();
        assertThat(JsonUtils.arrayToJsonBeans(reader.readTree("{}"))).isEqualTo(Collections.emptyList());
        assertThat(JsonUtils.arrayToJsonBeans(reader.readTree("{\"foo\": \"bar\"}"))).isEqualTo(Collections.emptyList());
        assertThat(JsonUtils.arrayToJsonBeans(reader.readTree("[]"))).isEqualTo(Collections.emptyList());
        assertThat(JsonUtils.arrayToJsonBeans(reader.readTree("[{\"foo\": \"bar\"}]"))).isEqualTo(Collections.singletonList("{\"foo\":\"bar\"}"));
        assertThat(JsonUtils.arrayToJsonBeans(reader.readTree("[{\"foo1\": \"bar1\"}, {\"foo2\": \"bar2\"}]"))).isEqualTo(Arrays.asList("{\"foo1\":\"bar1\"}", "{\"foo2\":\"bar2\"}"));
    }

    @Test
    public void jsonBeansToArray() {
        assertThat(JsonUtils.jsonBeansToArray(Collections.emptyList())).isEqualTo("[]");
        assertThat(JsonUtils.jsonBeansToArray(Collections.singletonList("{\"foo\": \"bar\"}"))).isEqualTo("[{\"foo\": \"bar\"}]");
        assertThat(JsonUtils.jsonBeansToArray(Arrays.asList("{\"foo1\": \"bar1\"}", "{\"foo2\": \"bar2\"}"))).isEqualTo("[{\"foo1\": \"bar1\"},{\"foo2\": \"bar2\"}]");
    }
}
