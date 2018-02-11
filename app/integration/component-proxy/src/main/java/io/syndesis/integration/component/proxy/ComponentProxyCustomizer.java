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
package io.syndesis.integration.component.proxy;

import java.util.Map;
import java.util.function.Consumer;

import org.apache.camel.CamelContext;
import org.apache.camel.TypeConverter;

@FunctionalInterface
public interface ComponentProxyCustomizer {
    /**
     * Customize the specified {@link ComponentProxyComponent}. The customizer
     * has to remove remove customizer specific properties once they are consumed.
     *
     * @param component the component to customize
     * @param options the component options
     */
    void customize(ComponentProxyComponent component, Map<String, Object> options);


    // **************************
    // Helpers
    // **************************

    default void consumeOption(Map<String, Object> options, String name, Consumer<Object> consumer) {
        Object val = options.remove(name);
        if (val != null) {
            consumer.accept(val);
        }
    }

    /**
     * Removes the option with the given name and invokes the consumer performing
     * property placeholders resolution and type conversion.
     */
    @SuppressWarnings("PMD.SignatureDeclareThrowsException")
    default <T> void consumeOption(CamelContext camelContext, Map<String, Object> options, String name, Class<T> type, Consumer<T> consumer) throws Exception {
        TypeConverter converter = camelContext.getTypeConverter();
        Object val = options.remove(name);

        if (val != null) {
            if (val instanceof String) {
                val = camelContext.resolvePropertyPlaceholders((String)val);
            }

            consumer.accept(
                converter.mandatoryConvertTo(type, val)
            );
        }
    }
}
