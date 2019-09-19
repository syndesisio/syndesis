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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.module.jsonSchema.factories.JsonSchemaFactory;
import com.fasterxml.jackson.module.jsonSchema.types.ObjectSchema;
import io.atlasmap.xml.v2.XmlComplexType;
import io.atlasmap.xml.v2.XmlDocument;
import io.atlasmap.xml.v2.XmlField;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.util.Json;
import io.syndesis.common.util.Resources;
import io.syndesis.common.util.SyndesisServerException;
import io.syndesis.connector.support.util.ConnectorOptions;
import io.syndesis.connector.support.verifier.api.ComponentMetadataRetrieval;
import io.syndesis.connector.support.verifier.api.PropertyPair;
import io.syndesis.connector.support.verifier.api.SyndesisMetadata;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.MetaDataExtension;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class FhirMetadataRetrieval extends ComponentMetadataRetrieval {

    final ObjectMapper mapper = io.atlasmap.v2.Json.mapper();

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
        if (actionId.contains("transaction")) {
            //Transaction is not part of the FHIR spec, but we use it as a workaround for DataMapper to include
            //a number of resources in a transaction, because the DataMapper support for lists and choice fields is limited.
            properties.put("resourceType", "Transaction");
        }

        if (!properties.containsKey("resourceType")) {
            return SyndesisMetadata.EMPTY;
        }

        Set<String> resourceTypes = (Set<String>) metadata.getPayload();
        List<PropertyPair> resourceTypeResult = new ArrayList<>();
        resourceTypes.stream().forEach(
            t -> resourceTypeResult.add(new PropertyPair(t, t))
        );
        final Map<String, List<PropertyPair>> enrichedProperties = new HashMap<>();
        enrichedProperties.put("resourceType",resourceTypeResult);
        enrichedProperties.put("containedResourceTypes", resourceTypeResult);

        if (ObjectHelper.isNotEmpty(ConnectorOptions.extractOption(properties, "resourceType"))) {
            return createSyndesisMetadata(actionId, properties, enrichedProperties);
        } else {
            return SyndesisMetadata.of(enrichedProperties);
        }
    }

    private SyndesisMetadata createSyndesisMetadata(String actionId, Map<String, Object> properties, Map<String, List<PropertyPair>> enrichedProperties) {
        String containedResourceTypes = ConnectorOptions.extractOption(properties, "containedResourceTypes");
        String type = ConnectorOptions.extractOption(properties, "resourceType");

        if (actionId.contains("read")) {
            return new SyndesisMetadata(
                enrichedProperties,
                new DataShape.Builder().kind(DataShapeKinds.JAVA)//
                    .type(FhirResourceId.class.getName())
                    .description("FHIR " + actionId)
                    .name(actionId).build(),
                new DataShape.Builder().kind(DataShapeKinds.XML_SCHEMA_INSPECTED)//
                    .type(type)
                    .description("FHIR " + type)
                    .specification(newResourceSpecification(type, containedResourceTypes))
                    .name(type).build());
        } else if (actionId.contains("search")) {
            return new SyndesisMetadata(
                enrichedProperties,
                new DataShape.Builder().kind(DataShapeKinds.JAVA)
                    .type(FhirResourceQuery.class.getName())
                    .description("FHIR " + actionId)
                    .name(actionId).build(),
                new DataShape.Builder().kind(DataShapeKinds.XML_SCHEMA_INSPECTED)//
                    .type(type)
                    .description("FHIR " + type)
                    .specification(newResourceSpecification(type, containedResourceTypes))
                    .name(type).build());
        } else if (actionId.contains("delete")) {
            return new SyndesisMetadata(
                enrichedProperties,
                new DataShape.Builder().kind(DataShapeKinds.JAVA)//
                    .type(FhirResourceId.class.getName())
                    .description("FHIR " + actionId)
                    .name(actionId).build(),
                new DataShape.Builder().kind(DataShapeKinds.JAVA)//
                    .type("ca.uhn.fhir.rest.api.MethodOutcome")
                    .description("FHIR " + actionId)
                    .name(actionId).build());
        } else if (actionId.contains("patch")) {
            Integer operationNumber = ConnectorOptions.extractOptionAndMap(
                properties, "operationNumber", Integer::valueOf, 0);
            return new SyndesisMetadata(
                enrichedProperties,
                new DataShape.Builder().kind(DataShapeKinds.JSON_SCHEMA)//
                    .type(type)
                    .specification(newPatchSpecification(operationNumber))
                    .description("FHIR " + actionId)
                    .name(actionId).build(),
                new DataShape.Builder().kind(DataShapeKinds.JAVA)//
                    .type("ca.uhn.fhir.rest.api.MethodOutcome")
                    .description("FHIR " + actionId)
                    .name(actionId).build());
        } else if (actionId.contains("transaction")) {
            String specification = newResourceSpecification(type, containedResourceTypes);
            return new SyndesisMetadata(
                enrichedProperties,
                new DataShape.Builder().kind(DataShapeKinds.XML_SCHEMA_INSPECTED)//
                    .type(type)
                    .description("FHIR " + type)
                    .specification(specification)
                    .name(type).build(),
                new DataShape.Builder().kind(DataShapeKinds.XML_SCHEMA_INSPECTED)//
                    .type(type)
                    .description("FHIR " + type)
                    .specification(specification)
                    .name(actionId).build());
        } else {
            //create, update
            return new SyndesisMetadata(
                enrichedProperties,
                new DataShape.Builder().kind(DataShapeKinds.XML_SCHEMA_INSPECTED)//
                    .type(type)
                    .description("FHIR " + type)
                    .specification(newResourceSpecification(type, containedResourceTypes))
                    .name(type).build(),
                new DataShape.Builder().kind(DataShapeKinds.JAVA)//
                    .type("ca.uhn.fhir.rest.api.MethodOutcome")
                    .description("FHIR " + actionId)
                    .name(actionId).build());
        }
    }

    private String newPatchSpecification(Integer operationNumber) {
        final JsonSchemaFactory factory = new JsonSchemaFactory();
        final ObjectSchema patchSchema = factory.objectSchema();
        patchSchema.set$schema("http://json-schema.org/schema#");
        patchSchema.setTitle("Patch");
        patchSchema.putProperty("id", factory.stringSchema());
        for (int i = 1; i <= operationNumber; i++) {
            final ObjectSchema operation = factory.objectSchema();
            operation.putProperty("op", factory.stringSchema());
            operation.putProperty("path", factory.stringSchema());
            operation.putProperty("value", factory.stringSchema());
            patchSchema.putProperty(String.valueOf(i), operation);
        }
        final String schema;
        try {
            schema = Json.writer().writeValueAsString(patchSchema);
        } catch (JsonProcessingException e) {
            throw new SyndesisServerException(e);
        }
        return schema;
    }

    private String newResourceSpecification(String type, String containedResourceTypes) {
        String specification;
        String resourcePath = type.toLowerCase(Locale.ENGLISH);
        try {
            specification = Resources.getResourceAsText("META-INF/syndesis/schemas/dstu3/" + resourcePath + ".json", FhirMetadataRetrieval.class.getClassLoader());

            if (ObjectHelper.isNotEmpty(containedResourceTypes)) {
                String[] containedResourceTypesSplit = Json.reader().forType(String[].class).readValue(containedResourceTypes);
                specification = includeResources(specification, containedResourceTypesSplit);
            }
        } catch (Exception e) {
            throw new IllegalStateException(
                "Error retrieving resource schema for type: " + type, e);
        }
        return specification;
    }

    String includeResources(String specification, String... resourceTypes) throws IOException {
        if (resourceTypes != null && resourceTypes.length != 0) {
            XmlDocument document = mapper.readValue(specification, XmlDocument.class);
            includeResources(null, document, resourceTypes);
            return mapper.writer((PrettyPrinter) null).writeValueAsString(document);
        } else {
            return specification;
        }
    }

    private void includeResources(String rootPath, XmlDocument resource, String... resourceTypes) throws IOException {
        XmlComplexType resourceElement = (XmlComplexType) resource.getFields().getField().get(0);
        switch (resourceElement.getName()) {
            case "tns:Bundle":
                includeResourcesInPath(rootPath, resourceElement, resourceTypes, "tns:entry", "tns:resource");
                includeResourcesInPath(rootPath, resourceElement, resourceTypes, "tns:entry", "tns:response", "tns:outcome");
                break;
            case "tns:Parameters":
                includeResourcesInPath(rootPath, resourceElement, resourceTypes, "tns:parameter", "tns:resource");
                break;
            case "tns:Transaction":
                includeResourcesInPath(rootPath, resourceElement, resourceTypes);
                break;
            default:
                includeResourcesInPath(rootPath, resourceElement, resourceTypes, "tns:contained");
                break;
        }
    }

    private void includeResourcesInPath(String rootPath, XmlComplexType resourceElement, String[] resourceTypes, String... path) throws IOException {
        XmlComplexType element = getElement(resourceElement, 0, path);
        if (element != null) {
            List<XmlField> elements = new ArrayList<>();

            for (String resourceType: resourceTypes) {
                String inspectionToInclude = Resources.getResourceAsText("META-INF/syndesis/schemas/dstu3/" + resourceType.toLowerCase(Locale.ENGLISH) + ".json", FhirMetadataRetrieval.class.getClassLoader());
                String pathToReplace = String.format("%s/%s", resourceElement.getName(), StringUtils.join(path, "/"));
                if (rootPath != null) {
                    pathToReplace = String.format("%s/%s", rootPath, pathToReplace);
                }
                inspectionToInclude = inspectionToInclude.replaceAll("\\\"path\\\"[\\s]*:[\\s]*\\\"", "\"path\":\"/" + pathToReplace);

                XmlDocument resourceToInclude = mapper.readValue(inspectionToInclude, XmlDocument.class);
                if (rootPath == null) {
                    includeResources(pathToReplace, resourceToInclude, resourceTypes);
                }

                elements.add((XmlField) resourceToInclude.getFields().getField().get(0));
            }

            element.getXmlFields().getXmlField().clear();
            element.getXmlFields().getXmlField().addAll(elements);
        }
    }

    XmlComplexType getElement(XmlComplexType field, int depth, String... path) {
        if (path.length == 0) {
            return field;
        }

        for (XmlField xmlField: field.getXmlFields().getXmlField()) {
            if (xmlField instanceof XmlComplexType) {
                XmlComplexType xmlComplexType = (XmlComplexType) xmlField;
                if (xmlComplexType.getName().equals(path[depth])) {
                    if (depth == path.length - 1) {
                        return xmlComplexType;
                    } else {
                        return getElement(xmlComplexType, depth + 1, path);
                    }
                }
            }
        }
        return null;
    }
}
