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
package io.syndesis.server.cli.main;

import io.syndesis.server.cli.command.migrate.MigrateCommand;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;

import com.kakawait.spring.boot.picocli.autoconfigure.PicocliConfigurer;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@SpringBootApplication(scanBasePackageClasses = SyndesisCommandLine.class)
@ComponentScan(basePackageClasses = SyndesisCommandLine.class, includeFilters = @Filter(Command.class))
@SuppressWarnings("PMD.UseUtilityClass")
public class SyndesisCommandLine implements PicocliConfigurer {

    private static final Class<?>[] COMMANDS = {MigrateCommand.class};

    @Override
    public void configure(final CommandLine commandLine) {
        for (final Class<?> command : COMMANDS) {
            final Object cmd;
            try {
                cmd = command.getDeclaredConstructor().newInstance();
            } catch (final ReflectiveOperationException e) {
                throw new IllegalStateException("Unable to instantiate", e);
            }
            final Command annotation = command.getAnnotation(Command.class);
            commandLine.addSubcommand(annotation.name(), cmd);
        }
    }

    public static void main(final String[] args) {
        SpringApplication.run(SyndesisCommandLine.class, args);
    }
}
