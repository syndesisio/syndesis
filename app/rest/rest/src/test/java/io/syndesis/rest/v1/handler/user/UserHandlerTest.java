/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.rest.v1.handler.user;

import io.fabric8.openshift.api.model.UserBuilder;
import io.fabric8.openshift.client.server.mock.OpenShiftServer;
import io.syndesis.model.user.User;
import io.syndesis.openshift.OpenShiftServiceImpl;

import org.assertj.core.api.Assertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
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
    public void successfulWhoAmI() {
        openShiftServer.expect()
            .get().withPath("/oapi/v1/users/~")
            .andReturn(
                200,
                new UserBuilder().withFullName("Test User").withNewMetadata().withName("testuser").and().build()
            ).once();

        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken("testuser", "doesn'tmatter"));

        UserHandler userHandler = new UserHandler(null, new OpenShiftServiceImpl(openShiftServer.getOpenshiftClient(), null));
        User user = userHandler.whoAmI();
        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getUsername()).isEqualTo("testuser");
        Assertions.assertThat(user.getFullName()).isNotEmpty().hasValue("Test User");
    }
    @Test
    public void successfulWhoAmIWithoutFullName() {
        openShiftServer.expect()
            .get().withPath("/oapi/v1/users/~")
            .andReturn(
                200,
                new UserBuilder().withNewMetadata().withName("testuser").and().build()
            ).once();

        SecurityContextHolder.getContext().setAuthentication(new PreAuthenticatedAuthenticationToken("testuser", "doesn'tmatter"));

        UserHandler userHandler = new UserHandler(null, new OpenShiftServiceImpl(openShiftServer.getOpenshiftClient(), null));
        User user = userHandler.whoAmI();
        Assertions.assertThat(user).isNotNull();
        Assertions.assertThat(user.getUsername()).isEqualTo("testuser");
        Assertions.assertThat(user.getFullName()).isEmpty();
    }

}
