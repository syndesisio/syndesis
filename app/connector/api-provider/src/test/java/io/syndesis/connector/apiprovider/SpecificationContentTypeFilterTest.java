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
package io.syndesis.connector.apiprovider;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class SpecificationContentTypeFilterTest {

    FilterChain chain = mock(FilterChain.class);

    SpecificationContentTypeFilter filter = new SpecificationContentTypeFilter();

    HttpServletRequest request = mock(HttpServletRequest.class);

    HttpServletResponse response = mock(HttpServletResponse.class);

    @Test
    public void shouldSetContentTypeForJsonApiSpecification() throws ServletException, IOException {
        assertContentTypeSwitch("/.api-doc/swagger.json", "application/vnd.oai.openapi+json;version=2.0");
    }

    @Test
    public void shouldSetContentTypeForYamlApiSpecification() throws ServletException, IOException {
        assertContentTypeSwitch("/.api-doc/swagger.yaml", "application/vnd.oai.openapi;version=2.0");
    }

    void assertContentTypeSwitch(final String requestUri, final String expectedContentType)
        throws IOException, ServletException {
        when(request.getRequestURI()).thenReturn(requestUri);

        final ArgumentCaptor<HttpServletResponse> responseCaptor = ArgumentCaptor.forClass(HttpServletResponse.class);

        doNothing().when(chain).doFilter(ArgumentMatchers.eq(request), responseCaptor.capture());

        filter.doFilter(request, response, chain);

        final HttpServletResponse wrappedResponse = responseCaptor.getValue();

        assertThat(wrappedResponse).isInstanceOf(HttpServletResponseWrapper.class);

        wrappedResponse.setContentType("ignored");

        verify(response).setContentType(expectedContentType);
    }
}
