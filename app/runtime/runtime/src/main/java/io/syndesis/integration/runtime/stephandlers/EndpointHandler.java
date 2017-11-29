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
package io.syndesis.integration.runtime.stephandlers;

import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.util.Map;

import com.google.auto.service.AutoService;
import io.syndesis.integration.model.steps.Endpoint;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.StepHandler;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import io.syndesis.integration.support.Strings;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.util.ObjectHelper;
import org.apache.camel.util.URISupport;

@AutoService(StepHandler.class)
public class EndpointHandler implements StepHandler<Endpoint> {
    @Override
    public boolean canHandle(Step step) {
      return step.getClass().equals(Endpoint.class);
    }

    @Override
    public ProcessorDefinition handle(Endpoint step, ProcessorDefinition route, SyndesisRouteBuilder routeBuilder) {
      final String uri = buildUri(step);

      if (!Strings.isEmpty(uri)) {
          if (route == null) {
              route = routeBuilder.from(uri);
          } else {
              route = route.to(uri);
          }

      }

      return route;
    }

    private static String buildUri(Endpoint step) {
        String uri = step.getUri();
        Map<String, Object> properties = step.getProperties();

        if (!Strings.isEmpty(uri)) {
            if (ObjectHelper.isNotEmpty(properties)) {
                try {
                    uri = URISupport.appendParametersToURI(uri, properties);
                } catch (UnsupportedEncodingException|URISyntaxException e) {
                    throw ObjectHelper.wrapRuntimeCamelException(e);
                }
            }
        }

        return uri;
    }
}
