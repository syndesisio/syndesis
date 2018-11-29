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
import org.apache.olingo.client.api.domain.ClientEntity;
import org.apache.olingo.client.api.domain.ClientEntitySet;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ClientEntitySetSerializer extends StdSerializer<ClientEntitySet> {

    private static final long serialVersionUID = 1L;

    public ClientEntitySetSerializer() {
        this(null);
    }

    public ClientEntitySetSerializer(Class<ClientEntitySet> t) {
        super(t);
    }

    @Override
    public Class<ClientEntitySet> handledType() {
        return ClientEntitySet.class;
    }

    @Override
    public void serialize(ClientEntitySet entitySet, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        if (entitySet.getCount() != null) {
            generator.writeNumberField("count", entitySet.getCount());
        }

        generator.writeFieldName("entities");
        generator.writeStartArray();
        for (ClientEntity entity : entitySet.getEntities()) {
            generator.writeObject(entity);
        }
        generator.writeEndArray();
        generator.writeEndObject();
    }

}
