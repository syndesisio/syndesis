package com.redhat.ipaas.verifier;

import java.util.*;

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

    private VerifierResponse(Verifier.Status status, Verifier.Scope scope) {
        this.status = status;
        this.scope = scope;
    }

    private VerifierResponse(String status, String scope) {
        this(Verifier.Status.valueOf(status.toUpperCase()),
             Verifier.Scope.valueOf(scope.toUpperCase()));
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
        private VerifierResponse resp;

        public Builder(String status, String scope) {
            resp = new VerifierResponse(status, scope);
        }

        public Builder(Verifier.Status status, Verifier.Scope scope) {
            resp = new VerifierResponse(status, scope);
        }

        public Builder error(String code, String description) {
            if (resp.errors == null) {
                resp.errors = new ArrayList<Error>();
            }
            Error error = new Error();
            error.code = code;
            error.description = description;
            resp.errors.add(error);
            return Builder.this;
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
                    error.parameters = new HashSet<String>();
                    error.parameters.addAll(params);
                }
                return this;
            }

            public ErrorBuilder attributes(Map<String, Object> attributes) {
                if (attributes != null) {
                    error.attributes = new HashMap<String, Object>();
                    error.attributes.putAll(attributes);
                }
                return this;
            }

            public Builder endError() {
                if (resp.errors == null) {
                    resp.errors = new ArrayList<Error>();
                }
                resp.errors.add(error);
                error = new Error();
                return Builder.this;
            }
        }
    }
}
