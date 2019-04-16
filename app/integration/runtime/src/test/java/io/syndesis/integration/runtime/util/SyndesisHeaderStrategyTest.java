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
package io.syndesis.integration.runtime.util;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultExchange;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

public class SyndesisHeaderStrategyTest {

    private final CamelContext camelContext = mock(CamelContext.class);

    private final Exchange exchange = new DefaultExchange(camelContext);

    @Test
    public void shouldFilterOutEverythingButInwardContentTypeHeader() {
        final SyndesisHeaderStrategy headerStrategy = new SyndesisHeaderStrategy();

        assertThat(headerStrategy.applyFilterToCamelHeaders("Content-Type", "", exchange)).isTrue();
        assertThat(headerStrategy.applyFilterToExternalHeaders("Content-Type", "", exchange)).isFalse();

        assertThat(headerStrategy.applyFilterToCamelHeaders("Host", "", exchange)).isTrue();
        assertThat(headerStrategy.applyFilterToExternalHeaders("Host", "", exchange)).isTrue();

        assertThat(headerStrategy.applyFilterToCamelHeaders("Forward", "", exchange)).isTrue();
        assertThat(headerStrategy.applyFilterToExternalHeaders("Forward", "", exchange)).isTrue();
    }
}
