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

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Used to build a JSON representation for a schema node
 */
@JsonSerialize(as = RestSchemaNode.class)
@JsonInclude(Include.NON_NULL)
public class RestSchemaNode {

    private LinkedHashSet<RestSchemaNode> children = new LinkedHashSet<RestSchemaNode>();

    private String name;

    private String teiidName;

    private String connectionName;

    private String type;

    private boolean queryable = false;

    /**
     * Constructor for use when deserializing
     */
    public RestSchemaNode() {
        super();
    }

    /**
     * Constructor
     * @param connName source name
     * @param name node name
     * @param type type
     */
    public RestSchemaNode(String connName, String name, String type) {
        this.connectionName = connName;
        this.name = name;
        this.type = type;
    }

    /**
     * Get connection name
     * @return the connection name
     */
    public String getConnectionName() {
        return connectionName;
    }

    /**
     * Set connection name
     * @param connName connection name
     */
    public void setConnectionName(String connName) {
        this.connectionName = connName;
    }

    /**
     * Get node name
     * @return the node name
     */
    public String getName() {
        return name;
    }

    /**
     * Set node name
     * @param name the node name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get node type
     * @return the node type
     */
    public String getType() {
        return type;
    }

    /**
     * Set node type
     * @param type the node type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Get isQueryable state
     * @return 'true' if is queryable
     */
    public boolean isQueryable() {
        return queryable;
    }

    /**
     * Set queryable state
     * @param queryable 'true' if queryable
     */
    public void setQueryable(boolean queryable) {
        this.queryable = queryable;
    }

    /**
     * Get node children
     * @return the node children
     */
    public Set<RestSchemaNode> getChildren() {
        return children;
    }

    /**
     * Add node child
     * @param child the child node
     */
    public void addChild(RestSchemaNode child) {
        this.children.add(child);
    }

    public String getTeiidName() {
        return teiidName;
    }

    public void setTeiidName(String teiidName) {
        this.teiidName = teiidName;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, connectionName, type);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof RestSchemaNode) {
            RestSchemaNode other = (RestSchemaNode) obj;
            return Objects.equals(connectionName, other.connectionName)
                && Objects.equals(name, other.name)
                && Objects.equals(type, other.type);
        }
        return false;
    }

}
