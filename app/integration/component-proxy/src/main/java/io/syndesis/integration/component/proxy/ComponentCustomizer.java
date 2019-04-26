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
import org.apache.camel.Component;
import org.apache.camel.TypeConverter;

public interface ComponentCustomizer<T extends Component> {
    /**
     * Customize the specified {@link Component}. The customizer has to
     * remove remove customizer specific properties once they are consumed.
     *
     * @param component the component to customize
     * @param options the component options
     */
    void customize(T component, Map<String, Object> options);


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
    default <T> void consumeOption(CamelContext camelContext, Map<String, Object> options, String name, Class<T> type, Consumer<T> consumer) {
        Object val = options.remove(name);

        if (val != null) {
            try {
                if (val instanceof String) {
                    val = camelContext.resolvePropertyPlaceholders((String)val);
                }

                if (type.isInstance(val)) {
                    consumer.accept(type.cast(val));
                } else {
                    TypeConverter converter = camelContext.getTypeConverter();
                    consumer.accept(
                        converter.mandatoryConvertTo(type, val)
                    );
                }
            } catch (Exception e) {
                throw new IllegalStateException("Unable to resolve or convert property named: " + name, e);
            }
        }
    }
}
