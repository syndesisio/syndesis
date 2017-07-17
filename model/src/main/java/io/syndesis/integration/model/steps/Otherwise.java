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
package io.syndesis.integration.model.steps;

import com.google.auto.service.AutoService;

import java.util.List;

/**
 * Represents the otherwise clause in a {@link Choice}
 */
@AutoService(Step.class)
public class Otherwise extends ChildSteps<Otherwise> {
    public static final String KIND = "otherwise";

    public Otherwise() {
        super(KIND);
    }

    public Otherwise(List<Step> steps) {
        super(KIND, steps);
    }

    @Override
    public String toString() {
        return "Otherwise: " + getSteps();
    }

    public String getKind() {
        return KIND;
    }

}
