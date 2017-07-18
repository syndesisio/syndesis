/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.integration.runtime.stephandlers;

import io.syndesis.integration.model.steps.SetHeaders;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.StepHandler;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import org.apache.camel.model.ProcessorDefinition;

import java.util.Map;
import java.util.Set;

public class SetHeadersHandler implements StepHandler<SetHeaders> {
  @Override
  public boolean canHandle(Step step) {
    return step.getClass().equals(SetHeaders.class);
  }

  @Override
  public ProcessorDefinition handle(SetHeaders step, ProcessorDefinition route, SyndesisRouteBuilder routeBuilder) {
    Map<String, Object> headers = step.getHeaders();
    if (headers != null) {
      Set<Map.Entry<String, Object>> entries = headers.entrySet();
      for (Map.Entry<String, Object> entry : entries) {
        String key = entry.getKey();
        Object value = entry.getValue();
        route.setHeader(key, routeBuilder.constant(value));
      }
    }
    return route;
  }
}
