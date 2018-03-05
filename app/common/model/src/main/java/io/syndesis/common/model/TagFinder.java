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
package io.syndesis.common.model;

import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Finds the unique set of tags set on Syndesis entities WithTags, which are
 * Connections and Integrations.
 *
 */
public class TagFinder {

    private final SortedSet<String> tags = new TreeSet<String>();

    public TagFinder add(ListResult<? extends WithTags> items) {
        if (items.getItems()!=null) {
            for (WithTags item : items.getItems()) {
                for (String tag: item.getTags()) {
                    tags.add(tag);
                }
            }
        }
        return this;
    }

    public ListResult<String> getResult() {
        return ListResult.of(tags);
    }

}
