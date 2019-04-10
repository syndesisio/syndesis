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
package io.syndesis.server.endpoint.v1.handler.support;

import java.io.BufferedReader;
import java.io.FilterReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// This class takes a reader and a pattern and removes lines
// that don't match the pattern.
// Line terminators are converted to a \n.
public class RegexBasedMasqueradeReader extends FilterReader {
    // This variable holds the current line.
    // If null and emitNewline is false, a newline must be fetched.
    String curLine;

    // This is the index of the first unread character in curLine.
    // If at any time curLineIx == curLine.length, curLine is set to null.
    int curLineIx;

    // If true, the newline at the end of curLine has not been returned.
    // It would have been more convenient to append the newline
    // onto freshly fetched lines. However, that would incur another
    // allocation and copyObjectMapperConfiguration.
    boolean emitNewline;

    // Matcher used to test every line
    Matcher matcher;

    public RegexBasedMasqueradeReader(BufferedReader in, String patternStr) {
        super(in);
        Pattern pattern = Pattern.compile(patternStr);
        matcher = pattern.matcher("");
    }

    // This overridden method fills sharedBuf with characters read from in.
    @Override
    @SuppressWarnings({"PMD.CyclomaticComplexity", "PMD.ModifiedCyclomaticComplexity", "PMD.StdCyclomaticComplexity", "PMD.NPathComplexity"})
    public int read(char sharedBuf[], int offset, int len) throws IOException {
        int off = offset;
        // Fetch new line if necessary
        if (curLine == null) {
            curLine = ((BufferedReader) in).readLine();
            curLineIx = 0;
        }

        // Return characters from current line
        if (curLine != null) {

            int start = Integer.MAX_VALUE;
            int end = Integer.MAX_VALUE;
            matcher.reset(curLine);
            List<Integer> matches = new ArrayList<>();
            while (matcher.find()) {
                MatchResult matchResult = matcher.toMatchResult();
                matches.add(matchResult.start());
                matches.add(matchResult.end());
            }

            int matchesIdx = 0;
            if (!matches.isEmpty()) {
                start = matches.get(matchesIdx++);
                end = matches.get(matchesIdx++);
            }

            int num = Math.min(len, Math.min(sharedBuf.length - off,
                curLine.length() - curLineIx));
            // Copy characters from curLine to sharedBuf
            for (int i = 0; i < num; i++) {
                if (curLineIx <= start || curLineIx > end - 1) {
                    sharedBuf[off++] = curLine.charAt(curLineIx);
                } else {
                    if (curLineIx == end - 1) {
                        if (matchesIdx == matches.size()) {
                            start = Integer.MAX_VALUE;
                            end = Integer.MAX_VALUE;
                        } else {
                            start = matches.get(matchesIdx++);
                            end = matches.get(matchesIdx++);
                        }
                    }
                    sharedBuf[off++] = '*';
                }
                curLineIx++;
            }

            // No more characters in curLine
            if (curLineIx == curLine.length()) {
                curLine = null;

                // Is there room for the newline?
                if (num < len && off < sharedBuf.length) {
                    sharedBuf[off++] = '\n';
                    emitNewline = false;
                    num++;
                }
            }

            // Return number of character read
            return num;
        } else if (emitNewline && len > 0) {
            // Emit just the newline
            sharedBuf[off] = '\n';
            emitNewline = false;
            return 1;
        } else if (len > 0) {
            // No more characters left in input reader
            return -1;
        } else {
            // Client did not ask for any characters
            return 0;
        }
    }

    @Override
    public boolean ready() throws IOException {
        return curLine != null || emitNewline || in.ready();
    }

    @Override
    public boolean markSupported() {
        return false;
    }


}
