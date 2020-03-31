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
package io.syndesis.dv.lsp.hover;

import java.util.Collections;
import java.util.function.Function;

import org.eclipse.lsp4j.Hover;
import org.eclipse.lsp4j.jsonrpc.messages.Either;


public class HoverFuture implements Function<Object, Hover> {

    private final String message;

    public HoverFuture(String message) {
        this.message = message;
    }

    @Override
    public Hover apply(Object camelCatalog) {
        Hover hover = new Hover();
        hover.setContents(Collections.singletonList(Either.forLeft(this.message)));
//        ComponentModel componentModel = ModelHelper.generateComponentModel(camelCatalog.componentJSonSchema(uriElement.getComponentName()), true);
//        hover.setContents(Collections.singletonList((Either.forLeft(uriElement.getDescription(componentModel)))));
        return hover;
    }

}
