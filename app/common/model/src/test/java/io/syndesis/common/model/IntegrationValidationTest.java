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
package io.syndesis.common.model;

import io.syndesis.common.model.integration.Integration;
import org.junit.Test;

import javax.validation.ConstraintViolationException;

public class IntegrationValidationTest {

    @Test
    public void shouldSucceedCorrectNameIntegrations() {
        Integration integration = new Integration.Builder().name("good-integration-name").build();
        Integration integration2 = new Integration.Builder().name("good-integration-name-2").build();
        Integration integration3 = new Integration.Builder().name("goodIntegrationname").build();
        Integration integration4 = new Integration.Builder().name("goodIntegrationname2").build();
        Integration integration5 = new Integration.Builder().name("goodIntegrationname_2").build();
        Integration integration6 = new Integration.Builder().name("goodIntegrationname.2").build();
        Integration integration7 = new Integration.Builder().name("good integrationname 2").build();
        Integration integration8 = new Integration.Builder().name("1 good integrationname").build();
        Integration integration9 = new Integration.Builder().name("_my_integration").build();
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldFailIllegalNameIntegrationComma() {
        Integration integration = new Integration.Builder().name("illegal,name").build();
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldFailIllegalNameIntegrationQuestionMark() {
        Integration integration3 = new Integration.Builder().name("illegalName?").build();
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldFailIllegalNameIntegrationEquals() {
        Integration integration4 = new Integration.Builder().name("illegal=name").build();
    }

    @Test(expected = ConstraintViolationException.class)
    public void shouldFailIllegalNameIntegrationStar() {
        Integration integration5 = new Integration.Builder().name("illegal*").build();
    }

}
