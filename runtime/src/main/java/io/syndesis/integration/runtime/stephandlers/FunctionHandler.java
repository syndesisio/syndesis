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

import com.google.auto.service.AutoService;
import io.syndesis.integration.model.steps.Function;
import io.syndesis.integration.model.steps.Step;
import io.syndesis.integration.runtime.StepHandler;
import io.syndesis.integration.runtime.SyndesisRouteBuilder;
import io.syndesis.integration.support.Strings;
import org.apache.camel.model.ProcessorDefinition;

@AutoService(StepHandler.class)
public class FunctionHandler implements StepHandler<Function> {

  @Override
  public boolean canHandle(Step step) {
    return step.getClass().equals(Function.class);
  }

  @Override
  public ProcessorDefinition handle(Function step, ProcessorDefinition route, SyndesisRouteBuilder routeBuilder) {
    String functionName = step.getName();
    if (!Strings.isEmpty(functionName)) {
      String method = null;
      int idx = functionName.indexOf("::");
      if (idx > 0) {
        method = functionName.substring(idx + 2);
        functionName = functionName.substring(0, idx);
      }
      String uri = "class:" + functionName;
      if (method != null) {
        uri += "?method=" + method;
      }
      uri = routeBuilder.convertEndpointURI(uri);
      route = route.to("json:marshal").to(uri);
    }
    return route;
  }
}
