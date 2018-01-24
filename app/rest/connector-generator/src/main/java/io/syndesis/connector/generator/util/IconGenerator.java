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
package io.syndesis.connector.generator.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.core.SyndesisServerException;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.google.common.escape.Escaper;
import com.google.common.io.CharStreams;
import com.google.common.net.PercentEscaper;

public final class IconGenerator {

    private static final String[] COLORS = {"#cc0000", "#a30000", "#8b0000", "#470000", "#2c0000", "#ec7a08", "#b35c00", "#773d00",
        "#3b1f00", "#b58100", "#795600", "#3d2c00", "#6ca100", "#486b00", "#253600", "#3f9c35", "#2d7623", "#1e4f18", "#0f280d", "#007a87",
        "#005c66", "#003d44", "#001f22", "#00b9e4", "#008bad", "#005c73", "#002d39", "#8461f7", "#703fec", "#582fc0", "#40199a", "#1f0066"};

    private static final Escaper ESCAPER = new PercentEscaper("", false);

    private static final Map<String, String> LETTERS = loadLetters();

    private static final MustacheFactory MUSTACHE_FACTORY = new DefaultMustacheFactory(
        resourceName -> new InputStreamReader(IconGenerator.class.getResourceAsStream(resourceName), StandardCharsets.UTF_8));

    private IconGenerator() {
        // utility class
    }

    public static String generate(final String template, final String name) {
        final Mustache mustache = MUSTACHE_FACTORY.compile("/icon-generator/" + template + ".svg.mustache");

        final Map<String, String> data = new HashMap<>();
        final String color = COLORS[(int) (Math.random() * COLORS.length)];
        data.put("color", color);

        data.put("letter", LETTERS.get(name.substring(0, 1).toUpperCase()));

        try (StringWriter icon = new StringWriter()) {
            mustache.execute(icon, data).flush();

            final String trimmed = trimXml(icon.toString());

            return "data:image/svg+xml;charset=utf-8," + ESCAPER.escape(trimmed);
        } catch (final IOException e) {
            throw new SyndesisServerException("Unable to generate icon from template `" + template + "`, for name: " + name, e);
        }
    }

    /* default */static String trimXml(final String xml) {
        return xml.replaceAll(">\\s*<", "><").replaceAll("\\s\\s+", " ").replaceAll(" />", "/>");
    }

    private static Map<String, String> loadLetters() {
        final Map<String, String> letters = new HashMap<>();

        for (int i = 'A'; i <= 'Z'; i++) {
            final String letter = String.valueOf((char) i);

            try (final InputStream letterStream = IconGenerator.class.getResourceAsStream("/icon-generator/" + letter + ".svg");
                final InputStreamReader letterReader = new InputStreamReader(letterStream, StandardCharsets.UTF_8)) {
                final String path = CharStreams.toString(letterReader);

                letters.put(letter, path);
            } catch (final IOException e) {
                throw new IllegalStateException("Unable to load SVG path for letter: " + letter, e);
            }
        }

        return Collections.unmodifiableMap(letters);
    }
}
