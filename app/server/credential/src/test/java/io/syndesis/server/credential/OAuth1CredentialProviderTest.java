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

import org.junit.Test;
import org.springframework.social.connect.support.OAuth1ConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class OAuth1CredentialProviderTest {

    @Test
    public void shouldCreateAcquisitionMethod() {
        @SuppressWarnings("unchecked")
        final OAuth1CredentialProvider<?> oauth1 = new OAuth1CredentialProvider<>("provider1",
            mock(OAuth1ConnectionFactory.class), mock(Applicator.class));

        final AcquisitionMethod method1 = new AcquisitionMethod.Builder()
            .description("provider1")
            .label("provider1")
            .icon("provider1")
            .type(Type.OAUTH1)
            .configured(true)
            .build();

        assertThat(oauth1.acquisitionMethod()).isEqualTo(method1);
    }

}
