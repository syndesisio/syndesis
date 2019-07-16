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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage.SymbolSyntax;

class VelocityTemplatePreProcessor extends AbstractTemplatePreProcessor {

    private static final Pattern LITERAL_PATTERN = Pattern.compile(
      "(?<leading>.*?)" + // Leading text / punctuation
      "(?<otag>\\$(?:!)?(?:\\{)?)" + // ${ + any syntax for open section etc...
      "(?<symbol>[\\w\\.\\s]+(?:\\|'[\\S ]+?')?)" + // Actual symbol name
      "(?<ctag>\\})?" // Optional closing tag is no brace at start
    );

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9\\-_\\.]+(?:\\|'[\\S ]+?')?");

    private static final String SET_LITERAL = "#set(";

    // Record velocity-only symbols to ignore them since they don't require a prefix
    private final List<String> vOnlySymbols = new ArrayList<>();

    // Flag if the symbol is a declaration for a velocity-only symbol
    private boolean vSymbolDeclaration;

    public VelocityTemplatePreProcessor() {
        super(
              new SymbolSyntax(DOLLAR_SIGN + OPEN_BRACE, CLOSE_BRACE),
              new SymbolSyntax(DOLLAR_SIGN, EMPTY_STRING));
    }

    @Override
    public boolean isMySymbol(String literal) {
        Matcher m = LITERAL_PATTERN.matcher(literal);
        return m.lookingAt();
    }

    @Override
    protected boolean isText(String token) {
        if (SET_LITERAL.equals(token)) {
            vSymbolDeclaration = true;
        }
        return !token.contains(DOLLAR_SIGN);
    }

    @SuppressWarnings("PMD.UseStringBufferForStringAppends")
    @Override
    protected void parseSymbol(String literal) throws TemplateProcessingException {
        if (vSymbolDeclaration) {
            vOnlySymbols.add(literal);
            // found the declaration so turn off the flag
            vSymbolDeclaration = false;
            append(literal);
            return;
        }

        if (vOnlySymbols.contains(literal)) {
            // Ignore these symbols since they are velocity only
            append(literal);
            return;
        }

        //
        // Scanner does not delineate between two symbols
        // with no whitespace between so match and loop
        //
        Matcher m = LITERAL_PATTERN.matcher(literal);
        while (m.find()) {
            String leading = labelledGroup(m, "leading");
            String otag = labelledGroup(m, "otag");
            String symbol = labelledGroup(m, "symbol");
            String ctag = labelledGroup(m, "ctag");

            append(leading);

            checkValidTags(otag, symbol, ctag);

            checkValidSymbol(symbol, SYMBOL_PATTERN);

            String replacement = otag + ensurePrefix(symbol);
            if (ctag != null) {
                //
                // If formal var definition, ie. ${xyz} then ctag
                // will be the final '}'.
                //
                replacement = replacement + ctag;
            }

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
}
