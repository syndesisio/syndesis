/*
 * Copyright 2016 Red Hat, Inc.
 * <p>
 * Red Hat licenses this file to you under the Apache License, version
 * 2.0 (the "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
 * implied.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 */
package io.fabric8.funktion.model.steps;

import io.fabric8.funktion.model.StepKinds;

/**
 * Sets the payload
 */
/*
@JsonDeserialize(
        using = JsonDeserializer.None.class
)
*/
public class SetBody extends Step {
    private String body;

    public SetBody() {
        super(StepKinds.SET_BODY);
    }

    public SetBody(String body) {
        this();
        this.body = body;
    }

    @Override
    public String toString() {
        return "SetBody: " + body;
    }

    public String getKind() {
        return "setBody";
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
