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

import static io.syndesis.server.jsondb.impl.JsonRecordSupport.ARRAY_VALUE_PREFIX;
import static io.syndesis.server.jsondb.impl.JsonRecordSupport.NUMBER_VALUE_PREFIX;

import java.io.Closeable;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonStreamContext;

import io.syndesis.server.jsondb.GetOptions;
import io.syndesis.server.jsondb.JsonDBException;

/**
 * Converts a stream of JsonRecords to json sent to a OutputStream.
 */
@SuppressWarnings("PMD.GodClass")
class JsonRecordConsumer implements Consumer<JsonRecord>, Closeable {

    private final String base;
    private final JsonGenerator jg;
    private final OutputStream output;
    private final GetOptions options;
    @SuppressWarnings({"JdkObsolete", "PMD.LooseCoupling"})
    private final LinkedList<JsonRecordSupport.PathPart> currentPath = new LinkedList<>();
    private final Set<String> shallowObjects = new LinkedHashSet<>();
    private int entriesAdded;
    private boolean closed;
    private String currentRootField;

    JsonRecordConsumer(String base, OutputStream output, GetOptions options) throws IOException {
        this.base = base;
        this.output = output;
        try {
            this.options = options.clone();
        } catch (CloneNotSupportedException e) {
            throw new IOException(e);
        }

        if (this.options.callback() != null) {
            String backack = this.options.callback() + "(";
            output.write(backack.getBytes(StandardCharsets.UTF_8));
        }
        this.jg = new JsonFactory().createGenerator(output);
        if (options.prettyPrint()) {
            jg.useDefaultPrettyPrinter();
        }
    }

    @Override
    @SuppressWarnings("PMD.CyclomaticComplexity")
    public void accept(JsonRecord record) {
        try {
            String path = record.getPath();
            path = Strings.trimSuffix(path.substring(base.length()), "/");

            // Lets see how much of the path we match compared to
            // when we last got called.
            List<String> newPath = new ArrayList<>(Arrays.asList(path.split("/")));
            if (newPath.size() == 1 && newPath.get(0).isEmpty()) {
                newPath.clear();
            }

            // Handle limit options.
            if( this.options.limitToFirst() !=null ) {
                if( !newPath.get(0).equals(currentRootField) ) {
                    this.entriesAdded++;
                    currentRootField = newPath.get(0);
                }
                if( this.entriesAdded > this.options.limitToFirst() ) {
                    close();
                    return;
                }
            }

            // should we skip over deep records?
            if (this.options.depth()!=null && this.options.depth() < newPath.size() ) {
                shallowObjects.add(newPath.get(0));
                return;
            }

            int pathMatches = getPathMatches(newPath);

            // we might need to close objects down...
            closeDownStructs(pathMatches);
            // or open some new ones up...
            openUpStructs(newPath, pathMatches);

            if (!currentPath.isEmpty() && jg.getOutputContext().inArray()) {
                JsonRecordSupport.PathPart pathPart = currentPath.getLast();
                String last = newPath.get(newPath.size() - 1);
                int idx = JsonRecordSupport.toArrayIndex(last);
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
            boolean array = part.charAt(0) == ARRAY_VALUE_PREFIX;

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
                boolean nextArray = nextPart.charAt(0) == ARRAY_VALUE_PREFIX;

                JsonRecordSupport.PathPart pathPart = new JsonRecordSupport.PathPart(part, nextArray);
                currentPath.add(pathPart);

                if (nextPart.charAt(0) == ARRAY_VALUE_PREFIX) {
                    jg.writeStartArray();

                    int idx = JsonRecordSupport.toArrayIndex(nextPart);
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
            JsonRecordSupport.PathPart lastPart = currentPath.get(i);
            if (lastPart.getPath().equals(newPath.get(i)) ||
                (lastPart.isArray() && newPath.get(i).charAt(0) == ARRAY_VALUE_PREFIX)) { // NOPMD, false positive
                pathMatches++;
            } else {
                break;
            }
        }
        return pathMatches;
    }

    @Override
    public void close() throws IOException{
        if( closed ) {
            return;
        }
        if (!shallowObjects.isEmpty()) {
            JsonStreamContext oc = jg.getOutputContext();
            if (!oc.inObject()) {
                jg.writeStartObject();
            }
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
        if (options.callback() != null) {
            output.write(")".getBytes(StandardCharsets.UTF_8));
        }
        jg.close();
        closed = true;
    }



    private void writeValue(JsonRecord value) throws IOException {
        switch (value.getValue().charAt(0)) {
            case JsonRecordSupport.STRING_VALUE_PREFIX:
                jg.writeString(value.getValue().substring(1));
                break;
            case JsonRecordSupport.NULL_VALUE_PREFIX:
                jg.writeNull();
                break;
            case JsonRecordSupport.NEG_NUMBER_VALUE_PREFIX:
            case NUMBER_VALUE_PREFIX:
                jg.writeNumber(value.getOValue());
                break;
            case JsonRecordSupport.TRUE_VALUE_PREFIX:
                jg.writeBoolean(true);
                break;
            case JsonRecordSupport.FALSE_VALUE_PREFIX:
                jg.writeBoolean(false);
                break;
            default:
                break;
        }
    }

    public boolean isClosed() {
        return closed;
    }

}
