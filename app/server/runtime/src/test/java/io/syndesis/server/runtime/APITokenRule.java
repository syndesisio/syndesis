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
package io.syndesis.server.runtime;

import org.junit.rules.ExternalResource;

public class APITokenRule extends ExternalResource {

    private static final String EXPIRED_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJGSjg2R2NGM2pUYk5MT2NvNE52WmtVQ0lVbWZZQ3FvcXRPUWVNZmJoTmxFIn0.eyJqdGkiOiIzNTg2M2I4Ny1mMjQ2LTQ3NzItYTMyNy00Yzc3NzY5NjVjNDkiLCJleHAiOjE0ODYzMzQ1MDAsIm5iZiI6MCwiaWF0IjoxNDg2MzM0MjAwLCJpc3MiOiJodHRwOi8vbG9jYWxob3N0OjgwODAvYXV0aC9yZWFsbXMvaXBhYXMtdGVzdCIsImF1ZCI6ImFkbWluLWNsaSIsInN1YiI6IjliZWRhNjUyLWY0NDYtNGFhZS1iODQ1LWQ1M2VjNDc1OGQ3OCIsInR5cCI6IkJlYXJlciIsImF6cCI6ImFkbWluLWNsaSIsImF1dGhfdGltZSI6MCwic2Vzc2lvbl9zdGF0ZSI6IjU3MzcyMmQ5LTc2NzAtNDhhZi1iMDY4LWUxNGNmNjRjM2U2NCIsImFjciI6IjEiLCJjbGllbnRfc2Vzc2lvbiI6IjAyY2M5ZTVkLTU3NWUtNDEzNi05NDk5LWI5OGY4ZjhjYmFhYiIsImFsbG93ZWQtb3JpZ2lucyI6W10sInJlc291cmNlX2FjY2VzcyI6e30sIm5hbWUiOiJTYW1wbGUgVXNlciIsInByZWZlcnJlZF91c2VybmFtZSI6InVzZXIiLCJnaXZlbl9uYW1lIjoiU2FtcGxlIiwiZmFtaWx5X25hbWUiOiJVc2VyIiwiZW1haWwiOiJzYW1wbGUtdXNlckBleGFtcGxlIn0.H2edv1-kUIYd7_nStjR-70hmdy7H6QG3sgjPhJGHhqMM6SMkjBjCHO0BHSkPFiG05fD6ah6kQAsxHIV-Bfd7k0rCoWrF3WH2mwtJDje36WLpGtFXbPNBUv0YFO5F61tkdCUL-gBJS-3VPWD68nskpAZcgabFGhM9TBxbC0geJzA";

    private static final String ACCESS_TOKEN = "sometoken";

    public APITokenRule() {
    }

    public String validToken() {
        return ACCESS_TOKEN;
    }

    public String expiredToken() {
        return EXPIRED_TOKEN;
    }

}
