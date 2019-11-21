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
package io.syndesis.dv.server;

import static io.syndesis.dv.StringConstants.DOT;

import java.util.ResourceBundle;

/**
 * Localized messages for the {@code server rest} project.
 */
public final class Messages {

    /**
     * Messages relating to errors.
     */
    public enum Error {

        RESOURCE_NOT_FOUND,

        /**
         * An error indicating a failure in authentication or authorization of the REST service
         */
        SECURITY_FAILURE_ERROR,

        /**
         * A general error
         */
        INTERNAL_ERROR,

        /**
         * An error indicating an error occurred trying to obtain the specified Dataservice.
         */
        DATASERVICE_SERVICE_GET_DATASERVICE_ERROR,

        /**
         * An error indicating an error occurred trying to obtain a dataservice's connections
         */
        DATASERVICE_SERVICE_GET_CONNECTIONS_ERROR,

        /**
         * An error indicating an error due to missing tablePath
         */
        DATASERVICE_SERVICE_GET_JOIN_MISSING_TABLEPATH,

        /**
         * An error indicating an error occurred trying to obtain a dataservice's drivers
         */
        DATASERVICE_SERVICE_GET_DRIVERS_ERROR,

        /**
         * An error indicating a request to create a dataservice failed
         */
        DATASERVICE_SERVICE_CREATE_DATASERVICE_ERROR,

        /**
         * An error indicating a request to delete a dataservice failed
         */
        DATASERVICE_SERVICE_DELETE_DATASERVICE_ERROR,

        /**
         * An error indicating a request to update a dataservice failed
         */
        DATASERVICE_SERVICE_UPDATE_DATASERVICE_ERROR,

        /**
         * An error indicating a request to refresh dataservice views failed
         */
        DATASERVICE_SERVICE_REFRESH_VIEWS_ERROR,

        /**
         * An error indicating an error occurred trying to find a service view info
         */
        DATASERVICE_SERVICE_FIND_VIEW_INFO_ERROR,

        /**
         * An error indicating an error occurred trying to find a matching source VDB
         */
        DATASERVICE_SERVICE_FIND_SOURCE_VDB_ERROR,

        /**
         * An error indicating update attempt was missing a name
         */
        DATASERVICE_SERVICE_MISSING_NAME,

        /**
         * An error indicating that the service does not exist
         */
        DATASERVICE_SERVICE_SERVICE_DNE,

        /**
         * An error indicating the parameter and JSON dataservice name does not match for a dataservice being created.
         */
        DATASERVICE_SERVICE_SERVICE_NAME_ERROR,

        /**
         * An error indicating create attempt failed because same name already exists
         */
        DATASERVICE_SERVICE_CREATE_ALREADY_EXISTS,

        /**
         * An error indicating that a table does not exist
         */
        DATASERVICE_SERVICE_SOURCE_TABLE_DNE,

        /**
         * An error indicating that a model source does not exist
         */
        DATASERVICE_SERVICE_MODEL_SOURCE_DNE,

        /**
         * The dataservice service lacks at least one parameter
         */
        DATASERVICE_SERVICE_MISSING_PARAMETER_ERROR,

        /**
         * A message indicating an unexpected error occurred updating data service
         */
        DATASERVICE_SERVICE_UPDATE_ERROR,

        /**
         * A message indicating that a data service with the given name already exists.
         */
        DATASERVICE_SERVICE_NAME_EXISTS,

        /**
         * A message indicating an unexpected error occurred during name validation.
         */
        DATASERVICE_SERVICE_NAME_VALIDATION_ERROR,

        /**
         * An error indicating the expected connection name was missing
         */
        CONNECTION_SERVICE_MISSING_CONNECTION_NAME,

        /**
         * A message indicating that a View with the given name already exists.
         */
        VIEW_NAME_EXISTS,

        /**
         * A message indicating an unexpected error occurred during name validation.
         */
        VIEW_NAME_VALIDATION_ERROR,

        /**
         * An error indicating a teiid status error
         */
        METADATA_SERVICE_STATUS_ERROR,

        /**
         * The teiid service cannot parse the request body
         */
        METADATA_SERVICE_REQUEST_PARSING_ERROR,

        /**
         * An error determining data service deployable status
         */
        METADATA_SERVICE_GET_DATA_SERVICE_DEPLOYABLE_ERROR,

        /**
         * An error when getting vdbs
         */
        METADATA_SERVICE_GET_VDBS_ERROR,

        /**
         * An error when getting datasources
         */
        METADATA_SERVICE_GET_DATA_SOURCES_ERROR,

        /**
         * An error when getting datasource
         */
        METADATA_SERVICE_GET_DATA_SOURCE_ERROR,

        /**
         * An error when getting a Datasource translator.
         */
        METADATA_SERVICE_GET_DATA_SOURCE_TRANSLATOR_ERROR,

        /**
         * An error indicating a teiid vdb status error
         */
        METADATA_SERVICE_VDBS_STATUS_ERROR,

        /**
         * An error when getting drivers
         */
        METADATA_SERVICE_GET_DRIVERS_ERROR,

        /**
         * An error indicating a timeout occurred whilst conducting an import
         */
        METADATA_SERVICE_IMPORT_TIMEOUT,

        /**
         * An error indicating an error occurred whilst fetching the teiid translators
         */
        METADATA_SERVICE_GET_TRANSLATORS_ERROR,

        /**
         * An error occurred locating the default translator mappings
         */
        METADATA_SERVICE_DEFAULT_TRANSLATOR_MAPPINGS_NOT_FOUND_ERROR,

        /**
         * An error occurred attempting to load the default translator mappings
         */
        METADATA_SERVICE_LOAD_DEFAULT_TRANSLATOR_MAPPINGS_ERROR,

        /**
         * An error indicating a teiid file attributes object has no parameters
         */
        METADATA_SERVICE_FILE_ATTRIB_NO_PARAMETERS,

        /**
         * An error indicating a teiid file attributes object has no name
         */
        METADATA_SERVICE_FILE_ATTRIB_NO_NAME,

        /**
         * An error indicating a teiid file attributes object has no file
         */
        METADATA_SERVICE_FILE_ATTRIB_NO_CONTENT,

        /**
         * An error indicating a teiid dataService deployment failure
         */
        METADATA_SERVICE_DEPLOY_DATA_SERVICE_ERROR,

        /**
         * An error indicating a teiid connection deployment failure
         */
        METADATA_SERVICE_DEPLOY_CONNECTION_ERROR,

        /**
         * An error indicating a workspace driver being deployed to teiid cannot be found due to
         * a missing path property
         */
        METADATA_SERVICE_DRIVER_MISSING_PATH,

        /**
         * A driver cannot be found at the given path in the workspace
         */
        METADATA_SERVICE_NO_DRIVER_FOUND_IN_WKSP,

        /**
         * Cannot deploy a driver since one of its attributes is missing
         */
        METADATA_SERVICE_DRIVER_ATTRIBUTES_MISSING,

        /**
         * An error indicating a teiid DataSource undeploy failure
         */
        METADATA_SERVICE_UNDEPLOY_DATA_SOURCE_ERROR,

        /**
         * An error indicating a teiid Vdb deployment failure
         */
        METADATA_SERVICE_DEPLOY_VDB_ERROR,

        /**
         * An error indicating a refresh preview Vdb failure
         */
        METADATA_SERVICE_REFRESH_PREVIEW_VDB_ERROR,

        /**
         * An error indicating the vdb name is missing
         */
        METADATA_SERVICE_MISSING_VDB_NAME,

        /**
         * An error indicating a teiid Vdb undeploy failure
         */
        METADATA_SERVICE_UNDEPLOY_VDB_ERROR,

        /**
         * An error indicating a teiid driver deployment failure
         */
        METADATA_SERVICE_DEPLOY_DRIVER_ERROR,

        /**
         * An error indicating a teiid driver undeployment failure
         */
        METADATA_SERVICE_UNDEPLOY_DRIVER_ERROR,

        /**
         * An error indicating a name is missing while deploying a data service
         */
        METADATA_SERVICE_DATA_SERVICE_MISSING_PATH,

        /**
         * No data service could be found while trying to deploy
         */
        METADATA_SERVICE_NO_DATA_SERVICE_FOUND,

        /**
         * An error indicating a name is missing while deploying a data source
         */
        METADATA_SERVICE_CONNECTION_MISSING_PATH,

        /**
         * No data source could be found while trying to deploy
         */
        METADATA_SERVICE_NO_CONNECTION_FOUND,

        /**
         * No VDB could be found while trying to deploy
         */
        METADATA_SERVICE_NO_VDB_FOUND,

        /**
         * An error indicating a name is missing while deploying a VDB
         */
        METADATA_SERVICE_VDB_MISSING_PATH,

        /**
         * No query specified for the query operation
         */
        METADATA_SERVICE_QUERY_MISSING_QUERY,

        /**
         * No target specified for the query operation
         */
        METADATA_SERVICE_QUERY_MISSING_TARGET,

        /**
         * The query target does not appear to have been deployed
         */
        METADATA_SERVICE_QUERY_TARGET_NOT_DEPLOYED,

        /**
         * An error indicating a query failure
         */
        METADATA_SERVICE_QUERY_ERROR,

        /**
         * Error indicating a ping type is missing
         */
        METADATA_SERVICE_PING_MISSING_TYPE,

        /**
         * The Teiid service update is missing a parameter
         */
        METADATA_SERVICE_UPDATE_MISSING_PARAMETER_ERROR,

        /**
         * The service lacks at least one parameter
         */
        METADATA_SERVICE_UPDATE_REQUEST_PARSING_ERROR,

        /**
         * An error indicating update attempt failed because the VDB name was missing
         */
        METADATA_SERVICE_UPDATE_MISSING_VDBNAME,

        /**
         * An error indicating update attempt failed because the Model name was missing
         */
        METADATA_SERVICE_UPDATE_MISSING_MODELNAME,

        /**
         * An error indicating update attempt failed because the Metadata VDB name was missing
         */
        METADATA_SERVICE_UPDATE_MISSING_METADATA_VDBNAME,

        /**
         * An error indicating update attempt failed because the Metadata Model name was missing
         */
        METADATA_SERVICE_UPDATE_MISSING_METADATA_MODELNAME,

        /**
         * An error indicating update attempt failed because retrieval of the teiid DDL failed.
         */
        METADATA_SERVICE_UPDATE_DDL_FETCH_ERROR,

        /**
         * An error indicating update attempt failed because the Teiid Model DDL was empty
         */
        METADATA_SERVICE_UPDATE_DDL_DNE,

        /**
         * An error indicating data source isn not a JDBC source.
         */
        METADATA_SERVICE_GET_DATA_SOURCE_NOT_JDBC_ERROR,

        /**
         * An error indicating data source cannot be instantiated from available data sources
         */
        METADATA_SERVICE_GET_DATA_SOURCE_INSTANTIATION_FAILURE,

        /**
         * An error indicating attempt to get source JDBC connection failed.
         */
        METADATA_SERVICE_GET_DATA_SOURCE_CONNECTION_ERROR,

        /**
         * An error indicating attempt to fetch source JDBC tables failed.
         */
        METADATA_SERVICE_GET_DATA_SOURCE_TABLE_FETCH_ERROR,

        /**
         * An error indicating attempt to get source tables failed.
         */
        METADATA_SERVICE_GET_DATA_SOURCE_TABLES_ERROR,

        /**
         * An error indicating attempt to get source catalog and schema failed.
         */
        METADATA_SERVICE_GET_DATA_SOURCE_CATALOG_SCHEMA_ERROR,

        /**
         * An error indicating jdbc info failed to be supplied from a data source.
         */
        METADATA_SERVICE_GET_DATA_SOURCE_JDBC_INFO_FAILURE,

        /**
         * An error indicating the jdbc data source is not recognised.
         */
        METADATA_SERVICE_GET_DATA_SOURCE_UNRECOGNISED_JDBC_SOURCE,

        /**
         * An error indicating a connection could not be undeployed
         */
        METADATA_SERVICE_UNDEPLOY_CONNECTION_ERROR,

        /**
         * An error indicating update attempt failed
         */
        METADATA_SERVICE_UPDATE_ERROR,

        /**
         * An error indicating the instance failed to get a data source template.
         */
        METADATA_SERVICE_GET_TEMPLATE_ERROR,

        /**
         * An error indicating the instance failed to get any data source templates.
         */
        METADATA_SERVICE_GET_TEMPLATES_ERROR,

        /**
         * An error indicating the instance failed to get any data source templates.
         */
        METADATA_SERVICE_GET_TEMPLATE_ENTRIES_ERROR,

        /**
         * An error indicating the failed status of to get syndesis sources
         */
        METADATA_GET_SYNDESIS_SOURCES_ERROR,

        /**
         * An error indicating a name of syndesis source missing from bind operation
         */
        METADATA_SYNDESIS_SOURCE_BIND_MISSING_NAME,

        /**
         * An error indicating payload parse error from bind operation
         */
        METADATA_SYNDESIS_SOURCE_BIND_PARSE_ERROR,

        /**
         * An error indicating from bind operation on service catalog service
         */
        METADATA_SYNDESIS_SOURCE_BIND_ERROR,

        /**
         * An error indicating the about service failed
         */
        ABOUT_SERVICE_ERROR,

        /**
         * An error indicating the user profile service method failed
         */
        USER_PROFILE_SERVICE_ERROR,

        /**
         * An error indicating no user profile can be found
         */
        NO_USER_PROFILE,

        /**
         * A failure to encrypt and secure sensitive data
         */
        ENCRYPT_FAILURE,

        /**
         * A failure to decrypt sensitive data
         */
        DECRYPT_FAILURE,

        /**
         * An error indicating from publish operation on service catalog service
         */
        PUBLISH_ERROR,

        /**
         * An error indicating a view definition is missing the name
         */
        VIEW_DEFINITION_MISSING_NAME,

        /**
         * An error indicating a view definition is missing the DDL
         */
        VIEW_DEFINITION_MISSING_DDL,

        VIEW_DEFINITION_MISSING_DATAVIRTUALIZATIONNAME,

        /**
         * An error indicating a problem with validating the viewDefinition DDL
         */
        VALIDATE_VIEW_DEFINITION_ERROR,

        /**
         * An error indicating a problem with name match validating the viewDefinition DDL
         */
        VALIDATE_VIEW_DEFINITION_NAME_MATCH_ERROR,

        /**
         * An error indicating a problem with getting view source table info
         */
        GET_VIEW_SOURCE_TABLE_INFO_ERROR,

        /**
         * VDB Not found
         */
        VDB_NOT_FOUND,

        /**
         * VDB name not provided
         */
        VDB_NAME_NOT_PROVIDED;
    }

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + DOT
                                              + Messages.class.getSimpleName().toLowerCase();

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

    /**
     * @param key
     *        the message key (cannot be <code>null</code>)
     * @param parameters
     *        the substitution parameters (can be <code>null</code>)
     * @return the localized message (never empty)
     */
    public static String getString( final Enum< ? > key,
                                    final Object... parameters ) {
        return io.syndesis.dv.utils.Messages.getString(key, RESOURCE_BUNDLE, parameters);
    }

    /**
     * Don't allow construction outside of this class.
     */
    private Messages() {
        // nothing to do
    }

}
