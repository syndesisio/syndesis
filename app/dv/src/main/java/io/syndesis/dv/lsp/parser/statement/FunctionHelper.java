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
package io.syndesis.dv.lsp.parser.statement;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.CompletionItem;

import com.google.common.base.Splitter;

import io.syndesis.dv.lsp.completion.providers.items.DdlCompletionItemLoader;

/*
 * A helper class to hold and provide function definition info
 */
public final class FunctionHelper {

    private static final FunctionHelper INSTANCE = new FunctionHelper();

    private final DdlCompletionItemLoader loader = DdlCompletionItemLoader.getInstance();

    List<FunctionData> functionDataList;

    private FunctionHelper() {

    }

    public static FunctionHelper getInstance() {
        return INSTANCE;
    }

    public List<FunctionData> getFunctionData() {
        if (functionDataList == null) {
            load();
        }

        return this.functionDataList;
    }

    private void load() {
        this.functionDataList = new ArrayList<FunctionData>();

        for (CompletionItem nextItem : loader.getFunctionCompletionItems()) {
            FunctionData data = getFunctionData(nextItem);
            if (data != null) {
                functionDataList.add(data);
            }
        }
    }

    private static FunctionData getFunctionData(CompletionItem item) {
        String labelNoSpaces = item.getLabel().replace(" ", "");
        // Label should be defined like: aes_encryptparam1, param2)
        // Validate the format
        String name = labelNoSpaces.substring(0, labelNoSpaces.indexOf('('));
        String body = labelNoSpaces.substring(labelNoSpaces.indexOf('(') + 1, labelNoSpaces.indexOf(')'));

        int numParams = 0;
        if (body.length() > 0) {
            List<String> params = Splitter.on(',').splitToList(body);
            numParams = params.size();
        }

        return new FunctionData(name, numParams);
    }

    public boolean isFunction(String name, int numArgs) {
        for (FunctionData data : getFunctionData()) {
            if (name.equalsIgnoreCase(data.getName()) && numArgs == data.getNumArgs()) {
                return true;
            }
        }
        return false;
    }

    public boolean isFunctionName(String name) {
        for (FunctionData data : getFunctionData()) {
            if (name.equalsIgnoreCase(data.getName())) {
                return true;
            }
        }
        return false;
    }
}
