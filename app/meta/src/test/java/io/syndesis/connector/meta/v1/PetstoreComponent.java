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
package io.syndesis.connector.meta.v1;

import java.util.Map;
import java.util.Optional;

import org.apache.camel.Endpoint;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;
import org.apache.camel.impl.DefaultComponent;

public class PetstoreComponent extends DefaultComponent {

    public PetstoreComponent(final Object payload) {
        registerExtension((MetaDataExtension) parameters -> Optional
            .of(MetaDataBuilder.on(getCamelContext()).withPayload(payload).build()));
    }

    @Override
    protected Endpoint createEndpoint(final String uri, final String remaining, final Map<String, Object> parameters)
        throws Exception {
        throw new UnsupportedOperationException();
    }
}
