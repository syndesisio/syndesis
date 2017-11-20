/*
 * Copyright (C) 2016 Red Hat, Inc.
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

package io.syndesis.project.converter.visitor;

import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class StepVisitorFactoryRegistry {
    private final ConcurrentMap<String, StepVisitorFactory> MAP = new ConcurrentHashMap<>();
    private final StepVisitorFactory FALLBACK_STEP_FACTORY = new GenericStepVisitor.Factory();

    public StepVisitorFactoryRegistry(StepVisitorFactory... factories) {
        for (StepVisitorFactory factory:  factories) {
            register(factory);
        }
    }

    public StepVisitorFactoryRegistry(List<StepVisitorFactory> factories) {
        factories.forEach(this::register);
    }

    public void register(StepVisitorFactory factory) {
        MAP.put(factory.getStepKind(), factory);
    }

    public StepVisitorFactory get(String kind) {
        return MAP.computeIfAbsent(kind, k -> {
            for (StepVisitorFactory factory : ServiceLoader.load(StepVisitorFactory.class, Thread.currentThread().getContextClassLoader())) {
                if (factory.getStepKind().equals(k)) {
                    return factory;
                }
            }
            return FALLBACK_STEP_FACTORY;
        });
    }
}
