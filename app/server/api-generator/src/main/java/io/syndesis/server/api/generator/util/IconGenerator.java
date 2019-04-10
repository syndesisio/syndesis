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
package io.syndesis.server.api.generator.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.common.util.SyndesisServerException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.github.mustachejava.MustacheNotFoundException;
import com.google.common.escape.Escaper;
import com.google.common.io.CharStreams;
import com.google.common.net.PercentEscaper;

public final class IconGenerator {

    private static final String[] COLORS = {"#cc0000", "#a30000", "#8b0000", "#470000", "#2c0000", "#ec7a08", "#b35c00", "#773d00",
        "#3b1f00", "#b58100", "#795600", "#3d2c00", "#6ca100", "#486b00", "#253600", "#3f9c35", "#2d7623", "#1e4f18", "#0f280d", "#007a87",
        "#005c66", "#003d44", "#001f22", "#00b9e4", "#008bad", "#005c73", "#002d39", "#8461f7", "#703fec", "#582fc0", "#40199a", "#1f0066"};

    private static final Escaper ESCAPER = new PercentEscaper("", false);

    private static final Map<Character, String> LETTERS = loadLetters();

    private static final Logger LOG = LoggerFactory.getLogger(IconGenerator.class);

    private static final MustacheFactory MUSTACHE_FACTORY = new DefaultMustacheFactory(resourceName -> {
        final InputStream resourceStream = IconGenerator.class.getResourceAsStream(resourceName);

        if (resourceStream == null) {
            return null;
        }

        return new InputStreamReader(resourceStream, StandardCharsets.UTF_8);
    });

    private IconGenerator() {
        // utility class
    }

    public static String generate(final String template, final String name) {
        Mustache mustache;
        try {
            mustache = MUSTACHE_FACTORY.compile("/icon-generator/" + template + ".svg.mustache");
        } catch (final MustacheNotFoundException e) {
            LOG.warn("Unable to load icon template for: `{}`, will use default template", template);
            LOG.debug("Unable to load icon template for: {}", template, e);

            mustache = MUSTACHE_FACTORY.compile("/icon-generator/default.svg.mustache");
        }

        final Map<String, String> data = new HashMap<>();
        final String color = COLORS[(int) (Math.random() * COLORS.length)];
        data.put("color", color);

        data.put("letter", LETTERS.get(Character.toUpperCase(name.charAt(0))));

        try (StringWriter icon = new StringWriter()) {
            mustache.execute(icon, data).flush();

            final String trimmed = trimXml(icon.toString());

            return "data:image/svg+xml," + ESCAPER.escape(trimmed);
        } catch (final IOException e) {
            throw new SyndesisServerException("Unable to generate icon from template `" + template + "`, for name: " + name, e);
        }
    }

    static String trimXml(final String xml) {
        return xml.trim().replaceAll(">\\s*<", "><").replaceAll("\\s\\s+", " ").replaceAll(" />", "/>");
    }

    private static Map<Character, String> loadLetters() {
        final Map<Character, String> letters = new HashMap<>();

        for (char ch = 'A'; ch <= 'Z'; ch++) {
            try (InputStream letterStream = IconGenerator.class.getResourceAsStream("/icon-generator/" + ch + ".svg");
                InputStreamReader letterReader = new InputStreamReader(letterStream, StandardCharsets.UTF_8)) {
                final String path = CharStreams.toString(letterReader);

                letters.put(ch, path);
            } catch (final IOException e) {
                throw new IllegalStateException("Unable to load SVG path for letter: " + ch, e);
            }
        }

        return Collections.unmodifiableMap(letters);
    }
}
