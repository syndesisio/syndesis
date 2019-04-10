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
package io.syndesis.connector.odata.customizer.json;

import java.io.IOException;
import java.util.Iterator;
import org.apache.olingo.client.api.domain.ClientCollectionValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.syndesis.common.util.StringConstants;

public class ClientCollectionValueSerializer
    extends StdSerializer<ClientCollectionValue<?>> implements StringConstants {

    private static final long serialVersionUID = 1L;

    public ClientCollectionValueSerializer() {
        this(null);
    }

    public ClientCollectionValueSerializer(Class<ClientCollectionValue<?>> t) {
        super(t);
    }

    @SuppressWarnings( "unchecked" )
    @Override
    public Class<ClientCollectionValue<?>> handledType() {
        return (Class<ClientCollectionValue<?>>) (Class<?>) ClientCollectionValue.class;
    }

    @Override
    public void serialize(ClientCollectionValue<?> value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartArray();

        Iterator<?> iterator = value.iterator();
        while (iterator.hasNext()) {
            Object element = iterator.next();
            generator.writeObject(element);
        }

        generator.writeEndArray();
    }
}
