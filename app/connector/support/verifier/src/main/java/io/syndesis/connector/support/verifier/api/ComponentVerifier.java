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
package io.syndesis.connector.support.verifier.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.camel.CamelContext;
import org.apache.camel.Component;
import org.apache.camel.component.extension.ComponentVerifierExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author roland
 * @since 28/03/2017
 */
public class ComponentVerifier implements Verifier {
    private static final Logger LOG = LoggerFactory.getLogger(ComponentVerifier.class);

    private final Class<? extends ComponentVerifierExtension> verifierExtensionClass;
    private final String defaultComponentScheme;

    @SuppressWarnings("PMD.AvoidUsingVolatile")
    private volatile ComponentVerifierExtension verifierExtension;

    public ComponentVerifier() {
        this(null, ComponentVerifierExtension.class);
    }

    public ComponentVerifier(String componentScheme) {
        this(componentScheme, ComponentVerifierExtension.class);
    }

    public ComponentVerifier(Class<? extends ComponentVerifierExtension> verifierExtensionClass) {
        this(null, verifierExtensionClass);
    }

    public ComponentVerifier(String componentScheme, Class<? extends ComponentVerifierExtension> verifierExtensionClass) {
        this.defaultComponentScheme = componentScheme;
        this.verifierExtensionClass = verifierExtensionClass;
    }

    // *************************************
    // Impl
    // *************************************

    @Override
    public List<VerifierResponse> verify(CamelContext context, String connectorId, Map<String, Object> params) {
        final String scheme = getConnectorAction().orElse(connectorId);
        final ComponentVerifierExtension verifier = resolveComponentVerifierExtension(context, scheme);

        if (verifier == null) {
            return Collections.singletonList(createUnsupportedResponse(scheme));
        }

        customize(params);

        return doVerify(verifier, scheme, params);
    }

    // *************************************
    // Customizations
    // *************************************

    protected Optional<String> getConnectorAction() {
        return Optional.ofNullable(defaultComponentScheme);
    }

    protected void customize(Map<String, Object> params) {
        // Hook for customizing params
    }

    @SuppressWarnings("PMD.AvoidDeeplyNestedIfStmts")
    protected ComponentVerifierExtension resolveComponentVerifierExtension(CamelContext context, String scheme) {
        if (verifierExtension == null) {
            synchronized (this) {
                if (verifierExtension == null) {
                    Component component = context.getComponent(scheme, true, false);
                    if (component == null) {
                        LOG.error("Component {} does not exist", scheme);
                    } else {
                        verifierExtension = component.getExtension(verifierExtensionClass).orElse(null);
                        if (verifierExtension == null) {
                            LOG.warn("Component {} does not support verifier extension", scheme);
                        }
                    }
                }
            }
        }

        return verifierExtension;
    }

    protected List<VerifierResponse> doVerify(ComponentVerifierExtension verifier, String scheme, Map<String, Object> params) {
        // the connector must support ping check if its verifiable
        final List<VerifierResponse> resp = new ArrayList<>();

        for (Verifier.Scope scope :  Verifier.Scope.values()) {
            try {
                ComponentVerifierExtension.Result result = verifier.verify(toComponentScope(scope), params);
                resp.add(toVerifierResponse(result));

                LOG.info("Verify ({}): {} === {}", scope, scheme, result.getStatus());

                if (result.getStatus() == ComponentVerifierExtension.Result.Status.ERROR) {
                    LOG.error("{} --> ", scheme);
                    for (ComponentVerifierExtension.VerificationError error : result.getErrors()) {
                        LOG.error("   {} : {}", error.getCode(), error.getDescription());
                    }
                }

                if (result.getStatus() == ComponentVerifierExtension.Result.Status.ERROR ||
                    result.getStatus() == ComponentVerifierExtension.Result.Status.UNSUPPORTED) {
                    break;
                }
            } catch (@SuppressWarnings("PMD") Exception exp) {
                resp.add(toExceptionResponse(exp, scope, params.keySet()));
                LOG.error("Exception during verify with params {} and scope {} : {}", params, scope, exp.getMessage(), exp);
            }
        }

        return resp;
    }

    // *************************************
    // Helpers
    // *************************************

    protected ComponentVerifierExtension.Scope toComponentScope(Scope scope) {
        switch (scope) {
            case CONNECTIVITY:
                return ComponentVerifierExtension.Scope.CONNECTIVITY;
            case PARAMETERS:
                return ComponentVerifierExtension.Scope.PARAMETERS;
            default:
                throw new IllegalArgumentException("Unknown scope value " + scope);
        }
    }

    protected VerifierResponse createUnsupportedResponse(String connectorId) {
        return new VerifierResponse.Builder(Status.UNSUPPORTED, Scope.PARAMETERS)
            .error("internal-error", String.format("No action %s used for the verification known", connectorId))
            .build();
    }

    protected VerifierResponse toVerifierResponse(ComponentVerifierExtension.Result result) {
        VerifierResponse.Builder builder =
            new VerifierResponse.Builder(result.getStatus().name(),
                                         result.getScope().name());
        if (result.getErrors() != null) {
            for (ComponentVerifierExtension.VerificationError error : result.getErrors()) {
                builder.withError(error.getCode().getName())
                     .description(error.getDescription())
                     .parameters(error.getParameterKeys())
                     .attributes(
                         error.getDetails().entrySet().stream().collect(
                             Collectors.toMap(
                                 e -> e.getKey().name(),
                                 e -> e.getValue()
                             )
                         )
                     )
                   .endError();
            }
        }
        return builder.build();
    }

    protected VerifierResponse toExceptionResponse(Exception exp, Scope scope, Set<String> params) {
        VerifierResponse.Builder builder = new VerifierResponse.Builder(Status.ERROR, scope);

        return builder
            .withError(ComponentVerifierExtension.VerificationError.StandardCode.EXCEPTION.name())
                .description(exp.getMessage())
                .parameters(params)
                .attributes(extractExceptionDetails(exp))
            .endError()
            .build();
    }

    protected Map<String, Object> extractExceptionDetails(Exception exp) {
        Map<String, Object> details = new HashMap<>();
        details.put(ComponentVerifierExtension.VerificationError.ExceptionAttribute.EXCEPTION_CLASS.name(), exp.getClass().getName());
        details.put(ComponentVerifierExtension.VerificationError.ExceptionAttribute.EXCEPTION_INSTANCE.name(), exp);
        return details;
    }

}
