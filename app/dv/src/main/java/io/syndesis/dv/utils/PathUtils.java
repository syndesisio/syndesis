/*
 * Copyright (C) 2013 Red Hat, Inc.
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
package io.syndesis.dv.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import org.springframework.data.util.Pair;

/**
 * This is a common place to put Path utility methods
 *
 *  The Path string will be composed of key=value string pairs separated by forward slash ('/')
 */
public class PathUtils {
    /**
     * The String "/"
     */
    public static final String OPTION_SEPARATOR = "/"; //$NON-NLS-1$

    /**
     * The String "="
     */
    public static final String VALUE_SEPARATOR = "="; //$NON-NLS-1$

    /**
     * Simple method parses the input path and returns a set of string {@link Properties}
     * @param path
     * @return properties object
     */
    public static List<Pair<String, String>> getOptions(String path) {
        StringTokenizer tokenizer = new StringTokenizer(path, OPTION_SEPARATOR);

        List<Pair<String, String>> props = new ArrayList<>();

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            // Now we split this token via the "="
            StringTokenizer strTkzr = new StringTokenizer(token, VALUE_SEPARATOR);
            String key = strTkzr.nextToken();
            String value = strTkzr.nextToken();
            try {
                props.add(Pair.of(URLDecoder.decode(key, "UTF-8") , URLDecoder.decode(value, "UTF-8")));
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }

        return props;

    }

}
