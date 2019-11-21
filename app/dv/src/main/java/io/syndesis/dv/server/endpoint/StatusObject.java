/*
 * Copyright (C) 2013 Red Hat, Inc.
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
package io.syndesis.dv.server.endpoint;

import java.util.LinkedHashMap;
import java.util.Map;

import org.teiid.core.util.ArgCheck;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;


/**
 * Object that provides key/message pairs
 */
@JsonSerialize(as = StatusObject.class)
@JsonInclude(Include.NON_NULL)
public class StatusObject {

    private String title;
    private Map<String, String> attributes = new LinkedHashMap<>();

    /**
     * Default constructor for deserialization
     */
    public StatusObject() {
        // do nothing
    }

    /**
     * @param title the subject of this status object
     *
     */
    public StatusObject(String title) {
        ArgCheck.isNotNull(title);
        this.title = title;
    }

    /**
     * @return the attributes
     */
    public Map<String, String> getAttributes() {
        return this.attributes;
    }

    /**
     * Add a message pair with a prefixed subject
     *
     * @param subject the subject of the message
     * @param message the message
     */
    public void addAttribute(String subject, String message) {
        attributes.put(subject, message);
    }

    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(Map<String, String> attributes) {
        this.attributes = attributes;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * @param title the title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.attributes == null) ? 0 : this.attributes.hashCode());
        result = prime * result + ((this.title == null) ? 0 : this.title.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        StatusObject other = (StatusObject)obj;
        if (this.attributes == null) {
            if (other.attributes != null) {
                return false;
            }
        } else
            if (!this.attributes.equals(other.attributes)) {
                return false;
            }
        if (this.title == null) {
            if (other.title != null) {
                return false;
            }
        } else
            if (!this.title.equals(other.title)) {
                return false;
            }
        return true;
    }

    @SuppressWarnings( "nls" )
    @Override
    public String toString() {
        return "KomodoStatusObject [title=" + this.title + ", attributes=" + this.attributes + "]";
    }
}
