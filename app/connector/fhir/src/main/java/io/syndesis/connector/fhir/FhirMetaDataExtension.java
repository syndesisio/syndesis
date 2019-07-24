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
package io.syndesis.connector.fhir;

import ca.uhn.fhir.context.FhirVersionEnum;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class FhirMetaDataExtension extends AbstractMetaDataExtension {

    FhirMetaDataExtension(CamelContext context) {
        super(context);
    }

    @Override
    public Optional<MetaData> meta(Map<String, Object> parameters) {
        FhirVersionEnum fhirVersionEnum = ConnectorOptions.extractOptionAndMap(
            parameters, "fhirVersion",
            FhirVersionEnum::valueOf, FhirVersionEnum.DSTU3);

        Set<String> resources = getResources(fhirVersionEnum);

        return Optional
                    .of(MetaDataBuilder.on(getCamelContext()).withAttribute(MetaData.CONTENT_TYPE, "text/plain")
                        .withAttribute(MetaData.JAVA_TYPE, String.class).withPayload(resources).build());
    }

    public static Set<String> getResources(FhirVersionEnum fhirVersion) {
        if (FhirVersionEnum.DSTU3.equals(fhirVersion)) {
            return toSet(org.hl7.fhir.dstu3.model.ResourceType.values());
        } else {
            throw new IllegalArgumentException(fhirVersion + " is not among supported FHIR versions: DSTU3");
        }
    }

    @SuppressWarnings({"PMD.UseVarargs"})
    private static Set<String> toSet(Object[] values) {
        Set<String> result = new HashSet<>();
        for (Object value: values) {
            result.add(String.valueOf(value));
        }

        return result;
    }
}
