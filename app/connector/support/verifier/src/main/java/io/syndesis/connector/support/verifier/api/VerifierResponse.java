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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author roland
 * @since 20/03/2017
 */
@JsonDeserialize
public class VerifierResponse {

    private Verifier.Scope scope;
    private Verifier.Status status;
    private List<Error> errors;

    VerifierResponse(Verifier.Status status, Verifier.Scope scope) {
        this.status = status;
        this.scope = scope;
    }

    VerifierResponse(String status, String scope) {
        this(Verifier.Status.valueOf(status.toUpperCase(Locale.US)),
             Verifier.Scope.valueOf(scope.toUpperCase(Locale.US)));
    }

    public Verifier.Scope getScope() {
        return scope;
    }

    public Verifier.Status getStatus() {
        return status;
    }

    public List<Error> getErrors() {
        return errors;
    }

    @SuppressWarnings("JavaLangClash")
    public static class Error {
        String code;
        String description;
        Set<String> parameters;
        Map<String, Object> attributes;

        public String getCode() {
            return code;
        }

        public String getDescription() {
            return description;
        }

        public Set<String> getParameters() {
            return parameters;
        }

        public Map<String, Object> getAttributes() {
            return attributes;
        }
    }

    public static class Builder {
        private final VerifierResponse resp;

        public Builder(String status, String scope) {
            resp = new VerifierResponse(status, scope);
        }

        public Builder(Verifier.Status status, Verifier.Scope scope) {
            resp = new VerifierResponse(status, scope);
        }

        public Builder error(String code, String description) {
            if (resp.errors == null) {
                resp.errors = new ArrayList<>();
            }
            Error error = new Error();
            error.code = code;
            error.description = description;
            resp.errors.add(error);
            return this;
        }

        public ErrorBuilder withError(String code) {
            return new ErrorBuilder(code);
        }

        public VerifierResponse build() {
            return resp;
        }

        public class ErrorBuilder {
            Error error = new Error();

            ErrorBuilder(String code) {
                error.code = code;
            }

            public ErrorBuilder description(String desc) {
                error.description = desc;
                return this;
            }

            public ErrorBuilder parameters(Set<String> params) {
                if (params != null) {
                    error.parameters = new HashSet<>(params);
                }
                return this;
            }

            public ErrorBuilder attributes(Map<String, Object> attributes) {
                if (attributes != null) {
                    error.attributes = new HashMap<>(attributes);
                }
                return this;
            }

            public Builder endError() {
                if (resp.errors == null) {
                    resp.errors = new ArrayList<>();
                }
                resp.errors.add(error);
                error = new Error();
                return Builder.this;
            }
        }
    }
}
