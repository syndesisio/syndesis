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

import java.util.List;
import java.util.Map;
import io.syndesis.common.model.integration.step.template.TemplateStepLanguage.SymbolSyntax;

public interface TemplateStepPreProcessor<C extends ProcessingContext> {

    /**
     * Takes a template and conducts checks to ensure it is compatible
     * with the processing endpoint, eg. ensure 'body' prefix is present on each of
     * the symbols.
     * @throws exception if processing fails
     */
    String preProcess(String template) throws TemplateProcessingException;

    /**
     * Parameters required for the endpoint
     */
    Map<String, Object> getUriParams();

    /**
     * Syntaxes for the template language.
     * @return the syntax for the language's symbol, eg. '{{...}}' or '${...}'
     * Note: returns an array since languages can have more than one
     */
    List<SymbolSyntax> getSymbolSyntaxes();

    /**
     * Determines if the given symbol is recognised by this pre-processor.
     * @return whether the given symbol is recognised by this pre-processor
     */
    boolean isMySymbol(String symbol);

    C createContext();
}
