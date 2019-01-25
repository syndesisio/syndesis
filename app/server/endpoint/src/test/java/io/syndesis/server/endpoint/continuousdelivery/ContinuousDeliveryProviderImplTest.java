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
package io.syndesis.server.endpoint.continuousdelivery;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.security.Principal;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.StreamingOutput;

import org.junit.Before;
import org.junit.Test;

import io.syndesis.common.model.ListResult;
import io.syndesis.common.model.WithResourceId;
import io.syndesis.common.model.integration.ContinuousDeliveryEnvironment;
import io.syndesis.common.model.integration.Integration;
import io.syndesis.server.dao.manager.DataManager;
import io.syndesis.server.dao.manager.EncryptionComponent;
import io.syndesis.server.endpoint.monitoring.MonitoringProvider;
import io.syndesis.server.endpoint.v1.handler.connection.ConnectionHandler;
import io.syndesis.server.endpoint.v1.handler.integration.IntegrationDeploymentHandler;
import io.syndesis.server.endpoint.v1.handler.integration.support.IntegrationSupportHandler;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit test for {@link ContinuousDeliveryProviderImpl}.
 */
public class ContinuousDeliveryProviderImplTest {

    private static final String INTEGRATION_ID = "integration-id";
    private static final String ENVIRONMENT = "environment";

    private final DataManager dataManager = mock(DataManager.class);
    private final IntegrationSupportHandler supportHandler = mock(IntegrationSupportHandler.class);
    private final EncryptionComponent encryptionComponent = mock(EncryptionComponent.class);
    private final IntegrationDeploymentHandler deploymentHandler = mock(IntegrationDeploymentHandler.class);
    private final ConnectionHandler connectionHandler = mock(ConnectionHandler.class);
    private final MonitoringProvider monitoringProvider = mock(MonitoringProvider.class);

    private final ContinuousDeliveryProvider provider = new ContinuousDeliveryProviderImpl(dataManager, supportHandler,
            encryptionComponent, deploymentHandler, connectionHandler, monitoringProvider);

    @Before
    public void setUp() throws Exception {
        // prime mock objects
        final HashMap<String, ContinuousDeliveryEnvironment> deliveryState = new HashMap<>();
        deliveryState.put(ENVIRONMENT, ContinuousDeliveryEnvironment.Builder.createFrom(ENVIRONMENT, new Date()));
        final Integration integration = new Integration.Builder()
                .id(INTEGRATION_ID)
                .continuousDeliveryState(deliveryState)
                .build();

        when(dataManager.fetch(Integration.class, INTEGRATION_ID)).thenReturn(integration);
        when(dataManager.fetchAll(eq(Integration.class), any())).thenReturn(ListResult.of(integration));

        when(supportHandler.export(any())).thenReturn(out -> out.write('b'));
        final HashMap<String, List<WithResourceId>> importResult = new HashMap<>();
        importResult.put(INTEGRATION_ID, Collections.singletonList(integration));
        when(supportHandler.importIntegration(any(), any())).thenReturn(importResult);
    }

    @Test
    public void tagForRelease() throws Exception {
        final ContinuousDeliveryEnvironment continuousDeliveryEnvironment = provider.tagForRelease(INTEGRATION_ID,
                ENVIRONMENT);

        assertThat(continuousDeliveryEnvironment, is(notNullValue()));
        assertThat(continuousDeliveryEnvironment.getName(), is(equalTo(ENVIRONMENT)));

        verify(dataManager).update(notNull());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void exportResources() throws Exception {
        final StreamingOutput streamingOutput = provider.exportResources(ENVIRONMENT, false);
        assertThat(streamingOutput, is(notNullValue()));

        verify(dataManager).fetchAll(any(Integration.class.getClass()), any(Function.class));
        verify(dataManager).update(any(Integration.class));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void importResources() throws Exception {
        // export integration
        final StreamingOutput streamingOutput = provider.exportResources(ENVIRONMENT, false);

        // import it back
        final SecurityContext security = mock(SecurityContext.class);
        final Principal principal = mock(Principal.class);
        when(security.getUserPrincipal()).thenReturn(principal);
        when(principal.getName()).thenReturn("user");

        ContinuousDeliveryProvider.ImportFormDataInput formInput = new ContinuousDeliveryProvider.ImportFormDataInput();
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        streamingOutput.write(bytes);
        formInput.setImportFile(new ByteArrayInputStream(bytes.toByteArray()));
        formInput.setParamsFile(new ByteArrayInputStream("test-connection.prop=value".getBytes("UTF-8")));
        formInput.setEnvironment(ENVIRONMENT);
        formInput.setDeploy(Boolean.TRUE);

        provider.importResources(security, formInput);

        // assert that integration was recreated
        verify(dataManager).fetchAll(any(Integration.class.getClass()), any());
        verify(dataManager, times(2)).update(any(Integration.class));

    }

}