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

import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Resources;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FhirMetadataRetrieval extends ComponentMetadataRetrieval {

    /**
     * TODO: use local extension, remove when switching to camel 2.22.x
     */
    @Override
    protected MetaDataExtension resolveMetaDataExtension(CamelContext context, Class<? extends MetaDataExtension> metaDataExtensionClass, String componentId, String actionId) {
        return new FhirMetaDataExtension(context);
    }

    @SuppressWarnings("unchecked")
    @Override
    protected SyndesisMetadata adapt(CamelContext context, String componentId, String actionId, Map<String, Object> properties, MetaDataExtension.MetaData metadata) {
        if (!properties.containsKey("resourceType")) {
            return SyndesisMetadata.EMPTY;
        }

        Set<String> resourceTypes = (Set<String>) metadata.getPayload();
        List<PropertyPair> resourceTypeResult = new ArrayList<>();
        resourceTypes.stream().forEach(
            t -> resourceTypeResult.add(new PropertyPair(t, t))
        );

        if (ObjectHelper.isNotEmpty(properties.get("resourceType"))) {
            final Map<String, List<PropertyPair>> enrichedProperties = new HashMap<>();
            enrichedProperties.put("resourceType",resourceTypeResult);

            String type = properties.get("resourceType").toString();
            try {

                String specification = buildSpecification(type);

                if (actionId.contains("read")) {
                    return new SyndesisMetadata(
                        enrichedProperties,
                        new DataShape.Builder().kind(DataShapeKinds.JAVA)//
                            .type("io.syndesis.connector.fhir.FhirReadMessageModel")
                            .description("FHIR " + actionId)
                            .name(actionId).build(),
                        new DataShape.Builder().kind(DataShapeKinds.XML_SCHEMA)//
                            .type(type)
                            .description("FHIR " + type)
                            .specification(specification)
                            .name(type).build());
                } else {
                    return new SyndesisMetadata(
                        enrichedProperties,
                        new DataShape.Builder().kind(DataShapeKinds.XML_SCHEMA)//
                            .type(type)
                            .description("FHIR " + type)
                            .specification(specification)
                            .name(type).build(),
                        new DataShape.Builder().kind(DataShapeKinds.JAVA)//
                            .type("ca.uhn.fhir.rest.api.MethodOutcome")
                            .description("FHIR " + actionId)
                            .name(actionId).build());
                }
            } catch (Exception e) {
                throw new IllegalStateException(
                    "Error retrieving resource schema for type: " + type, e);
            }
        } else if (properties.containsKey("resourceType")){
            return SyndesisMetadata.of(
                Collections.singletonMap("resourceType", resourceTypeResult)
            );
        }

        return SyndesisMetadata.EMPTY;
    }

    @SuppressWarnings({"PMD.UseStringBufferForStringAppends"})
    String buildSpecification(String type) throws IOException {
        String resourcePath = type.toLowerCase(Locale.ENGLISH);

        String specification = Resources.getResourceAsText("schemas/dstu3/" + resourcePath + ".xsd", FhirMetadataRetrieval.class.getClassLoader());

        String fhirBaseTemplate = Resources.getResourceAsText("schemas/dstu3/fhir-base-template.xml", FhirMetadataRetrieval.class.getClassLoader());
        String resourceContainer = "<xs:complexType name=\"ResourceContainer\"><xs:choice><xs:element ref=\"" + type + "\"/></xs:choice></xs:complexType>";

        fhirBaseTemplate = StringUtils.replaceOnce(fhirBaseTemplate, "<!-- RESOURCE CONTAINER PLACEHOLDER -->", resourceContainer);

        String fhirCommonTemplate = Resources.getResourceAsText("schemas/dstu3/fhir-common-template.xml", FhirMetadataRetrieval.class.getClassLoader());
        fhirBaseTemplate += fhirCommonTemplate;

        specification = StringUtils.replaceOnce(specification, "<xs:include schemaLocation=\"fhir-base.xsd\"/>", fhirBaseTemplate);
        return specification;
    }
}
