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
package io.syndesis.common.model.integration.step.template;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage.SymbolSyntax;

class FreeMarkerTemplatePreProcessor extends AbstractTemplatePreProcessor {

    private static final Pattern LITERAL_PATTERN = Pattern.compile(
      "(?<leading>.*?)" + // Leading text / punctuation
      "(?<otag>\\$\\{)" + // ${ + any syntax for open section etc...
      "(?<symbol>.*?)" + // Actual symbol name
      "(?<ctag>\\})"
    );

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9_@\\$\\.]+");

    public FreeMarkerTemplatePreProcessor() {
        super(new SymbolSyntax(DOLLAR_SIGN + OPEN_BRACE, CLOSE_BRACE));
    }

    @Override
    protected boolean isText(String token) {
        SymbolSyntax symbolSyntax = getSymbolSyntaxes().get(0);
        return !token.contains(symbolSyntax.open()) &&
                        !token.contains(symbolSyntax.close());
    }

    @Override
    protected void parseSymbol(String literal) throws TemplateProcessingException {
        //
        // Scanner does not delineate between two symbols
        // with no whitespace between so match and loop
        //
        Matcher m = LITERAL_PATTERN.matcher(literal);
        while (m.find()) {;
            String leading = labelledGroup(m, "leading");
            String otag = labelledGroup(m, "otag");
            String symbol = labelledGroup(m, "symbol");
            String ctag = labelledGroup(m, "ctag");

            append(leading);

            checkValidTags(otag, symbol, ctag);

            checkValidSymbol(symbol, SYMBOL_PATTERN);

            String replacement = otag + ensurePrefix(symbol) + ctag;

            // Allows for appending text that comes after
            // the found symbol by using appendTail (see below)
            StringBuffer buf = new StringBuffer();
            m.appendReplacement(buf, Matcher.quoteReplacement(replacement));
            append(buf.toString());
        }

        //
        // Get the tail of the content from the matcher.
        // If the matcher did not match then the tail is
        // the whole symbol so only append the tail if
        // this is not the case
        //
        StringBuffer buf = new StringBuffer();
        m.appendTail(buf);
        if (! buf.toString().equals(literal)) {
            append(buf.toString());
        }
    }

    @Override
    public boolean isMySymbol(String literal) {
        Matcher m = LITERAL_PATTERN.matcher(literal);
        return m.lookingAt();
    }
}
