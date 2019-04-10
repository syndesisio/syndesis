/**
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.syndesis.server.builder.maven;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

@Mojo(name = "extract-tooltips", defaultPhase = LifecyclePhase.GENERATE_RESOURCES)
public class TooltipExtractorMojo extends AbstractMojo {
    @Parameter(required = true)
    protected List<File> sources;

    @Parameter(required = true)
    protected File output;

    @Parameter(defaultValue = "start")
    protected String tooltipBeginTag = "start";

    @Parameter(defaultValue = "stop")
    protected String tooltipEndTag = "stop";

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            final TooltipExtractor consumer = new TooltipExtractor();

            for (File path: sources) {
                try (Stream<String> lines = Files.lines(path.toPath())) {
                    lines.forEach(consumer);
                }
            }

            consumer.write(Files.newBufferedWriter(output.toPath(), StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new MojoFailureException(e.getMessage(), e);
        }
    }

    private final class TooltipExtractor implements Consumer<String> {
        private final Pattern pattern = Pattern.compile("^// (.*?)\\:(.*)");
        private final ObjectMapper mapper = new ObjectMapper();

        private boolean isTooltip;
	@SuppressWarnings("PMD.AvoidStringBufferField")
        private StringBuilder tooltip = new StringBuilder();
        private Map<String, Object> root = new HashMap<>();

        @Override
        public void accept(String line) {
            Matcher matcher = pattern.matcher(line);
            if (matcher.matches()) {
                final String entry = matcher.group(1); // i.e. address.queue.label
                final String value = matcher.group(2);  // the text after the first :
                final String[] keys = entry.split("\\.", -1);

                Map<String, Object> node = root;
                for (int i = 0; i < keys.length; i++) {
                    if (i < keys.length - 1) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> tmpNode = (Map)node.computeIfAbsent(keys[i], k -> new HashMap<>());
                        node = tmpNode;
                    } else {
                        if (tooltipBeginTag.equals(value)) {
                            isTooltip = true;
                            tooltip.setLength(0);
                            continue;
                        } else if (tooltipEndTag.equals(value)) {
                            isTooltip = false;
                            node.put(keys[i], tooltip.toString());
                            continue;
                        } else {
                            node.put(keys[i], value);
                        }
                    }
                }
            } else if (isTooltip) {
                if (tooltip.length() > 0) {
                    tooltip.append(' ');
                }

                tooltip.append(line.trim());
            }
        }

        public void write(Writer writer) throws IOException {
            mapper.writer().withDefaultPrettyPrinter().writeValue(writer, root);
        }
    }
}
