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

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage.SymbolSyntax;

abstract class AbstractTemplatePreProcessor implements TemplateStepPreProcessor {

    private final List<SymbolSyntax> symbolSyntax;

    @SuppressWarnings("PMD.AvoidStringBufferField")
    private StringBuilder sb = new StringBuilder();

    private SymbolSyntax onlyPartial;

    protected AbstractTemplatePreProcessor(SymbolSyntax... symbolSyntax) {
       this.symbolSyntax = Arrays.asList(symbolSyntax);
    }

    protected StringBuilder append(String token) {
        if (token == null) {
            return sb;
        }

        return sb.append(token);
    }

    protected String ensurePrefix(String symbolName) {
        return symbolName.startsWith(BODY_PREFIX) ? symbolName : BODY_PREFIX + symbolName;
    }

    protected String labelledGroup(Matcher m, String label) {
        Optional<String> optional = Optional.ofNullable(m.group(label));
        return optional.orElse(EMPTY_STRING);
    }

    protected void checkValidTags(String otag, String symbol, String ctag) throws TemplateProcessingException {
        List<SymbolSyntax> syntaxes = getSymbolSyntaxes();
        for (SymbolSyntax syntax : syntaxes) {
            if (otag.equals(syntax.open()) && ! ctag.equals(syntax.close())) {
                // Not a valid symbol since formal velocity syntax must start with a '${' & end with a '}'
                throw new TemplateProcessingException("The symbol '" + symbol + "' is invalid");
            }
        }
    }

    protected void checkValidSymbol(String symbol, Pattern validPattern) throws TemplateProcessingException {
        Matcher m = validPattern.matcher(symbol);
        if (! m.matches()) {
            throw new TemplateProcessingException("The symbol '" + symbol + "' is not valid syntactically");
        }
    }

    @Override
    public abstract boolean isMySymbol(String literal);

    private boolean isSymbol(String symbol) throws TemplateProcessingException {
        if (isMySymbol(symbol)) {
            return true;
        }

        for (TemplateStepLanguage language : TemplateStepLanguage.values()) {
            List<SymbolSyntax> languageSyntaxes = language.getSymbolSyntaxes();
            if (symbolSyntax.equals(languageSyntaxes)) {
                continue; // ignore the language that this pre-processor belongs
            }

            if (language.isSymbol(symbol)) {
                throw new TemplateProcessingException("The symbol '" + symbol + "' is invalid as it appears to be the wrong language");
            }
        }

        return false;
    }

    protected abstract boolean isText(String token);

    protected abstract void parseSymbol(String symbol) throws TemplateProcessingException;

    private String checkPartial(String partial, String currentToken) {
        String token = "";

        for (SymbolSyntax sSyntax : getSymbolSyntaxes()) {
            String open = sSyntax.open();
            String close = sSyntax.close();

            if (close.length() == 0) {
                //
                // Some languages don't require a close clause
                // hence impossible for a partial to be valid.
                //
                token = partial;
                this.onlyPartial = null;
                break;
            }

            if (partial.startsWith(open) && partial.contains(close)) {
                //
                // partial is a complete symbol so assign as is
                //
                token = partial;
                onlyPartial = null;
                break;
            }
            else if (partial.startsWith(open) && ! partial.contains(close)) {
                //
                // token may contain a space with could be valid for some languages
                // therefore store the partial token and grab another until complete
                //
                token = partial;
                onlyPartial = sSyntax;
                break;
            }
            else if (partial.contains(close) && sSyntax.equals(onlyPartial)) {
                //
                // Found a partial containing the close and onlyPartial flag matches
                // this syntax so found the end of the token so token is complete
                //
                token = currentToken + SPACE + partial;
                onlyPartial = null;
                break;
            }
            else if (sSyntax.equals(onlyPartial)) {
                //
                // partial contains neither open or close symbols so could be the
                // middle of a symbol since onlyPartial flag is set thus append and
                // continue
                //
                token = currentToken + SPACE + partial;
                break;
            }
        }

        //
        // partial checked against syntaxes available and doesn't conform
        // therefore should be text or unrecognised symbol so not a partial
        //
        if (onlyPartial == null && currentToken == null) {
            token = partial;
        }

        return token;
    }

    @Override
    public String preProcess(String template) throws TemplateProcessingException {
        Scanner lineScanner = new Scanner(template);
        try {
            while(lineScanner.hasNextLine()) {
                String line = lineScanner.nextLine();
                Scanner scanner = new Scanner(line);
                scanner.useDelimiter(SPACE);

                try {
                    String completeToken = null;
                    while(scanner.hasNext()) {
                        String token = scanner.next();

                        //
                        // Some languages allow spaces inside symbols
                        // therefore need to identifier 'partial' symbols
                        // and glue them back together before processing
                        //
                        completeToken = checkPartial(token, completeToken);

                        if (onlyPartial != null) {
                            //
                            // Only time that completeToken is not nullified
                            // since we have identified that token is a partial
                            //
                            continue;
                        }
                        else if (isSymbol(completeToken)) {
                            parseSymbol(completeToken);
                        }
                        else if (isText(completeToken)) {
                            append(completeToken);
                        } else {
                            throw new TemplateProcessingException("The template is invalid due to the string '" + completeToken + "'");
                        }

                        if (scanner.hasNext()) {
                            append(SPACE);
                        }

                        completeToken = null;
                    }

                    if (lineScanner.hasNextLine()) {
                        append(NEW_LINE);
                    }

                    if (onlyPartial != null) {
                        //
                        // We have a partial token left over so cannot be a valid template
                        //
                        throw new TemplateProcessingException("the template is invalid due to an incomplete symbol");
                    }
                } finally {
                    scanner.close();
                }
            }

        } finally {
            lineScanner.close();
        }

        return sb.toString();
    }

    @Override
    public void reset() {
        onlyPartial = null;
        sb = new StringBuilder();
    }

    /*
     * Suppress warning as this method is the default and we want to only override it
     * if parameters are specifically required.
     */
    @SuppressWarnings("PMD.EmptyMethodInAbstractClassShouldBeAbstract")
    @Override
    public Map<String, Object> getUriParams() {
        return null;
    }

    @Override
    public List<SymbolSyntax> getSymbolSyntaxes() {
        return symbolSyntax;
    }
}
