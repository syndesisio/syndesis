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
package io.syndesis.server.endpoint.v1.handler.user;

import io.fabric8.kubernetes.client.dsl.NonNamespaceOperation;
import io.fabric8.kubernetes.client.dsl.Resource;
import io.fabric8.openshift.api.model.DoneableUser;
import io.fabric8.openshift.api.model.User;
import io.fabric8.openshift.api.model.UserBuilder;
import io.fabric8.openshift.api.model.UserList;
import io.fabric8.openshift.client.NamespacedOpenShiftClient;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import io.syndesis.server.openshift.OpenShiftServiceImpl;

import javax.ws.rs.core.SecurityContext;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.security.Principal;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;

public class UserHandlerTest {

    @Rule
    public OpenShiftServer openShiftServer = new OpenShiftServer();

    @After
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void successfulWhoAmIWithoutFullName() {

        SecurityContext sec = mock(SecurityContext.class);
        Principal principal = mock(Principal.class);
        when(principal.getName()).thenReturn("testuser");
        when(sec.getUserPrincipal()).thenReturn(principal);

        NamespacedOpenShiftClient client = mock(NamespacedOpenShiftClient.class);
        @SuppressWarnings("unchecked")
        Resource<User,DoneableUser> user = mock(Resource.class);
        when(user.get()).thenReturn(new UserBuilder().withNewMetadata().withName("testuser").and().build());
        @SuppressWarnings("unchecked")
        NonNamespaceOperation<User, UserList, DoneableUser, Resource<User, DoneableUser>> users = mock(NonNamespaceOperation.class);
        when(users.withName("testuser")).thenReturn(user);
        when(client.users()).thenReturn(users);

        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken("testuser", "doesn'tmatter"));

        UserConfigurationProperties properties = new UserConfigurationProperties();
        properties.setMaxDeploymentsPerUser(1);
        UserHandler userHandler = new UserHandler(null, new OpenShiftServiceImpl(client, null), properties);
        io.syndesis.common.model.user.User whoAmI = userHandler.whoAmI(sec);
        Assertions.assertThat(whoAmI).isNotNull();
        Assertions.assertThat(whoAmI.getUsername()).isEqualTo("testuser");
        Assertions.assertThat(whoAmI.getFullName()).isEmpty();
    }

}
