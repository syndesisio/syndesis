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
package io.syndesis.server.runtime.swagger;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessorType;

import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.PrimitiveType;
import io.syndesis.common.util.json.JsonUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.introspect.BeanPropertyDefinition;

public class ModelConverter extends ModelResolver {

    public ModelConverter() {
        super(JsonUtils.copyObjectMapperConfiguration());

        // see https://github.com/swagger-api/swagger-core/issues/3443
        PrimitiveType.customClasses().put(java.time.Instant.class.getName(), PrimitiveType.LONG);
    }

    @Override
    protected boolean ignore(final Annotated member, final XmlAccessorType xmlAccessorTypeAnnotation, final String propName,
        final Set<String> propertiesToIgnore) {
        return ignoreConsideringValue(member) && super.ignore(member, xmlAccessorTypeAnnotation, propName, propertiesToIgnore);
    }

    @Override
    protected boolean ignore(final Annotated member, final XmlAccessorType xmlAccessorTypeAnnotation, final String propName,
        final Set<String> propertiesToIgnore, final BeanPropertyDefinition propDef) {

        return ignoreConsideringValue(member) && super.ignore(member, xmlAccessorTypeAnnotation, propName, propertiesToIgnore, propDef);
    }

    private static boolean ignoreConsideringValue(final Annotated member) {
        final JsonIgnore ignore = member.getAnnotation(JsonIgnore.class);

        return ignore == null || ignore.value();
    }
}
