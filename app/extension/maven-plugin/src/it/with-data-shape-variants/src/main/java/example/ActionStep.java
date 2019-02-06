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
package example;

import java.util.ArrayList;
import java.util.List;

import io.syndesis.extension.api.annotations.Action;
import io.syndesis.extension.api.annotations.DataShape;
import org.apache.camel.Body;
import org.apache.camel.Handler;

@Action(
    id = "simple-action",
    name = "simple-action",
    description = "simple-action",
    inputDataShape = @DataShape(
        kind = "java",
        type = "example.ActionStep$In",
        metadata = {
            @DataShape.Meta(key = "variant", value = "element")
        }
    ),
    outputDataShape = @DataShape(
        kind = "java",
        type = "example.ActionStep$Out",
        collectionType = "List",
        collectionClassName = "java.util.ArrayList",
        metadata = {
            @DataShape.Meta(key = "variant", value = "collection")
        },
        variants = {
            @DataShape.Variant(
                kind = "java",
                type = "example.ActionStep$Out",
                metadata = {
                    @DataShape.Meta(key = "variant", value = "element"),
                    @DataShape.Meta(key = "compression", value = "true")
                }
            )
        }
    )
)
public class ActionStep {

    @Handler
    public List<Out> process(@Body In body) {
        List<Out> out = new ArrayList<>();
        out.add(new Out());

        return out;
    }

    public static class In {
    }

    public static class Out {
    }
}
