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

    /*
     * group 1
     * (\\$(?:!)?(?:\\{)?) -> $ (required) ! (optional-nogroup) { (optional-nogroup)
     *
     * group 2
     * ([\\w|_|\\.]+(?:\\|'[\\S ]+')?) -> A-Za-z-0-9|_|. identifier followed by alternate value |'john doe'
     * (see https://velocity.apache.org/engine/devel/user-guide.html#alternate-values)
     *
     * group 3
     * (\\})? -> } (optional)
     */
    private static final Pattern SYMBOL_PATTERN = Pattern.compile("(\\$(?:!)?(?:\\{)?)([\\w_\\.]+(?:\\|'[\\S ]+?')?)(\\})?");

    private static final String SET_LITERAL = "#set(";

    // Record velocity-only symbols to ignore them since they don't require a prefix
    private List<String> vOnlySymbols = new ArrayList<>();

    // Flag if the symbol is a declaration for a velocity-only symbol
    private boolean vSymbolDeclaration;

    public VelocityTemplatePreProcessor() {
        super(
              new SymbolSyntax(DOLLAR_SIGN + OPEN_BRACE, CLOSE_BRACE),
              new SymbolSyntax(DOLLAR_SIGN, EMPTY_STRING));
    }

    @Override
    public boolean isMySymbol(String symbol) {
        Matcher m = SYMBOL_PATTERN.matcher(symbol);
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
    protected void parseSymbol(String symbol) throws TemplateProcessingException {
        if (vSymbolDeclaration) {
            vOnlySymbols.add(symbol);
            // found the declaration so turn off the flag
            vSymbolDeclaration = false;
            append(symbol);
            return;
        }

        if (vOnlySymbols.contains(symbol)) {
            // Ignore these symbols since they are velocity only
            append(symbol);
            return;
        }

        //
        // Scanner does not delineate between two symbols
        // with no whitespace between so match and loop
        //
        Matcher m = SYMBOL_PATTERN.matcher(symbol);
        while (m.find()) {;
            String aSymbol = m.group();
            SymbolSyntax formalSyntax = getSymbolSyntaxes().get(0);
            if (aSymbol.startsWith(formalSyntax.open()) && ! aSymbol.endsWith(formalSyntax.close())) {
                // Not a valid symbol since it starts with a '${' but doesn't end with a '}'
                throw new TemplateProcessingException("The symbol '" + aSymbol + "' is invalid");
            }

            String ref = m.group(1);
            String replacement = ref + ensurePrefix(m.group(2));
            if (m.group(3) != null) {
                //
                // If formal var definition, ie. ${xyz} then group 3
                // will be the final '{'.
                //
                replacement = replacement + m.group(3);
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
        if (! buf.toString().equals(symbol)) {
            append(buf.toString());
        }
    }
}
