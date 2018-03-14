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
package io.syndesis.extension.example.log;

import java.io.IOException;

import com.github.lalyos.jfiglet.FigletFont;
import io.syndesis.extension.api.annotations.ConfigurationProperty;
import io.syndesis.extension.api.annotations.Action;
import org.apache.camel.Body;
import org.apache.camel.Handler;
import org.apache.camel.util.ObjectHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Action(id = "log-body", name = "simple-log", description = "A simple POJO based logging extension")
public class LogStep {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogStep.class);

    // ************************
    // Extension Properties
    // ************************

    @ConfigurationProperty(name = "ascii", displayName = "ascii", description = "Ascii")
    private boolean ascii;

    @ConfigurationProperty(name = "font", displayName = "font", description = "Font")
    private String font;

    // ************************
    // Accessors
    // ************************

    public void setAscii(boolean ascii) {
        this.ascii = ascii;
    }

    public boolean isAscii() {
        return this.ascii;
    }

    public String getFont() {
        return font;
    }

    public void setFont(String font) {
        this.font = font;
    }

    // ************************
    // Extension
    // ************************

    @Handler
    public void log(@Body String body) {
        try {
            if (ascii) {
                if (ObjectHelper.isNotEmpty(font)) {
                    LOGGER.info("Body is: \n{}", FigletFont.convertOneLine(font, body));
                } else {
                    LOGGER.info("Body is: \n{}", FigletFont.convertOneLine(body));
                }
            } else {
                LOGGER.info("Body is: {}", body);
            }
        } catch (IOException e) {
            throw ObjectHelper.wrapRuntimeCamelException(e);
        }
    }
}
