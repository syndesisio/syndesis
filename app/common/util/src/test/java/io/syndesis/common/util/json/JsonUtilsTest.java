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
import io.syndesis.common.util.Json;
import org.assertj.core.api.Assertions;
import org.junit.Test;

/**
 * @author Christoph Deppisch
 */
public class JsonUtilsTest {

    @Test
    public void isJson() {
        Assertions.assertThat(JsonUtils.isJson(null)).isFalse();
        Assertions.assertThat(JsonUtils.isJson("")).isFalse();
        Assertions.assertThat(JsonUtils.isJson("{}")).isTrue();
        Assertions.assertThat(JsonUtils.isJson("{\"foo\": \"bar\"}")).isTrue();
        Assertions.assertThat(JsonUtils.isJson("  {\"foo\": \"bar\"}  ")).isTrue();
        Assertions.assertThat(JsonUtils.isJson("\n{\"foo\": \"bar\"}\n")).isTrue();
        Assertions.assertThat(JsonUtils.isJson("[]")).isTrue();
        Assertions.assertThat(JsonUtils.isJson("[{\"foo\": \"bar\"}]")).isTrue();
        Assertions.assertThat(JsonUtils.isJson("  [{\"foo\": \"bar\"}]  ")).isTrue();
        Assertions.assertThat(JsonUtils.isJson("\n[{\"foo\": \"bar\"}]\n")).isTrue();
    }

    @Test
    public void isJsonObject() {
        Assertions.assertThat(JsonUtils.isJsonObject(null)).isFalse();
        Assertions.assertThat(JsonUtils.isJsonObject("")).isFalse();
        Assertions.assertThat(JsonUtils.isJsonObject("{}")).isTrue();
        Assertions.assertThat(JsonUtils.isJsonObject("{\"foo\": \"bar\"}")).isTrue();
        Assertions.assertThat(JsonUtils.isJsonObject("  {\"foo\": \"bar\"}  ")).isTrue();
        Assertions.assertThat(JsonUtils.isJsonObject("\n{\"foo\": \"bar\"}\n")).isTrue();
        Assertions.assertThat(JsonUtils.isJsonObject("[]")).isFalse();
    }

    @Test
    public void isJsonArray() {
        Assertions.assertThat(JsonUtils.isJsonArray(null)).isFalse();
        Assertions.assertThat(JsonUtils.isJsonArray("")).isFalse();
        Assertions.assertThat(JsonUtils.isJsonArray("{}")).isFalse();
        Assertions.assertThat(JsonUtils.isJsonArray("[]")).isTrue();
        Assertions.assertThat(JsonUtils.isJsonArray("[{\"foo\": \"bar\"}]")).isTrue();
        Assertions.assertThat(JsonUtils.isJsonArray("  [{\"foo\": \"bar\"}]  ")).isTrue();
        Assertions.assertThat(JsonUtils.isJsonArray("\n[{\"foo\": \"bar\"}]\n")).isTrue();
    }

    @Test
    public void arrayToJsonBeans() throws IOException {
        ObjectReader reader = Json.reader();
        Assertions.assertThat(JsonUtils.arrayToJsonBeans(reader.readTree("{}"))).isEqualTo(Collections.emptyList());
        Assertions.assertThat(JsonUtils.arrayToJsonBeans(reader.readTree("{\"foo\": \"bar\"}"))).isEqualTo(Collections.emptyList());
        Assertions.assertThat(JsonUtils.arrayToJsonBeans(reader.readTree("[]"))).isEqualTo(Collections.emptyList());
        Assertions.assertThat(JsonUtils.arrayToJsonBeans(reader.readTree("[{\"foo\": \"bar\"}]"))).isEqualTo(Collections.singletonList("{\"foo\":\"bar\"}"));
        Assertions.assertThat(JsonUtils.arrayToJsonBeans(reader.readTree("[{\"foo1\": \"bar1\"}, {\"foo2\": \"bar2\"}]"))).isEqualTo(Arrays.asList("{\"foo1\":\"bar1\"}", "{\"foo2\":\"bar2\"}"));
    }

    @Test
    public void jsonBeansToArray() {
        Assertions.assertThat(JsonUtils.jsonBeansToArray(Collections.emptyList())).isEqualTo("[]");
        Assertions.assertThat(JsonUtils.jsonBeansToArray(Collections.singletonList("{\"foo\": \"bar\"}"))).isEqualTo("[{\"foo\": \"bar\"}]");
        Assertions.assertThat(JsonUtils.jsonBeansToArray(Arrays.asList("{\"foo1\": \"bar1\"}", "{\"foo2\": \"bar2\"}"))).isEqualTo("[{\"foo1\": \"bar1\"},{\"foo2\": \"bar2\"}]");
    }
}