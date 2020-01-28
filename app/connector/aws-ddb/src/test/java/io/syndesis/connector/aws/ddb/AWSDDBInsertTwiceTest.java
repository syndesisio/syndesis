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
package io.syndesis.connector.aws.ddb;

import org.apache.camel.ProducerTemplate;
import org.junit.Ignore;
import org.junit.Test;

import static net.javacrumbs.jsonunit.assertj.JsonAssertions.assertThatJson;

@Ignore("Make sure the AWSDDBConfiguration has the proper credentials before running this test")
public class AWSDDBInsertTwiceTest extends AWSDDBGenericOperation {

    @Override
    String getConnectorId() {
        return "io.syndesis:aws-ddb-putitem-to-connector";
    }

    @Override
    String getCustomizer() {
        return "io.syndesis.connector.aws.ddb.customizer" +
                   ".DDBConnectorCustomizerPutItem";
    }

    @Override
    String getElement() {
        return AWSDDBConfiguration.ELEMENT_VALUE_VARIABLE;
    }

    @Test
    @Override
    /**
     * To run this test you need to change the values of the parameters for real values of an
     * actual account
     */
    public void runIt() {

        assertNotNull(context());

        ProducerTemplate template = context().createProducerTemplate();

        @SuppressWarnings("unchecked")
        String result = template.requestBody("direct:start",
            "{\"#attribute\":\"to overwrite\"}", String.class);

        assertThatJson(result).isEqualTo("{\"clave\":\"" + AWSDDBConfiguration.RANDOM_ID
                                    + "\", \"attr\":\"to overwrite\"}");

        result = template.requestBody("direct:start",
            "{\"#attribute\":\"final value\"}", String.class);

        assertThatJson(result).isEqualTo("{\"clave\":\"" + AWSDDBConfiguration.RANDOM_ID
                                    + "\", \"attr\":\"final value\"}");

    }

}
