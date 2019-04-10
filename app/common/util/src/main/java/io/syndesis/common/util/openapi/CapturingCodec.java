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
package io.syndesis.common.util.openapi;

import java.io.IOException;
import java.util.Iterator;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.core.type.ResolvedType;
import com.fasterxml.jackson.core.type.TypeReference;

final class CapturingCodec extends ObjectCodec {

    private Object captured;

    private final ObjectCodec delegate;

    CapturingCodec(final ObjectCodec delegate) {
        this.delegate = delegate;
    }

    @Override
    public TreeNode createArrayNode() {
        final TreeNode node = delegate.createArrayNode();
        captured = node;
        return node;
    }

    @Override
    public TreeNode createObjectNode() {
        final TreeNode node = delegate.createObjectNode();
        captured = node;
        return node;
    }

    @Override
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T extends TreeNode> T readTree(final JsonParser p) throws IOException {
        final T ret = delegate.readTree(p);
        captured = ret;

        return ret;
    }

    @Override
    public <T> T readValue(final JsonParser p, final Class<T> valueType) throws IOException {
        final T ret = delegate.readValue(p, valueType);
        captured = ret;

        return ret;
    }

    @Override
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T> T readValue(final JsonParser p, final ResolvedType valueType) throws IOException {
        final T ret = delegate.readValue(p, valueType);
        captured = ret;

        return ret;
    }

    @Override
    @SuppressWarnings("TypeParameterUnusedInFormals")
    public <T> T readValue(final JsonParser p, final TypeReference<?> valueTypeRef) throws IOException {
        final T ret = delegate.readValue(p, valueTypeRef);
        captured = ret;

        return ret;
    }

    @Override
    public <T> Iterator<T> readValues(final JsonParser p, final Class<T> valueType) throws IOException {
        final Iterator<T> ret = delegate.readValues(p, valueType);
        captured = ret;

        return ret;
    }

    @Override
    public <T> Iterator<T> readValues(final JsonParser p, final ResolvedType valueType) throws IOException {
        final Iterator<T> ret = delegate.readValue(p, valueType);
        captured = ret;

        return ret;
    }

    @Override
    public <T> Iterator<T> readValues(final JsonParser p, final TypeReference<?> valueTypeRef) throws IOException {
        final Iterator<T> ret = delegate.readValue(p, valueTypeRef);
        captured = ret;

        return ret;
    }

    @Override
    public JsonParser treeAsTokens(final TreeNode n) {
        final JsonParser parser = delegate.treeAsTokens(n);
        captured = parser;
        return parser;
    }

    @Override
    public <T> T treeToValue(final TreeNode n, final Class<T> valueType) throws JsonProcessingException {
        final T value = delegate.treeToValue(n, valueType);
        captured = value;
        return value;
    }

    @Override
    public Version version() {
        return delegate.version();
    }

    @Override
    public void writeTree(final JsonGenerator gen, final TreeNode tree) throws IOException {
        captured = tree;
        delegate.writeTree(gen, tree);
    }

    @Override
    public void writeValue(final JsonGenerator gen, final Object value) throws IOException {
        delegate.writeValue(gen, value);
        captured = value;
    }

    Object captured() {
        return captured;
    }
}
