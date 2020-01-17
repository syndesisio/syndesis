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
package io.syndesis.dv.lsp.completion.providers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;

public class SampleTableCompletionProvider {

    public SampleTableCompletionProvider() {
        super();
    }

    private static final String[] PARTS_ITEM_DATA = {"parts", null, null, "parts", null };
    private static final String[] SUPPLIER_ITEM_DATA = { "supplier", null, null, "supplier", null };
    private static final String[] CUSTOMER_ITEM_DATA = { "customer", null, null, "customer", null };
    private static final String[] SHIP_VIA_ITEM_DATA = { "ship_via", null, null, "ship_via", null };
    private static final String[] PARTS_SUPPLIER_ITEM_DATA = { "partssupplier", null, null, "partssupplier", null };

    static public List<CompletionItem> getCompletionItems() {
        
        List<CompletionItem> sampleTableItems = new ArrayList<CompletionItem>();
        
        sampleTableItems.add(getCompletionItem(PARTS_ITEM_DATA) );
        sampleTableItems.add(getCompletionItem(SUPPLIER_ITEM_DATA) );
        sampleTableItems.add(getCompletionItem(CUSTOMER_ITEM_DATA) );
        sampleTableItems.add(getCompletionItem(SHIP_VIA_ITEM_DATA) );
        sampleTableItems.add(getCompletionItem(PARTS_SUPPLIER_ITEM_DATA) );

        return sampleTableItems;
    }

    public static CompletionItem getCompletionItem(String[] itemInfo) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel(itemInfo[0]);
        ci.setKind(CompletionItemKind.Field);
        ci.setDetail(itemInfo[2]);
        ci.setDocumentation(itemInfo[3]);
        return ci;
    }
}
