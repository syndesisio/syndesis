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
package io.syndesis.connector.fhir.customizer;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.FhirVersionEnum;
import io.syndesis.connector.support.util.ConnectorOptions;
import org.apache.camel.component.fhir.internal.FhirApiCollection;
import org.apache.camel.util.component.ApiMethod;

import java.util.Map;

public final class FhirCustomizerHelper {

    private FhirCustomizerHelper(){

    }

    public static FhirContext newFhirContext(Map<String, Object> options) {
        FhirVersionEnum fhirVersionEnum = ConnectorOptions.extractOptionAndMap(
            options, "fhirVersion", FhirVersionEnum::valueOf, null);
        return new FhirContext(fhirVersionEnum);
    }

    public static String getFhirApiName(Class<? extends ApiMethod> apiMethod) {
        return FhirApiCollection.getCollection().getApiName(apiMethod).getName();
    }
}
