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
package io.syndesis.server.jsondb.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.syndesis.server.jsondb.JsonDBException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.core.JsonToken.END_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.END_OBJECT;
import static com.fasterxml.jackson.core.JsonToken.FIELD_NAME;
import static com.fasterxml.jackson.core.JsonToken.START_ARRAY;
import static com.fasterxml.jackson.core.JsonToken.VALUE_NULL;

/**
 * Helper methods for converting between JsonRecord lists and Json
 */
@SuppressWarnings({"PMD.GodClass", "PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity"})
public final class JsonRecordSupport {

    public static final Pattern INTEGER_PATTERN = Pattern.compile("^\\d+$");
    public static final Pattern INDEX_EXTRACTOR_PATTERN = Pattern.compile("^(.+)/[^/]+/([^/]+)/$");

    public static final char NULL_VALUE_PREFIX = '\u0000';
    public static final char FALSE_VALUE_PREFIX = '\u0001';
    public static final char TRUE_VALUE_PREFIX = '\u0002';
    public static final char NUMBER_VALUE_PREFIX = '[';
    public static final char NEG_NUMBER_VALUE_PREFIX = '-';
    public static final char STRING_VALUE_PREFIX = '`';
    public static final char ARRAY_VALUE_PREFIX = NUMBER_VALUE_PREFIX;


    static class PathPart {
        private final String path;

        private int idx;

        PathPart(String path, boolean array) {
            this.path = path;
            this.idx = array ? 0 : -1;
        }

        public String getPath() {
            return path;
        }

        public boolean isArray() {
            return idx >= 0;
        }

        public int getIdx() {
            return idx;
        }

        public void incrementIdx() {
            idx++;
        }
    }

    private JsonRecordSupport() {
        // utility class
    }

    public static void jsonStreamToRecords(Set<String> indexes, String dbPath, InputStream is, Consumer<JsonRecord> consumer) throws IOException {
        try (JsonParser jp = new JsonFactory().createParser(is)) {
            jsonStreamToRecords(indexes, jp, dbPath, consumer);

            JsonToken jsonToken = jp.nextToken();
            if (jsonToken != null) {
                throw new JsonParseException(jp, "Document did not terminate as expected.");
            }
        }
    }

    public static String convertToDBPath(String base) {
        String value = Arrays.stream(base.split("/")).filter(x -> !x.isEmpty()).map(x ->
            INTEGER_PATTERN.matcher(validateKey(x)).matches() ? toArrayIndexPath(Integer.parseInt(x)) : x
        ).collect(Collectors.joining("/"));
        return Strings.suffix(Strings.prefix(value, "/"), "/");
    }

    public static String validateKey(String key) {
        if( key.chars().anyMatch(x -> { switch(x){
            case '.':
            case '%':
            case '$':
            case '#':
            case '[':
            case ']':
            case '/':
            case 127:
                return true;
            default:
                if( 0 < x &&  x < 32) {
                    return true;
                }
                return false;
        }})) {
            throw new JsonDBException("Invalid key. Cannot contain ., %, $, #, [, ], /, or ASCII control characters 0-31 or 127. Key: "+key);
        }
        if( key.length() > 768 ) {
            throw new JsonDBException("Invalid key. Key cannot ben longer than 768 characters. Key: "+key);
        }
        return key;
    }

    public static void jsonStreamToRecords(Set<String> indexes, JsonParser jp, String path, Consumer<JsonRecord> consumer) throws IOException {
        boolean inArray = false;
        int arrayIndex = 0;
        while (true) {
            JsonToken nextToken = jp.nextToken();

            String currentPath = path;

            if (nextToken == FIELD_NAME) {
                if (inArray) {
                    currentPath = path + toArrayIndexPath(arrayIndex) + "/";
                }
                jsonStreamToRecords(indexes, jp, currentPath + validateKey(jp.getCurrentName()) + "/", consumer);
            } else if (nextToken == VALUE_NULL) {
                if (inArray) {
                    currentPath = path + toArrayIndexPath(arrayIndex) + "/";
                }
                consumer.accept(JsonRecord.of(currentPath, String.valueOf(NULL_VALUE_PREFIX), "null", indexFieldValue(indexes, currentPath)));
                if( inArray ) {
                    arrayIndex++;
                } else {
                    return;
                }
            } else if (nextToken.isScalarValue()) {
                if (inArray) {
                    currentPath = path + toArrayIndexPath(arrayIndex) + "/";
                }

                String value = jp.getValueAsString();
                String ovalue = null;

                if( nextToken == JsonToken.VALUE_STRING ) {
                    value = STRING_VALUE_PREFIX + value; //NOPMD
                } else if( nextToken == JsonToken.VALUE_NUMBER_INT || nextToken == JsonToken.VALUE_NUMBER_FLOAT ) {
                    ovalue = value; // hold on to the original number in th ovalue field.
                    value = toLexSortableString(value); // encode it so we can lexically sort.
                } else if( nextToken == JsonToken.VALUE_TRUE ) {
                    ovalue = value;
                    value = String.valueOf(TRUE_VALUE_PREFIX);
                } else if( nextToken == JsonToken.VALUE_FALSE ) {
                    ovalue = value;
                    value = String.valueOf(FALSE_VALUE_PREFIX);
                }

                consumer.accept(JsonRecord.of(currentPath, value, ovalue, indexFieldValue(indexes, currentPath)));
                if( inArray ) {
                    arrayIndex++;
                } else {
                    return;
                }
            } else if (nextToken == END_OBJECT) {
                if( inArray ) {
                    arrayIndex++;
                } else {
                    return;
                }
            } else if (nextToken == START_ARRAY) {
                inArray = true;
            } else if (nextToken == END_ARRAY) {
                return;
            }
        }
    }

    private static String indexFieldValue(Set<String> indexes, String path) {
        Matcher matcher = INDEX_EXTRACTOR_PATTERN.matcher(path);
        if( !matcher.matches() ) {
            return null;
        }

        String idx = matcher.replaceAll("$1/#$2");
        if( !indexes.contains(idx) ) {
            return null;
        }

        return idx;
    }

    private static String toArrayIndexPath(int idx) {
        // todo: encode the idx using something like http://www.zanopha.com/docs/elen.pdf
        // so we get lexicographic ordering.
        return toLexSortableString(idx);
    }

    static int toArrayIndex(String value) {
        return fromLexSortableStringToInt(value);
    }

    public static String toLexSortableString(long value) {
        return toLexSortableString(Long.toString(value));
    }

    public static String toLexSortableString(int value) {
        return toLexSortableString(Integer.toString(value));
    }

    /**
     * Based on:
     * http://www.zanopha.com/docs/elen.pdf
     */
    @SuppressWarnings("PMD.NPathComplexity")
    public static String toLexSortableString(final String value) {

        String seq = value;
        char prefix = NUMBER_VALUE_PREFIX;
        if( seq.startsWith("-") ) {
            prefix = NEG_NUMBER_VALUE_PREFIX;
            seq = seq.substring(1);
        }

        String suffix = null;
        int dot = seq.indexOf('.');
        if( dot >= 0 ) {
            suffix = seq.substring(dot+1);
            seq = seq.substring(0, dot);
        }

        ArrayList<String> seqs = new ArrayList<String>();
        seqs.add(seq);
        while (seq.length() > 1) {
            seq = Integer.toString(seq.length());
            seqs.add(seq);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < seqs.size(); i++) {
            builder.append(prefix);
        }
        for (int i = seqs.size() - 1; i >= 0; i--) {
            builder.append(seqs.get(i));
        }

        if( suffix!=null ) {
            builder.append(suffix);
            if( prefix == NEG_NUMBER_VALUE_PREFIX ) {
                builder.append(NUMBER_VALUE_PREFIX);
            } else {
                builder.append(NEG_NUMBER_VALUE_PREFIX);
            }
        }

        String rc = builder.toString();
        if( prefix == NEG_NUMBER_VALUE_PREFIX ) {
            char[] chars = rc.toCharArray();
            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                if( '0' <= c && c <= '9') {
                    chars[i] = (char) ('9' - (c - '0'));
                }
            }
            rc = new String(chars);
        }
        return rc;
    }

    static int fromLexSortableStringToInt(String value) {
        // Trim the initial markers.
        String remaining = value.replaceFirst("^" + Pattern.quote(String.valueOf(NUMBER_VALUE_PREFIX)) + "+", "");

        int rc = 1;
        while (!remaining.isEmpty()) {
            String x = remaining.substring(0, rc);
            remaining = remaining.substring(rc);
            rc = Integer.parseInt(x);
        }
        return rc;
    }

}
