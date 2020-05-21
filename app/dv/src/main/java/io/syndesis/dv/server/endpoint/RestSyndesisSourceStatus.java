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
package io.syndesis.dv.server.endpoint;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import io.syndesis.dv.StringConstants;
import io.syndesis.dv.server.V1Constants;

/**
 * The Syndesis source status.  Determine the following for syndesis source
 * - does it have corresponding teiid source
 * - the corresponding teiid source vdb state, and details
 * - the corresponding schema vdb state, and details
 */
@JsonSerialize(as = RestSyndesisSourceStatus.class)
@JsonInclude(Include.NON_NULL)
public final class RestSyndesisSourceStatus implements V1Constants {

    /**
     * Enumeration for state
     */
    public enum EntityState {
        /**
         * Active state
         */
        ACTIVE,
        /**
         * Failed state
         */
        FAILED,
        /**
         * Missing state
         */
        MISSING;
    }

    private String sourceName;
    private String teiidName;
    private List< String > errors;
    private EntityState schemaState = EntityState.MISSING;
    private String id;
    private boolean loading;
    private Date lastLoad;

    /**
     * Constructor for use in deserialization.
     */
    public RestSyndesisSourceStatus() {
        // nothing to do
    }

    /**
     * Constructor with name only.  Server VDB state is set to {@link EntityState#MISSING}.  Schema state is set to {@link EntityState#MISSING}.
     * @param sourceName the syndesis source name (cannot be <code>null</code>)
     */
    public RestSyndesisSourceStatus( final String sourceName ) {
        this.sourceName = sourceName;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals( final Object obj ) {
        if ( !( obj instanceof RestSyndesisSourceStatus ) ) {
            return false;
        }

        final RestSyndesisSourceStatus that = ( RestSyndesisSourceStatus )obj;

        return Objects.equals( this.sourceName, that.sourceName )
               && Objects.equals( this.teiidName, that.teiidName )
               && Objects.equals( this.errors, that.errors )
               && Objects.equals( this.id, that.id )
               && Objects.equals(  this.schemaState, that.schemaState )
               && this.loading == that.loading;
    }

    /**
     * @return the source name (can be empty)
     */
    public String getSourceName() {
        return this.sourceName;
    }

    /**
     * @return the teiid name (can be empty)
     */
    public String getTeiidName() {
        return this.teiidName;
    }

    /**
     * @return the errors (never <code>null</code>)
     */
    public List<String> getErrors() {
        return this.errors == null ? Arrays.asList( StringConstants.EMPTY_ARRAY ) : this.errors;
    }

    /**
     * @return the schema state or {@link EntityState#MISSING} if not set
     */
    public EntityState getSchemaState() {
        return this.schemaState == null ? EntityState.MISSING : this.schemaState;
    }

    /**
     * @return the schema model name (can be empty)
     */
    public String getId() {
        return this.id;
    }

    /**
     * {@inheritDoc}
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return Objects.hash( this.sourceName,
                             this.teiidName,
                             this.errors,
                             this.id,
                             this.schemaState,
                             this.loading );
    }

    /**
     * @param sourceName the source name (can be empty)
     */
    public void setSourceName( final String sourceName ) {
        this.sourceName = sourceName;
    }

    /**
     * @param teiidName the teiid name (can be empty)
     */
    public void setTeiidName( final String teiidName ) {
        this.teiidName = teiidName;
    }

    /**
     * @param errors the server VDB validity errors (can be <code>null</code>)
     */
    public void setErrors( final List< String > errors ) {
        this.errors = errors;
    }

    /**
     * @param schemaState the schema state (can be <code>null</code>)
     */
    public void setSchemaState( final EntityState schemaState ) {
        this.schemaState = schemaState == null ? EntityState.MISSING : schemaState;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setLoading(boolean loading) {
        this.loading = loading;
    }

    public boolean isLoading() {
        return loading;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public void setLastLoad(Date lastLoad) {
        this.lastLoad = lastLoad;
    }

    @SuppressFBWarnings("EI_EXPOSE_REP")
    public Date getLastLoad() {
        return lastLoad;
    }

}
