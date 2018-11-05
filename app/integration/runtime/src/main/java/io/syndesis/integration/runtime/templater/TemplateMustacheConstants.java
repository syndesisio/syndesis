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

import java.util.regex.Pattern;
import io.syndesis.common.util.StringConstants;

@SuppressWarnings("PMD.ConstantsInInterface")
public interface TemplateMustacheConstants extends StringConstants {

    String BODY_PREFIX = "body" + DOT;

    String DOUBLE_OPEN_BRACE_PATTERN = "\\{\\{";

    String DOUBLE_CLOSE_BRACE_PATTERN = "\\}\\}";

    Pattern SYMBOL_PATTERN = Pattern.compile("(" + DOUBLE_OPEN_BRACE_PATTERN + "(?:\\/|#|\\^|>)?)(.*?)(" + DOUBLE_CLOSE_BRACE_PATTERN + ")");

    Pattern SYMBOL_OPEN_SECTION_PATTERN = Pattern.compile(DOUBLE_OPEN_BRACE_PATTERN + "(#|\\^)(.*?)" + DOUBLE_CLOSE_BRACE_PATTERN);

    Pattern SYMBOL_CLOSE_SECTION_PATTERN = Pattern.compile(DOUBLE_OPEN_BRACE_PATTERN + "\\/(.*?)" + DOUBLE_CLOSE_BRACE_PATTERN);
}
