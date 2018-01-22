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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.function.Consumer;

import io.syndesis.core.SyndesisServerException;

import org.apache.batik.script.Interpreter;
import org.apache.batik.transcoder.SVGAbstractTranscoder;
import org.apache.batik.transcoder.Transcoder;
import org.apache.batik.transcoder.TranscoderException;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.TranscoderOutput;
import org.apache.batik.transcoder.image.PNGTranscoder;

public final class IconGenerator {

    /* default */ static final class CustomTranscoder extends PNGTranscoder {

        private final Consumer<Interpreter> hook;

        /* default */ CustomTranscoder(final Consumer<Interpreter> hook) {
            this.hook = hook;
        }

        @Override
        protected void setImageSize(final float docWidth, final float docHeight) {
            // convenient method that is invoked (execution-wise) after the
            // scripts have been parsed
            super.setImageSize(docWidth, docHeight);

            final Interpreter interpreter = ctx.getInterpreter("text/ecmascript");

            hook.accept(interpreter);
        }
    }

    private IconGenerator() {
        // utility class
    }

    public static String generate(final String template, final String name) {
        final Transcoder transcoder = new CustomTranscoder(interpreter -> interpreter.evaluate("applyTemplate('" + name + "')"));

        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_HEIGHT, 200f);
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_WIDTH, 200f);
        transcoder.addTranscodingHint(SVGAbstractTranscoder.KEY_EXECUTE_ONLOAD, true);

        try (InputStream in = IconGenerator.class.getResourceAsStream("/icon-generator/" + template + ".svg");
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            final TranscoderInput input = new TranscoderInput(in);

            final TranscoderOutput output = new TranscoderOutput(out);

            transcoder.transcode(input, output);

            return "data:image/png;base64," + Base64.getEncoder().encodeToString(out.toByteArray());
        } catch (IOException | TranscoderException e) {
            throw new SyndesisServerException("Unable to generate icon", e);
        }
    }
}
