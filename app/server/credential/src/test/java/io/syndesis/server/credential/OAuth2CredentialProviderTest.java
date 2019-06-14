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
package io.syndesis.server.credential;

import java.util.Collections;

import org.junit.Test;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OAuth2CredentialProviderTest {

    @Test
    public void shouldCreateAcquisitionMethod() {
        @SuppressWarnings("unchecked")
        final OAuth2CredentialProvider<?> oauth2 = new OAuth2CredentialProvider<>("provider2",
            mock(OAuth2ConnectionFactory.class), mock(Applicator.class), Collections.emptyMap());

        final AcquisitionMethod method2 = new AcquisitionMethod.Builder()
            .description("provider2")
            .label("provider2")
            .icon("provider2")
            .type(Type.OAUTH2)
            .configured(true)
            .build();

        assertThat(oauth2.acquisitionMethod()).isEqualTo(method2);
    }

}
