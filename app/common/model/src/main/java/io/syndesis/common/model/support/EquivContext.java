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
package io.syndesis.common.model.support;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

class EquivContext {

    private final String name;

    private final String type;

    private String fail;

    private final List<EquivContext> children = new ArrayList<>();

    private String a;

    private String b;

    public EquivContext(String name, Class<?> klazz) {
        this.name = name;
        this.type = klazz.getSimpleName().replaceAll("Immutable", Equivalencer.EMPTY_STRING);
    }

    public String id() {
        StringBuilder builder = new StringBuilder();
        if (name != null) {
            builder.append(name);
        }

        return builder.append(Equivalencer.COLON).append(type).toString();
    }

    public boolean hasFailed() {
        return this.fail != null;
    }

    public String getFailed() {
        return this.fail + Equivalencer.NEW_LINE +
                        Equivalencer.TAB + "=> " + Equivalencer.QUOTE_MARK + this.a + Equivalencer.QUOTE_MARK + Equivalencer.NEW_LINE +
                        Equivalencer.TAB + "=> " + Equivalencer.QUOTE_MARK + this.b + Equivalencer.QUOTE_MARK + Equivalencer.NEW_LINE;
    }

    private String truncate(String fullText, String diffText, int diffIndex) {
        StringBuilder truncated = new StringBuilder();

        //
        // Adds a portion of the full text at the start of the value
        // then include '...'
        //
        if (diffIndex > 0 && diffIndex <= 10) {
            truncated.append(fullText.substring(0, diffIndex));
        } else if (diffIndex > 10) {
            truncated
                .append(fullText.substring(0, 10))
                .append(Equivalencer.SPACE).append(Equivalencer.ELLIPSE).append(Equivalencer.SPACE);
        }

        if (diffText.length() > 70) {
            truncated.append(diffText.substring(0, 70)).append(Equivalencer.SPACE).append(Equivalencer.ELLIPSE);
        } else {
            truncated.append(diffText);
        }

        return truncated.toString();
    }

    public void setFail(String property, Object a, Object b) {
        this.fail = property;
        String aStr = a.toString();
        String bStr = b.toString();

        int diffPos = StringUtils.indexOfDifference(aStr, bStr);
        if (diffPos < 0) {
            this.a = aStr;
            this.b = bStr;
            return;
        }

        String aDiff = aStr.substring(diffPos);
        String bDiff = bStr.substring(diffPos);

        this.a = truncate(aStr, aDiff, diffPos);
        this.b = truncate(bStr, bDiff, diffPos);
    }

    public List<EquivContext> children() {
        return Collections.unmodifiableList(children);
    }

    public EquivContext addChild(String name, Class<?> type) {
        EquivContext child = new EquivContext(name, type);
        children.add(child);
        return child;
    }
}
