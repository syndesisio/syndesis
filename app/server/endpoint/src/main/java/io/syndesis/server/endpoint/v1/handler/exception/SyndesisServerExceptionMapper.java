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
package io.syndesis.server.endpoint.v1.handler.exception;

import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

import org.springframework.stereotype.Component;

@Component
@Provider
public class SyndesisServerExceptionMapper extends BaseExceptionMapper<Exception> {

    public SyndesisServerExceptionMapper() {
        super(Response.Status.INTERNAL_SERVER_ERROR, "Please contact the administrator and file a bug report");
    }

    @Override
    protected String developerMessage(final Exception exception) {
        return "Internal Server Exception. " + exception.getMessage();
    }

}
