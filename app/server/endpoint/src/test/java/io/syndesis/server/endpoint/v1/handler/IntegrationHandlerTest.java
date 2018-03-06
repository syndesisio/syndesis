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
package io.syndesis.server.endpoint.v1.handler;

import java.util.Arrays;
import java.util.Optional;

import javax.validation.Validator;

import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.inspector.Inspectors;
import io.syndesis.common.model.DataShape;
import io.syndesis.common.model.DataShapeKinds;
import io.syndesis.common.model.filter.FilterOptions;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationHandler;

import io.syndesis.server.openshift.OpenShiftService;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author roland
 * @since 29.08.17
 */
public class IntegrationHandlerTest {


    private IntegrationHandler handler;
    private Inspectors inspectors;
    private OpenShiftService openShiftService;

    @Before
    public void setUp() {
        DataManager manager = mock(DataManager.class);
        Validator validator = mock(Validator.class);
        openShiftService = mock(OpenShiftService.class);
        inspectors = mock(Inspectors.class);
        handler = new IntegrationHandler(manager, openShiftService, validator, inspectors, new EncryptionComponent(null));
    }

    @Test
    public void filterOptionsSimple() {
        when(inspectors.getPaths(DataShapeKinds.JAVA.toString(), "twitter4j.Status", null, Optional.empty())).thenReturn(Arrays.asList("paramA", "paramB"));
        DataShape dataShape = dataShape(DataShapeKinds.JAVA, "twitter4j.Status");

        FilterOptions options = handler.getFilterOptions(dataShape);
        assertThat(options.getPaths()).hasSize(2).contains("paramA","paramB");
    }

    @Test
    public void filterOptionsNoOutputShape() {
        DataShape dataShape = dataShape(DataShapeKinds.NONE);

        FilterOptions options = handler.getFilterOptions(dataShape);
        assertThat(options.getPaths()).isEmpty();
    }

    private DataShape dataShape(DataShapeKinds kind) {
        return dataShape(kind, null);
    }

    private DataShape dataShape(DataShapeKinds kind, String type) {
        return new DataShape.Builder().kind(kind).type(type).build();
    }
}
