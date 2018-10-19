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

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.camel.CamelContext;
import org.apache.camel.component.extension.metadata.AbstractMetaDataExtension;
import org.apache.camel.component.extension.metadata.MetaDataBuilder;

public class FhirMetaDataExtension extends AbstractMetaDataExtension {

    FhirMetaDataExtension(CamelContext context) {
        super(context);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Optional<MetaData> meta(Map<String, Object> parameters) {

        Set<String> resources = new HashSet<>();
        resources.add("Patient");
//        https://github.com/atlasmap/atlasmap/issues/577
//        String fhirVersion = (String) parameters.get("fhirVersion");
//        FhirVersionEnum fhirVersionEnum = fhirVersion == null ? FhirVersionEnum.DSTU3 : FhirVersionEnum.valueOf(fhirVersion);
//        switch (fhirVersionEnum) {
//            case DSTU2:
//                addResources(ca.uhn.fhir.model.dstu2.valueset.ResourceTypeEnum.values(),resources);
//                break;
//            case DSTU2_1:
//                addResources(org.hl7.fhir.dstu2016may.model.ResourceType.values(),resources);
//                break;
//            case DSTU3:
//                addResources(org.hl7.fhir.dstu3.model.ResourceType.values(),resources);
//                break;
//            case R4:
//                addResources(org.hl7.fhir.r4.model.ResourceType.values(),resources);
//                break;
//             default:
//                 addResources(org.hl7.fhir.dstu3.model.ResourceType.values(),resources);
//                 break;
//        }

        return Optional
                    .of(MetaDataBuilder.on(getCamelContext()).withAttribute(MetaData.CONTENT_TYPE, "text/plain")
                        .withAttribute(MetaData.JAVA_TYPE, String.class).withPayload(resources).build());
    }

//    private void addResources(Object[] resourceTypes, Set<String> resources) {
//        for (Object resourceType : resourceTypes) {
//            resources.add(resourceType.toString());
//        }
//    }
}
