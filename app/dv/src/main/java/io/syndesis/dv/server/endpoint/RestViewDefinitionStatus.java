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

import org.teiid.core.util.ArgCheck;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import io.syndesis.dv.model.ViewDefinition;

@JsonSerialize(as = RestViewDefinitionStatus.class)
@JsonInclude(Include.NON_NULL)
public class RestViewDefinitionStatus {

    private String status;
    private String message;
    @JsonUnwrapped
    private ViewDefinition viewDefinition;

    /**
     * Default constructor for deserialization
     */
    public RestViewDefinitionStatus() {
        // do nothing
    }

    /**
     * @param status the subject of this status object
     *
     */
    public RestViewDefinitionStatus(String status) {
        ArgCheck.isNotNull(status);
        this.status = status;
    }

    public ViewDefinition getViewDefinition() {
        return viewDefinition;
    }

    public void setViewDefinition(ViewDefinition viewDefinition) {
        this.viewDefinition = viewDefinition;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * @param status the status
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return the message
     */
    public String getMessage() {
        return this.message;
    }

    /**
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.status == null) ? 0 : this.status.hashCode());
        result = prime * result + ((this.message == null) ? 0 : this.message.hashCode());
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
        RestViewDefinitionStatus other = (RestViewDefinitionStatus)obj;
        if (this.status == null) {
            if (other.status != null) {
                return false;
            }
        } else
            if (!this.status.equals(other.status)) {
                return false;
            }
        if (this.message == null) {
            if (other.message != null) {
                return false;
            }
        } else
            if (!this.message.equals(other.message)) {
                return false;
            }
        return true;
    }

    @SuppressWarnings( "nls" )
    @Override
    public String toString() {
        return "RestViewDefinitionStatus [status=" + this.status + ", message=" + this.message + "]";
    }
}
