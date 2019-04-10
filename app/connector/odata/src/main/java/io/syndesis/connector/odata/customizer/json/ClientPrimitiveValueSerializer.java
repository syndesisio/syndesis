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
import java.math.BigDecimal;
import org.apache.olingo.client.api.domain.ClientPrimitiveValue;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeException;
import org.apache.olingo.commons.api.edm.EdmPrimitiveTypeKind;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.syndesis.common.util.StringConstants;

public class ClientPrimitiveValueSerializer
    extends StdSerializer<ClientPrimitiveValue> implements StringConstants {

    private static final long serialVersionUID = 1L;

    public ClientPrimitiveValueSerializer() {
        this(null);
    }

    public ClientPrimitiveValueSerializer(Class<ClientPrimitiveValue> t) {
        super(t);
    }

    @Override
    public Class<ClientPrimitiveValue> handledType() {
        return ClientPrimitiveValue.class;
    }

    @SuppressWarnings("PMD.MissingBreakInSwitch")
    @Override
    public void serialize(ClientPrimitiveValue value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        try {
            EdmPrimitiveTypeKind typeKind = value.getTypeKind();
            switch (typeKind) {
                case Boolean:
                    generator.writeBoolean(value.toCastValue(Boolean.class));
                    break;
                case Decimal:
                    generator.writeNumber(value.toCastValue(BigDecimal.class));
                    break;
                case Double:
                    generator.writeNumber(value.toCastValue(Double.class));
                    break;
                case Single:
                case Int16:
                case Int32:
                case Int64:
                    generator.writeNumber(value.toCastValue(Integer.class));
                    break;
                case String:
                default:
                    generator.writeString(value.toString());
            }
        } catch (EdmPrimitiveTypeException e) {
            generator.writeString(value.toString());
        }
    }
}
