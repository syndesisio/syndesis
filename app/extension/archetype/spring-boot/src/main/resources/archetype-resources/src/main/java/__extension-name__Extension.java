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
package ${package};

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.springframework.context.annotation.Bean;

import org.springframework.context.annotation.Configuration;

import io.syndesis.extension.api.SyndesisExtensionAction;

@Configuration
public class ${extension-name}Extension {
    @Bean
    @SyndesisExtensionAction(
        id = "my-step",
        name = "My Step",
        description = "A simple step",
        entrypoint = "direct:my-step"
    )
    public RouteBuilder myAction() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:my-step")
                    .log("Body is: ${body}");
            }
        };
    }
}
