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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage.SymbolSyntax;

class MustacheTemplatePreProcessor extends AbstractTemplatePreProcessor {

    private static final String MUSTACHE_OPEN_DELIMITER = "[[";

    private static final String MUSTACHE_CLOSE_DELIMITER = "]]";

    private static final String DOUBLE_OPEN_BRACE_PATTERN = "\\{\\{";

    private static final String DOUBLE_CLOSE_BRACE_PATTERN = "\\}\\}";

    private static final Pattern LITERAL_PATTERN = Pattern.compile(
      "(?<leading>.*?)" + // Leading text / punctuation
      "(?<otag>" + DOUBLE_OPEN_BRACE_PATTERN + "(?:\\/|#|\\^|>)?)" + // {{ + any syntax for open section etc...
      "(?<symbol>.*?)" + // Actual symbol name
      "(?<ctag>" + DOUBLE_CLOSE_BRACE_PATTERN + ")"
    );

    private static final Pattern SYMBOL_OPEN_SECTION_PATTERN = Pattern.compile(
      DOUBLE_OPEN_BRACE_PATTERN + "(#|\\^)");

    private static final Pattern SYMBOL_CLOSE_SECTION_PATTERN = Pattern.compile(
      DOUBLE_OPEN_BRACE_PATTERN + "\\/");

    private boolean inSectionSymbol;

    public MustacheTemplatePreProcessor() {
        super(new SymbolSyntax(OPEN_BRACE + OPEN_BRACE, CLOSE_BRACE + CLOSE_BRACE));
    }

    private boolean isOpeningSectionSymbol(String literal) {
        Matcher m = SYMBOL_OPEN_SECTION_PATTERN.matcher(literal);
        return m.matches();
    }

    private boolean isClosingSectionSymbol(String literal) {
        Matcher m = SYMBOL_CLOSE_SECTION_PATTERN.matcher(literal);
        return m.matches();
    }

    @Override
    protected boolean isText(String token) {
        SymbolSyntax symbolSyntax = getSymbolSyntaxes().get(0);
        return !token.startsWith(symbolSyntax.open()) &&
                        !token.contains(symbolSyntax.close());
    }

    @Override
    protected void parseSymbol(String literal) throws TemplateProcessingException {
        //
        // Scanner does not delineate between two symbols
        // with no whitespace between so match and loop
        //
        Matcher m = LITERAL_PATTERN.matcher(literal);
        while (m.find()) {
            String leading = m.group("leading");
            String otag = m.group("otag");
            String symbol = m.group("symbol");
            String ctag = m.group("ctag");

            append(leading);

            checkValidTags(otag, symbol, ctag);

            if (isClosingSectionSymbol(otag)) { // check that the otag denotes a closing section
                inSectionSymbol = false;
            }

            if (inSectionSymbol) {
                //
                // Any symbol within another symbol, eg. section symbol,
                // should not have a prefix.
                //
                append(otag).append(symbol).append(ctag);
            } else {
                String replacement = otag + ensurePrefix(symbol) + ctag;

                StringBuffer buf = new StringBuffer();
                m.appendReplacement(buf, Matcher.quoteReplacement(replacement));
                append(buf.toString());
            }

            if (isOpeningSectionSymbol(otag)) {
                inSectionSymbol = true;
            }
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

    @SuppressWarnings("PMD.PrematureDeclaration")
    @Override
    public String preProcess(String template) throws TemplateProcessingException {
        String newTemplate = super.preProcess(template);
        //
        // Invalid template is inSectionSymbol has not been terminated
        //
        if (inSectionSymbol) {
           throw new TemplateProcessingException("The template is invalid since a section has not been closed");
        }

        /*
         * Mustache endpoint has a conflict with ProcessorDefinition.
         *
         * Once returned, the route (ProcessorDefinition) is resolved. This resolution
         * looks for any properties delimited by {{ and }}, which is the default syntax
         * for mustache properties. Thus, resolution of the mustache properties is
         * incorrectly attempted and fails since these properties are meant for mustache
         * and not camel.
         *
         * Changing the mustache delimiter patterns avoids this problem and allows the
         * properties to be resolved later by mustache.
         */
        newTemplate = newTemplate.replaceAll(DOUBLE_OPEN_BRACE_PATTERN, MUSTACHE_OPEN_DELIMITER);
        newTemplate = newTemplate.replaceAll(DOUBLE_CLOSE_BRACE_PATTERN, MUSTACHE_CLOSE_DELIMITER);
        return newTemplate;
    }

    @Override
    public boolean isMySymbol(String literal) {
        Matcher m = LITERAL_PATTERN.matcher(literal);
        return m.lookingAt();
    }

    @Override
    public void reset() {
        super.reset();
        inSectionSymbol = false;
    }

    @Override
    public Map<String, Object> getUriParams() {
        /*
         * Need to specify the start and end delimiters since we have
         * modified the template symbols.
         */
        Map<String, Object> params = new HashMap<>();
        params.put("startDelimiter", MUSTACHE_OPEN_DELIMITER);
        params.put("endDelimiter", MUSTACHE_CLOSE_DELIMITER);
        return params;
    }
}
