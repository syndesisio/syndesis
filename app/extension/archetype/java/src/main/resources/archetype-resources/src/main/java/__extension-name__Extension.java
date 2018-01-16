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

import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.syndesis.extension.api.SyndesisActionProperty;
import io.syndesis.extension.api.SyndesisExtensionAction;

@SyndesisExtensionAction(
    id = "my-step",
    name = "My Logging Step",
    description = "A simple logging step"
)
public class ${extension-name}Extension {
    private static final Logger LOGGER = LoggerFactory.getLogger(${extension-name}Extension.class);

    @SyndesisActionProperty(
        name = "trace",
        displayName = "Trace",
        description = "Log the body as TRACE level, default INFO")
    private boolean trace;

    public void setTrace(boolean trace) {
        this.trace = trace;
    }

    public boolean isTrace() {
        return this.trace;
    }

    @Handler
    public void log(@Body Object body){
        if(trace) {
            LOGGER.trace("Body is: {}",body);
        } else {
            LOGGER.info("Body is: {}",body);
        }
    }
}
