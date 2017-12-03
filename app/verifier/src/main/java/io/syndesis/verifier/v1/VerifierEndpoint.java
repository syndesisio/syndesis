/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.verifier.v1;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import io.syndesis.verifier.Verifier;
import io.syndesis.verifier.VerifierRegistry;
import io.syndesis.verifier.VerifierResponse;
import org.springframework.stereotype.Component;


@Component
@Path("/verifier")
public class VerifierEndpoint {

    private VerifierRegistry verifierRegistry;

    public VerifierEndpoint(VerifierRegistry verifierRegistry) {
        this.verifierRegistry = verifierRegistry;
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public List<VerifierResponse> verify(@PathParam("id") String connectorId,
                                         Map<String, Object> parameters) {
        Verifier verifier = verifierRegistry.getVerifier(connectorId);
        if (verifier == null) {
            return Collections.singletonList(createUnsupportedResponse(connectorId));
        }
        return filterExceptions(verifier.verify(parameters));
    }

    private List<VerifierResponse> filterExceptions(List<VerifierResponse> responses) {
        for (VerifierResponse response : responses) {
            List<VerifierResponse.Error> errors = response.getErrors();
            if (errors != null) {
                for (VerifierResponse.Error error : errors) {
                    Map<String,Object> attributes = error.getAttributes();
                    if (attributes != null) {
                        Set<String> toRemove = new HashSet<String>();
                        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
                            if (entry.getValue() instanceof Exception) {
                                toRemove.add(entry.getKey());
                            }
                        }
                        for (String key : toRemove) {
                            attributes.remove(key);
                        }
                    }
                }
            }
        }
        return responses;
    }

    private VerifierResponse createUnsupportedResponse(String connectorId) {
        return new VerifierResponse.Builder(Verifier.Status.UNSUPPORTED,
                                            Verifier.Scope.PARAMETERS)
            .error("unknown-connector", String.format("No connector for ID %s registered", connectorId))
            .build();
    }
}
