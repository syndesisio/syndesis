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
package io.syndesis.server.endpoint.v1.handler.support;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import static org.junit.Assert.assertEquals;

/**
 * @author roland
 * @since 29.08.17
 */
public class RegexBasedMasqueradeReaderTest {
    @Test
    public void basicFilter() throws IOException {
        String myString = "key1=val1,password=SECRET,key2=val2";
        String pattern = "(?<=password)[:=](\\w+)";
        BufferedReader rd = new BufferedReader(new StringReader(myString));
        rd = new BufferedReader(new RegexBasedMasqueradeReader(rd, pattern));
        String s = rd.readLine();
        assertEquals("key1=val1,password=******,key2=val2", s);
        rd.close();

        myString = "key1=val1,password:SECRET,key2=val2";
        rd = new BufferedReader(new StringReader(myString));
        rd = new BufferedReader(new RegexBasedMasqueradeReader(rd, pattern));
        s = rd.readLine();
        assertEquals("key1=val1,password:******,key2=val2", s);
        rd.close();
    }

    @Test
    public void emptyString() throws IOException {
        String myString = "";
        String pattern = "(?<=password)[:=](\\w+)";
        BufferedReader rd = new BufferedReader(new StringReader(myString));
        rd = new BufferedReader(new RegexBasedMasqueradeReader(rd, pattern));
        String s = rd.readLine();
        assertEquals(null, s);
        rd.close();

    }

    @Test
    public void multipleOccurencies() throws IOException {
        String myString = "password=SECRET1 password=SECRET2";
        String pattern = "(?<=password)[:=](\\w+)";
        BufferedReader rd = new BufferedReader(new StringReader(myString));
        rd = new BufferedReader(new RegexBasedMasqueradeReader(rd, pattern));
        String s = rd.readLine();
        assertEquals("password=******* password=*******", s);
        rd.close();

    }


}
