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
package io.syndesis.connector.odata.server;

import org.apache.olingo.commons.api.edm.FullQualifiedName;
import io.syndesis.common.util.StringConstants;

public interface ODataServerConstants extends StringConstants {

    // Service Namespace
    String NAMESPACE = "OData.Demo";

    // EDM Container
    String CONTAINER_NAME = "Container";
    FullQualifiedName CONTAINER = new FullQualifiedName(NAMESPACE, CONTAINER_NAME);

    // Entity Types Names
    String ET_PRODUCT_NAME = "Product";
    FullQualifiedName ET_PRODUCT_FQN = new FullQualifiedName(NAMESPACE, ET_PRODUCT_NAME);

    String ET_SPEC_NAME = "Specification";
    FullQualifiedName ET_SPEC_FQN = new FullQualifiedName(NAMESPACE, ET_SPEC_NAME);

    String ET_POWER_TYPE_NAME = "PowerType";
    FullQualifiedName ET_POWER_TYPE_FQN = new FullQualifiedName(NAMESPACE, ET_POWER_TYPE_NAME);

    // Entity Set Names
    String ES_PRODUCTS_NAME = "Products";

    String PRODUCT_ID = "ID";
    String PRODUCT_NAME = "Name";
    String PRODUCT_DESCRIPTION = "Description";
    String PRODUCT_SERIALS = "SerialNumbers";
    String PRODUCT_SPEC = "Specification";

    String SPEC_PRODUCT_TYPE = "ProductType";
    String SPEC_DETAIL_1 = "Detail1";
    String SPEC_DETAIL_2 = "Detail2";
    String SPEC_POWER_TYPE = "PowerType";

}
