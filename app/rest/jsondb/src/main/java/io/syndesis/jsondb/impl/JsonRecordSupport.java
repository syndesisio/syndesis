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
package io.syndesis.jsondb.impl;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import io.syndesis.jsondb.JsonDBException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
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

    public static void jsonStreamToRecords(HashSet<String> indexes, String dbPath, InputStream is, Consumer<JsonRecord> consumer) throws IOException {
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

    public static void jsonStreamToRecords(HashSet<String> indexes, JsonParser jp, String path, Consumer<JsonRecord> consumer) throws IOException {
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
                consumer.accept(JsonRecord.of(currentPath, "", nextToken.id(), indexFieldValue(indexes, currentPath)));
                if( inArray ) {
                    arrayIndex++;
                } else {
                    return;
                }
            } else if (nextToken.isScalarValue()) {
                if (inArray) {
                    currentPath = path + toArrayIndexPath(arrayIndex) + "/";
                }
                consumer.accept(JsonRecord.of(currentPath, jp.getValueAsString(), nextToken.id(), indexFieldValue(indexes, currentPath)));
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

    private static String indexFieldValue(HashSet<String> indexes, String path) {
        Matcher matcher = INDEX_EXTRACTOR_PATTERN.matcher(path);
        if( !matcher.matches() ) {
            return null;
        }

        String idx = matcher.replaceAll("$1/$2");
        if( !indexes.contains(idx) ) {
            return null;
        }

        return idx;
    }

    private static String toArrayIndexPath(int idx) {
        // todo: encode the idx using something like http://www.zanopha.com/docs/elen.pdf
        // so we get lexicographic ordering.
        return toLexSortableString(idx, '[');
    }

    protected static int toArrayIndex(String value) {
        return fromLexSortableStringToInt(value, '[');
    }

    /**
     * Based on:
     * http://www.zanopha.com/docs/elen.pdf
     */
    static String toLexSortableString(int value, char marker) {
        ArrayList<String> seqs = new ArrayList<String>();

        String seq = Integer.toString(value);
        seqs.add(seq);
        while (seq.length() > 1) {
            seq = Integer.toString(seq.length());
            seqs.add(seq);
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < seqs.size(); i++) {
            builder.append(marker);
        }
        for (int i = seqs.size() - 1; i >= 0; i--) {
            builder.append(seqs.get(i));
        }
        return builder.toString();
    }

    static int fromLexSortableStringToInt(String value, char marker) {
        // Trim the initial markers.
        String remaining = value.replaceFirst("^" + Pattern.quote(String.valueOf(marker)) + "+", "");

        int rc = 1;
        while (!remaining.isEmpty()) {
            String x = remaining.substring(0, rc);
            remaining = remaining.substring(rc);
            rc = Integer.parseInt(x);
        }

        return rc;
    }

}
