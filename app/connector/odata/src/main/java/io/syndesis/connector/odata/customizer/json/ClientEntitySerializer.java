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
import org.apache.olingo.client.api.domain.ClientProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class ClientEntitySerializer extends StdSerializer<ClientEntity> {

    private static final long serialVersionUID = 1L;

    public ClientEntitySerializer() {
        this(null);
    }

    public ClientEntitySerializer(Class<ClientEntity> t) {
        super(t);
    }

    @Override
    public Class<ClientEntity> handledType() {
        return ClientEntity.class;
    }

    @Override
    public void serialize(ClientEntity entity, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeStartObject();
        for (ClientProperty prop : entity.getProperties()) {
            generator.writeFieldName(prop.getName());
            generator.writeObject(prop.getValue());
        }
        generator.writeEndObject();
    }
}
