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
package io.syndesis.integration.runtime.templater;

import java.util.Scanner;
import java.util.regex.Matcher;

public class MustacheTemplatePreProcessor implements TemplateMustacheConstants {

    @SuppressWarnings("PMD.AvoidStringBufferField")
    private StringBuilder sb = new StringBuilder();

    private boolean inSectionSymbol;

    private void append(String token) {
        sb.append(token);
    }

    private String ensurePrefix(String symbolName) {
        return symbolName.startsWith(BODY_PREFIX) ? symbolName : BODY_PREFIX + symbolName;
    }

    private boolean isOpeningSectionSymbol(String symbol) {
        Matcher m = SYMBOL_OPEN_SECTION_PATTERN.matcher(symbol);
        return m.matches();
    }

    private boolean isClosingSectionSymbol(String symbol) {
        Matcher m = SYMBOL_CLOSE_SECTION_PATTERN.matcher(symbol);
        return m.matches();
    }

    private boolean hasSymbol(String symbol) {
        Matcher m = SYMBOL_PATTERN.matcher(symbol);
        return m.lookingAt();
    }

    private boolean isText(String token) {
        return !token.contains(OPEN_BRACE + OPEN_BRACE) &&
                        !token.contains(CLOSE_BRACE + CLOSE_BRACE);
    }

    private void parseSymbol(String symbol) {
        //
        // Scanner does not delineate between two symbols
        // with no whitespace between so match and loop
        //
        Matcher m = SYMBOL_PATTERN.matcher(symbol);
        while (m.find()) {
            String aSymbol = m.group();

            if (isClosingSectionSymbol(aSymbol)) {
                inSectionSymbol = false;
            }

            if (inSectionSymbol) {
                //
                // Any symbol within another symbol, eg. section symbol,
                // should not have a prefix.
                //
                append(aSymbol);
            } else {
                String replacement = m.group(1) + ensurePrefix(m.group(2)) + m.group(3);
                StringBuffer buf = new StringBuffer();
                m.appendReplacement(buf, replacement);
                append(buf.toString());
            }

            if (isOpeningSectionSymbol(aSymbol)) {
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
        if (! buf.toString().equals(symbol)) {
            append(buf.toString());
        }
    }

    /**
     * Takes a mustache template and conducts checks to ensure it is compatible
     * with the processing endpoint, eg. ensure 'body' prefix is present on each of
     * the symbols.
     *
     * @param template
     * @return pre-processed template
     * @throws exception if processing fails
     */
    public String preProcess(String template) throws TemplateProcessingException {
        Scanner lineScanner = new Scanner(template);
        try {
            while(lineScanner.hasNextLine()) {
                String line = lineScanner.nextLine();
                Scanner scanner = new Scanner(line);
                scanner.useDelimiter(SPACE);

                try {
                    while(scanner.hasNext()) {
                        String token = scanner.next();

                        if (hasSymbol(token)) {
                            parseSymbol(token);
                        }
                        else if (isText(token)) {
                            append(token);
                        } else {
                            throw new TemplateProcessingException("The template is invalid due to the string '" + token + "'");
                        }

                        if (scanner.hasNext()) {
                            append(SPACE);
                        }
                    }

                    append(NEW_LINE);
                } finally {
                    scanner.close();
                }
            }

        } finally {
            lineScanner.close();
        }

        //
        // Invalid template is inSectionSymbol has not been terminated
        //
        if (inSectionSymbol) {
           throw new TemplateProcessingException("The template in invalid since a section has not been closed");
        }

        return sb.toString();
    }

    /**
     *
     */
    public void reset() {
        sb = new StringBuilder();
        inSectionSymbol = false;
    }
}
