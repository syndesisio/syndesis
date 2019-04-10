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
package io.syndesis.connector.http;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.component.extension.ComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.DefaultComponentVerifierExtension;
import org.apache.camel.component.extension.verifier.ResultBuilder;
import org.apache.camel.component.extension.verifier.ResultErrorBuilder;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.StringHelper;
import org.apache.commons.lang3.StringUtils;

public class HttpConnectorVerifierExtension extends DefaultComponentVerifierExtension {
    private final String componentScheme;
    private final String supportedScheme;

    public HttpConnectorVerifierExtension(String componentScheme, String supportedScheme, CamelContext context) {
        super(componentScheme, context);

        this.componentScheme = componentScheme;
        this.supportedScheme = supportedScheme;
    }

    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    @Override
    public Result verify(Scope scope, Map<String, Object> parameters) {
        final Map<String, Object> options = new HashMap<>(parameters);

        String baseUrl = (String) options.remove("baseUrl");
        String uriScheme = StringHelper.before(baseUrl, "://");

        if (ObjectHelper.isNotEmpty(uriScheme) && !ObjectHelper.equal(supportedScheme, uriScheme)) {
            return ResultBuilder.withScope(scope).error(
                ResultErrorBuilder.withCode("unsupported_scheme")
                    .description("Unsupported scheme: " + uriScheme)
                    .parameterKey("baseUrl")
                    .build()
            ).build();
        }

        if (ObjectHelper.isEmpty(uriScheme)) {
            baseUrl = supportedScheme + "://" + baseUrl;
        }

        String path = (String) options.remove("path");
        if (ObjectHelper.isNotEmpty(path)) {
            if (path.charAt(0) != '/') {
                path = "/" + path;
            }

            options.put("httpUri", StringUtils.removeEnd(baseUrl, "/") + path);
        } else {
            options.put("httpUri", baseUrl);
        }

        Component component = getCamelContext().getComponent(this.componentScheme);
        if (component == null) {
            return ResultBuilder.withScope(scope).error(
                ResultErrorBuilder.withCode(VerificationError.StandardCode.UNSUPPORTED_COMPONENT)
                    .description("Unsupported component " + this.componentScheme)
                    .build()
            ).build();
        }

        return component.getExtension(ComponentVerifierExtension.class)
            .map(extension -> extension.verify(scope, options))
            .orElseGet(() -> ResultBuilder.unsupported().build());
    }
}
