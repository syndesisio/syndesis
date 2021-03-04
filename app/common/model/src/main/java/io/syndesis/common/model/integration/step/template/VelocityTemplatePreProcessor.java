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

class VelocityTemplatePreProcessor extends AbstractTemplatePreProcessor<VelocityContext> {

    private static final Pattern LITERAL_PATTERN = Pattern.compile(
      "(?<leading>.*?)" + // Leading text / punctuation
      "(?<otag>\\$(?:!)?(?:\\{)?)" + // ${ + any syntax for open section etc...
      "(?<symbol>[\\w\\.\\s]+(?:\\|'[\\S ]+?')?)" + // Actual symbol name
      "(?<ctag>\\})?" // Optional closing tag is no brace at start
    );

    private static final Pattern SYMBOL_PATTERN = Pattern.compile("[a-zA-Z][a-zA-Z0-9\\-_\\.]+(?:\\|'[\\S ]+?')?");

    private static final String SET_LITERAL = "#set(";

    VelocityTemplatePreProcessor() {
        super(
              new SymbolSyntax("${", "}"),
              new SymbolSyntax("$", ""));
    }

    @Override
    public boolean isMySymbol(String literal) {
        Matcher m = LITERAL_PATTERN.matcher(literal);
        return m.lookingAt();
    }

    @Override
    protected boolean isText(VelocityContext context, String token) {
        if (SET_LITERAL.equals(token)) {
            context.vSymbolDeclaration = true;
        }
        return !token.contains("$");
    }

    @Override
    protected void parseSymbol(VelocityContext context, String literal, StringBuilder buff) throws TemplateProcessingException {
        if (context.vSymbolDeclaration) {
            context.vOnlySymbols.add(literal);
            // found the declaration so turn off the flag
            context.vSymbolDeclaration = false;
            buff.append(literal);
            return;
        }

        if (context.vOnlySymbols.contains(literal)) {
            // Ignore these symbols since they are velocity only
            buff.append(literal);
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

            buff.append(leading);

            checkValidTags(otag, symbol, ctag);

            checkValidSymbol(symbol, SYMBOL_PATTERN);

            StringBuilder replacement = new StringBuilder(otag)
                .append(ensurePrefix(symbol));

            if (ctag != null) {
                //
                // If formal var definition, ie. ${xyz} then ctag
                // will be the final '}'.
                //
                replacement.append(ctag);
            }

            // Allows for appending text that comes after
            // the found symbol by using appendTail (see below)
            StringBuffer buf = new StringBuffer();
            m.appendReplacement(buf, Matcher.quoteReplacement(replacement.toString()));
            buff.append(buf.toString());
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
            buff.append(buf.toString());
        }
    }

    @Override
    public VelocityContext createContext() {
        return new VelocityContext();
    }
}
