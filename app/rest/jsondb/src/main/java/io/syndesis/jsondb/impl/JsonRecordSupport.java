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
package io.syndesis.jsondb.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.JsonTokenId;

import io.syndesis.jsondb.GetOptions;
import io.syndesis.jsondb.JsonDBException;

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

    /* default */ static class PathPart {
        private final String path;

        private int idx;

        /* default */ PathPart(String path, boolean array) {
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

    public static Consumer<JsonRecord> recordsToJsonStream(String dbPath, OutputStream output, GetOptions options) throws IOException {
        return new JsonRecordConsumer(dbPath, output, options);
    }

    public static void jsonStreamToRecords(String dbPath, InputStream is, Consumer<JsonRecord> consumer) throws IOException {
        try (JsonParser jp = new JsonFactory().createParser(is)) {
            jsonStreamToRecords(jp, dbPath, consumer);

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

    private static String validateKey(String key) {
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

    public static void jsonStreamToRecords(JsonParser jp, String path, Consumer<JsonRecord> consumer) throws IOException {
        boolean inArray = false;
        int arrayIndex = 0;
        while (true) {
            JsonToken nextToken = jp.nextToken();

            String currentPath = path;

            if (nextToken == FIELD_NAME) {
                if (inArray) {
                    currentPath = path + toArrayIndexPath(arrayIndex) + "/";
                }
                jsonStreamToRecords(jp, currentPath + validateKey(jp.getCurrentName()) + "/", consumer);
            } else if (nextToken == VALUE_NULL) {
                if (inArray) {
                    currentPath = path + toArrayIndexPath(arrayIndex) + "/";
                }
                consumer.accept(JsonRecord.of(currentPath, "", nextToken.id()));
                if( inArray ) {
                    arrayIndex++;
                } else {
                    return;
                }
            } else if (nextToken.isScalarValue()) {
                if (inArray) {
                    currentPath = path + toArrayIndexPath(arrayIndex) + "/";
                }
                consumer.accept(JsonRecord.of(currentPath, jp.getValueAsString(), nextToken.id()));
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

    private static String toArrayIndexPath(int idx) {
        // todo: encode the idx using something like http://www.zanopha.com/docs/elen.pdf
        // so we get lexicographic ordering.
        return toLexSortableString(idx, '[');
    }

    private static int toArrayIndex(String value) {
        return fromLexSortableStringToInt(value, '[');
    }

    /**
     * Based on:
     * http://www.zanopha.com/docs/elen.pdf
     */
    /* default */ static String toLexSortableString(int value, char marker) {
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

    /* default */ static int fromLexSortableStringToInt(String value, char marker) {
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

    /* default */ static class JsonRecordConsumer implements Consumer<JsonRecord> {

        private final String base;
        private final JsonGenerator jg;
        private final OutputStream output;
        private final GetOptions options;
        @SuppressWarnings("JdkObsolete")
        private final LinkedList<PathPart> currentPath = new LinkedList<>();
        private final Set<String> shallowObjects = new LinkedHashSet<>();

        /* default */ JsonRecordConsumer(String base, OutputStream output, GetOptions options) throws IOException {
            this.base = base;
            this.output = output;
            try {
                this.options = options.clone();
            } catch (CloneNotSupportedException e) {
                throw new IOException(e);
            }

            if( this.options.callback()!=null ) {
                String backack = this.options.callback() + "(";
                output.write(backack.getBytes(StandardCharsets.UTF_8));
            }
            this.jg = new JsonFactory().createGenerator(output);
            if( options.prettyPrint() ) {
                jg.useDefaultPrettyPrinter();
            }
        }

        @Override
        public void accept(JsonRecord record) {
            try {
                // Handle the end end of stream..
                if (record == null) {
                    close();
                    return;
                }

                String path = record.getPath();
                path = Strings.trimSuffix(path.substring(base.length()), "/");

                // Lets see how much of the path we match compared to
                // when we last got called.
                List<String> newPath = new ArrayList<>(Arrays.asList(path.split("/")));
                if (newPath.size() == 1 && newPath.get(0).isEmpty()) {
                    newPath.clear();
                }

                // should we skip over deep records?
                if( this.options.shallow() && newPath.size() > 1 ) {
                    shallowObjects.add(newPath.get(0));
                    return;
                }

                int pathMatches = getPathMatches(newPath);

                // we might need to close objects down...
                closeDownStructs(pathMatches);
                // or open some new ones up...
                openUpStructs(newPath, pathMatches);

                if (!currentPath.isEmpty() && jg.getOutputContext().inArray()) {
                    PathPart pathPart = currentPath.getLast();
                    String last = newPath.get(newPath.size() - 1);
                    int idx = toArrayIndex(last);
                    while (idx > pathPart.getIdx()) {
                        // to track the nulls that lead up to the next value.
                        pathPart.incrementIdx();
                        jg.writeNull();
                    }
                    // to track the value that we are about to write.
                    pathPart.incrementIdx();
                }

                writeValue(record);

            } catch (IOException e) {
                throw new JsonDBException(e);
            }
        }

        private void openUpStructs(List<String> newPath, int pathMatches) throws IOException {
            int count;

            // we might need to open up objects...
            count = newPath.size();
            for (int i = pathMatches; i < count; i++) {
                String part = newPath.get(i);
                boolean array = part.startsWith("[");

                if (array) {
                    if (jg.getOutputContext().inRoot()) {
                        jg.writeStartArray();
                    }
                } else {
                    if (jg.getOutputContext().inRoot()) {
                        jg.writeStartObject();
                    }
                    jg.writeFieldName(part);
                }

                if (i + 1 < count) {
                    String nextPart = newPath.get(i + 1);
                    boolean nextArray = nextPart.startsWith("[");

                    PathPart pathPart = new PathPart(part, nextArray);
                    currentPath.add(pathPart);

                    if (nextPart.startsWith("[")) {
                        jg.writeStartArray();

                        int idx = toArrayIndex(nextPart);
                        while (idx > pathPart.getIdx()) {
                            pathPart.incrementIdx();
                            jg.writeNull();
                        }

                    } else {
                        jg.writeStartObject();
                    }
                }
            }
        }

        private void closeDownStructs(int pathMatches) throws IOException {
            int count = currentPath.size() - pathMatches;
            for (int i = 0; i < count; i++) {
                if (currentPath.removeLast().isArray()) {
                    jg.writeEndArray();
                } else {
                    jg.writeEndObject();
                }
            }
        }

        private int getPathMatches(List<String> newPath) {
            int pathMatches = 0;
            for (int i = 0; i < currentPath.size() && i < newPath.size(); i++) {
                PathPart lastPart = currentPath.get(i);
                if (lastPart.getPath().equals(newPath.get(i)) ||
                    (lastPart.isArray() && newPath.get(i).startsWith("["))) {
                    pathMatches++;
                } else {
                    break;
                }
            }
            return pathMatches;
        }

        private void close() throws IOException {
            if ( !shallowObjects.isEmpty() ) {
                for (String o : shallowObjects) {
                    jg.writeFieldName(o);
                    jg.writeBoolean(true);
                }
            }
            while (jg.getOutputContext().getParent() != null) {
                JsonStreamContext oc = jg.getOutputContext();
                if (oc.inObject()) {
                    jg.writeEndObject();
                } else if (oc.inArray()) {
                    jg.writeEndArray();
                }
            }
            jg.flush();
            if( options.callback()!=null ) {
                output.write(")".getBytes(StandardCharsets.UTF_8));
            }
            jg.close();
        }

        private void writeValue(JsonRecord value) throws IOException {
            switch (value.getKind()) {
                case JsonTokenId.ID_STRING:
                    jg.writeString(value.getValue());
                    break;
                case JsonTokenId.ID_NULL:
                    jg.writeNull();
                    break;
                case JsonTokenId.ID_NUMBER_FLOAT:
                case JsonTokenId.ID_NUMBER_INT:
                    jg.writeNumber(value.getValue());
                    break;
                case JsonTokenId.ID_TRUE:
                    jg.writeBoolean(true);
                    break;
                case JsonTokenId.ID_FALSE:
                    jg.writeBoolean(false);
                    break;
                default:
            }
        }
    }
}
