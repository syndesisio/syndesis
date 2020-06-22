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
package io.syndesis.dv.lsp.codeactions;

import java.util.List;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionParams;

import io.syndesis.dv.lsp.parser.DdlAnalyzerException;

public abstract class AbstractQuickfix {

    /**
     * returns a replacement string value for the targeted error string
     * the default behavior is to return the current value
     * implementations must override this behavior to change the value
     * @param currentValue
     * @return
     */
    protected String getReplacementValue(String currentValue) {
        return currentValue;
    }

    /**
     * returns a {@link CodeAction} for the provided parameters and information
     * available from the {@link DdlAnalyzerException}
     * @param params
     * @param exception
     * @return
     */
    abstract List<CodeAction> createCodeActions(
            CodeActionParams params,
            DdlAnalyzerException exception);

}
