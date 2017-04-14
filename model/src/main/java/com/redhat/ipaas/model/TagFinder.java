package com.redhat.ipaas.model;

import java.util.Set;
import java.util.TreeSet;

/**
 * Finds the unique set of tags set on IPaas entities WithTags, which are
 * Connections and Integrations.
 *
 */
public class TagFinder {

    private Set<String> tags = new TreeSet<String>();
    
    public TagFinder add(ListResult<? extends WithTags> items) {
        if (items.getItems()!=null) {
            for (WithTags item : items.getItems()) {
                if (item.getTags().isPresent()) {
                    for (String tag: (Set<String>) item.getTags().get()) {
                        tags.add(tag);
                    }
                }
            }
        }
        return this;
    }
    
    public ListResult<String> getResult() {
        return ListResult.of(tags);
    }
    
}
