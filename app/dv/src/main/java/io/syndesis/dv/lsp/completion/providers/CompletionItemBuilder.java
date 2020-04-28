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
import org.eclipse.lsp4j.InsertTextFormat;
import org.eclipse.lsp4j.MarkupContent;
import org.eclipse.lsp4j.MarkupKind;
import org.eclipse.lsp4j.jsonrpc.messages.Either;

import io.syndesis.dv.lsp.completion.DdlCompletionConstants;
import io.syndesis.dv.lsp.completion.providers.items.DdlCompletionItemLoader;

public class CompletionItemBuilder implements DdlCompletionConstants {
    private DdlCompletionItemLoader loader = DdlCompletionItemLoader.getInstance();

    public CompletionItemBuilder() {
    }

    public DdlCompletionItemLoader getItemLoader() {
        return loader;
    }

    public CompletionItem createKeywordItem(String label, String detail, String documentation) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel(label);
        ci.setKind(CompletionItemKind.Keyword);
        if (detail != null) {
            ci.setDetail(detail);
        }
        if (documentation != null) {
            ci.setDocumentation(documentation);
        }
        return ci;
    }

    public CompletionItem createKeywordItemFromItemData(String label) {
        String[] itemData = getItemData(label);
        CompletionItem ci = new CompletionItem();
        ci.setLabel(label);
        ci.setKind(CompletionItemKind.Keyword);

        if( itemData != null ) {
            if( itemData.length > 1 ) {
                String detail = itemData[1];
                if (detail != null) {
                    ci.setDetail(detail);
                }
            }

            if( itemData.length > 2 ) {
                String documentation = itemData[2];
                if (documentation != null) {
                    ci.setDocumentation(documentation);
                }
            }

            if( itemData.length > 3 ) {
                String insertText = itemData[3];
                if (insertText != null) {
                    ci.setInsertText(insertText);
                    ci.setKind(CompletionItemKind.Snippet);
                }
            }
        }

        return ci;
    }

    public CompletionItem createFieldItem(String label, String detail,
            String documentation) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel(label);
        ci.setKind(CompletionItemKind.Field);
        if (detail != null) {
            ci.setDetail(detail);
        }
        if (documentation != null) {
            ci.setDocumentation(documentation);
        }
        return ci;
    }

    public CompletionItem createSnippetItem(String label, String detail,
            String documentation, String insertText) {
        CompletionItem ci = new CompletionItem();
        ci.setLabel(label);
        ci.setKind(CompletionItemKind.Snippet);
        ci.setInsertTextFormat(InsertTextFormat.Snippet);
        ci.setInsertText(insertText);
        if (documentation != null) {
            ci.setDocumentation(documentation);
        } else {
            ci.setDocumentation(CompletionItemBuilder.beautifyDocument(ci.getInsertText()));
        }
        if (detail != null) {
            ci.setDetail(detail);
        }

        return ci;
    }

    public List<CompletionItem> generateCompletionItems(String[] words) {
        List<CompletionItem> items = new ArrayList<CompletionItem>();

        for(String word: words ) {
            items.add(createKeywordItemFromItemData(word));
        }

        return items;
    }

    public CompletionItem generateCompletionItem(String label, String details, String documentation, String insertText) {
        // String[] >> label, detail, documentation, insertText
        if( insertText != null ) {
            return createSnippetItem(label, details, documentation, insertText);
        }
        return createKeywordItem(label, details, documentation);
    }

    /**
     *
     * @param label
     * @return    String[] array >>>>
        String[0] label;
        String[1] detail;
        String[2] documentation;
        String[3] insertText;
        String[4] insertTextFormat;
     */
    public String[] getItemData(String label) {
        String[] result = KEYWORDS_ITEM_DATA.get(label.toUpperCase());

        if( result == null ) {
            result = DATATYPES_ITEM_DATA.get(label);
        }

        return result;
    }

    public String[] getKeywordLabels(int[] keywordIds, boolean upperCase) {
        List<String> labels = new ArrayList<String>();

        for( int id: keywordIds) {
            labels.add(getKeywordLabel(id, upperCase));
        }

        return labels.toArray(new String[0]);
    }

    /**
     * The tokenImage[...] call is returning strings wrapped in double-quotes
     *
     * Need to return a simple string
     * @return string without double quotes
     */
    public String getKeywordLabel(int keywordId, boolean upperCase) {
        return DdlCompletionConstants.getLabel(keywordId, upperCase);
    }

    public List<CompletionItem> setItemsSortText(List<CompletionItem> items, String sortText) {
        for( CompletionItem item: items ) {
            item.setSortText(sortText);
        }
        return items;
    }

    public static Either<String, MarkupContent> beautifyDocument(String raw) {
        // remove the placeholder for the plain cursor like: ${0}, ${1:variable}
        String escapedString = raw.replaceAll("\\$\\{\\d:?(.*?)\\}", "$1");

        MarkupContent markupContent = new MarkupContent();
        markupContent.setKind(MarkupKind.MARKDOWN);
        markupContent.setValue(
                String.format("```%s\n%s\n```", "java", escapedString));
        return Either.forRight(markupContent);
    }

}
