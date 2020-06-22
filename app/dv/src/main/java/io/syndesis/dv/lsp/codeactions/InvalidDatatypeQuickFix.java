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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.eclipse.lsp4j.CodeAction;
import org.eclipse.lsp4j.CodeActionKind;
import org.eclipse.lsp4j.CodeActionParams;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lsp4j.WorkspaceEdit;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;
import io.syndesis.dv.lsp.parser.DdlAnalyzerException;

public class InvalidDatatypeQuickFix extends AbstractQuickfix {

    @Override
    public List<CodeAction> createCodeActions(CodeActionParams params, DdlAnalyzerException exception) {
        List<CodeAction> actions = new ArrayList<>();
        List<String> dTypeMatches = getPotentialDatatypes(exception.getTargetedString());

        for(String match: dTypeMatches) {
            CodeAction codeAction = new CodeAction("Did you mean '" + match + "' datatype?");
            codeAction.setDiagnostics(Collections.singletonList(exception.getDiagnostic()));
            codeAction.setKind(CodeActionKind.QuickFix);
            Map<String, List<TextEdit>> changes = new HashMap<>();
            TextEdit textEdit = new TextEdit(exception.getDiagnostic().getRange(), match);
            changes.put(params.getTextDocument().getUri(), Arrays.asList(textEdit));
            codeAction.setEdit(new WorkspaceEdit(changes));
            actions.add(codeAction);
        }

        return actions;
    }

    private static List<String> getPotentialDatatypes(String targetedString) {
        List<String> matches = new ArrayList<String>();
        for(String dType: DdlAnalyzerConstants.DATATYPE_LIST) {
            if(dType.toUpperCase(Locale.US).startsWith(targetedString.toUpperCase(Locale.US))  ) {
                matches.add(dType);
            }
        }
        return matches;
    }
}
