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
package io.syndesis.cli;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.ComponentScan.Filter;
import org.springframework.context.annotation.FilterType;

import com.kakawait.spring.boot.picocli.autoconfigure.ExitStatus;
import com.kakawait.spring.boot.picocli.autoconfigure.HelpAwarePicocliCommand;

import picocli.CommandLine.Command;
import picocli.CommandLine.Help.Ansi;

@SpringBootApplication
@ComponentScan(includeFilters = @Filter(type = FilterType.ANNOTATION, value = Command.class),
    excludeFilters = @Filter(pattern = "io\\.syndesis\\.cli\\..*Configuration", type = FilterType.REGEX))
@SuppressWarnings("PMD.UseUtilityClass")
public class SyndesisCommandLine {

    @Command
    static class NullCommand extends HelpAwarePicocliCommand {
        @Override
        public ExitStatus call() {
            if (getParsedCommands().size() == 1) {
                getContext().usage(System.out, Ansi.AUTO);
                return ExitStatus.TERMINATION;
            }

            return ExitStatus.OK;
        }
    }

    public static void main(final String[] args) {
        SpringApplication.run(SyndesisCommandLine.class, args);
    }
}
