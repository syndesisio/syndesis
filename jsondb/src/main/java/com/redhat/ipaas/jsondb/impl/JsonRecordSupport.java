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
package com.redhat.ipaas.jsondb.impl;

import com.fasterxml.jackson.core.*;
import com.redhat.ipaas.jsondb.GetOptions;
import com.redhat.ipaas.jsondb.JsonDBException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.fasterxml.jackson.core.JsonToken.*;

/**
 * Helper methods for converting between JsonRecord lists and Json
 */
public class JsonRecordSupport {

    private static class PathPart {
        private final String path;

        private int idx;

        public PathPart(String path, boolean array) {
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

    static public Consumer<JsonRecord> recordsToJsonStream(String dbPath, OutputStream output, GetOptions options) throws IOException {
        return new JsonRecordConsumer(dbPath, output, options);
    }

    static public void jsonStreamToRecords(String dbPath, InputStream is, Consumer<JsonRecord> consumer) throws IOException {
        JsonParser jp = new JsonFactory().createParser(is);

        jsonStreamToRecords(jp, dbPath, consumer);

        JsonToken jsonToken = jp.nextToken();
        if (jsonToken != null) {
            throw new JsonParseException(jp, "Document did not terminate as expected.");
        }
    }

    public static String convertToDBPath(String base) {
        Pattern pattern = Pattern.compile("^\\d+$");
        String value = Arrays.stream(base.split("/")).filter(x -> !x.isEmpty()).map(x ->
            pattern.matcher(x).matches() ? toArrayIndexPath(Integer.parseInt(x)) : x
        ).collect(Collectors.joining("/"));
        return Strings.suffix(Strings.prefix(value, "/"), "/");
    }

    private static void jsonStreamToRecords(JsonParser jp, String path, Consumer<JsonRecord> consumer) throws IOException {
        boolean inArray = false;
        int arrayIndex = 0;
        while (true) {
            JsonToken nextToken = jp.nextToken();

            String currentPath = path;

            if (nextToken == FIELD_NAME) {
                if (inArray) {
                    currentPath = path + toArrayIndexPath(arrayIndex) + "/";
                }
                jsonStreamToRecords(jp, currentPath + jp.getCurrentName() + "/", consumer);
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
    protected static String toLexSortableString(int value, char marker) {
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

    protected static int fromLexSortableStringToInt(String value, char marker) {
        // Trim the initial markers.
        String remaining = value.replaceFirst("^" + Pattern.quote("" + marker) + "+", "");

        int rc = 1;
        while (!remaining.isEmpty()) {
            String x = remaining.substring(0, rc);
            remaining = remaining.substring(rc);
            rc = Integer.parseInt(x);
        }

        return rc;
    }

    private static class JsonRecordConsumer implements Consumer<JsonRecord> {

        private final String base;
        private final JsonGenerator jg;
        private final OutputStream output;
        private final GetOptions options;
        private final LinkedList<PathPart> currentPath = new LinkedList<>();

        public JsonRecordConsumer(String base, OutputStream output, GetOptions options) throws IOException {
            this.base = base;
            this.output = output;
            this.options = options;

            if( options.callback().isPresent() ) {
                String backack = options.callback().get() + "(";
                output.write(backack.getBytes("UTF-8"));
            }
            this.jg = new JsonFactory().createGenerator(output);
            if( options.prettyPrint().orElse(Boolean.FALSE).booleanValue() ) {
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
                ArrayList<String> newPath = new ArrayList<>(Arrays.asList(path.split("/")));
                if (newPath.size() == 1 && newPath.get(0).isEmpty()) {
                    newPath.clear();
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

        private void openUpStructs(ArrayList<String> newPath, int pathMatches) throws IOException {
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

        private int getPathMatches(ArrayList<String> newPath) {
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
            while (jg.getOutputContext().getParent() != null) {
                JsonStreamContext oc = jg.getOutputContext();
                if (oc.inObject()) {
                    jg.writeEndObject();
                } else if (oc.inArray()) {
                    jg.writeEndArray();
                }
            }
            jg.flush();
            if( options.callback().isPresent() ) {
                output.write(")".getBytes("UTF-8"));
            }
            jg.close();
        }

        private String currentPath() {
            LinkedList<String> path = new LinkedList<>();

            JsonStreamContext oc = jg.getOutputContext();
            while (oc != null) {
                if (!oc.inRoot()) {
                    if (oc.getCurrentName() != null) {
                        path.addFirst(oc.getCurrentName());
                    } else {
                        path.addFirst("" + oc.getCurrentIndex());
                    }
                }
                oc = oc.getParent();
            }
            if (path.isEmpty()) {
                return "/";
            }
            return "/" + path.stream().collect(Collectors.joining("/")) + "/";
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
