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
package io.syndesis.server.cli.command;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.kakawait.spring.boot.picocli.autoconfigure.ExitStatus;
import com.kakawait.spring.boot.picocli.autoconfigure.HelpAwarePicocliCommand;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;
import org.springframework.core.env.StandardEnvironment;
import picocli.CommandLine.Command;

public abstract class SyndesisCommand extends HelpAwarePicocliCommand {

    private final String name;

    private final Map<String, Object> parameters = new HashMap<>();

    protected SyndesisCommand() {
        final Command command = getClass().getAnnotation(Command.class);

        name = command.name();
    }

    @Override
    public final ExitStatus call() {
        prepare();

        try (AbstractApplicationContext context = createContext()) {
            final AutowireCapableBeanFactory beanFactory = context.getAutowireCapableBeanFactory();

            beanFactory.autowireBean(this);

            perform();
        }

        return ExitStatus.OK;
    }

    private AbstractApplicationContext createContext() {
        final AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();

        final YamlPropertySourceLoader propertySourceLoader = new YamlPropertySourceLoader();
        final List<PropertySource<?>> yamlPropertySources;
        try {
            yamlPropertySources = propertySourceLoader.load(name, context.getResource("classpath:" + name + ".yml"));
        } catch (final IOException e) {
            throw new IllegalStateException(e);
        }

        final StandardEnvironment environment = new StandardEnvironment();
        final MutablePropertySources propertySources = environment.getPropertySources();
        propertySources.addFirst(new MapPropertySource("parameters", parameters));
        yamlPropertySources.forEach(propertySources::addLast);

        context.setEnvironment(environment);

        final String packageName = getClass().getPackage().getName();
        context.scan(packageName);

        context.refresh();

        return context;
    }

    protected abstract void perform();

    protected abstract void prepare();

    protected final void putParameter(final String key, final String value) {
        if (value != null) {
            parameters.put(key, value);
        }
    }
}
