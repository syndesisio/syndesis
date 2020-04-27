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

package io.syndesis.dv.server;

import java.io.InputStream;
import java.util.Properties;

import io.syndesis.dv.StringConstants;

/**
 * Constants associated with version 1 of the Komodo REST application.
 */
public interface V1Constants extends StringConstants {

    class App {
        private App() {}

        private static final Properties properties = new Properties();

        private static void init() {
            InputStream fileStream = V1Constants.class.getClassLoader().getResourceAsStream("app.properties"); //$NON-NLS-1$

            try {
                properties.load(fileStream);
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        /**
         * Application name and context
         */
        public static String name() {
            init();

            return properties.getProperty("app.name"); //$NON-NLS-1$
        }

        /**
         * Application display title
         */
        public static String title() {
            init();

            return properties.getProperty("app.title"); //$NON-NLS-1$
        }

        /**
         * Application description
         */
        public static String description() {
            init();

            return properties.getProperty("app.description"); //$NON-NLS-1$
        }

        /**
         * Version of the application
         */
        public static String version() {
            init();

            return properties.getProperty("app.version"); //$NON-NLS-1$
        }
    }

    /**
     * The URI path segment for the Komodo REST application. It is included in the base URI. <strong>DO NOT INCLUDE THIS IN
     * OTHER URI SEGMENTS</strong>
     */
    String APP_PATH = FS + "v1"; //$NON-NLS-1$

    /**
     * The name of the URI path segment for the utility service.
     */
    String EDITORS_SEGMENT = "editors"; //$NON-NLS-1$

    /**
     * The name of the URI path segment for the metadata service.
     */
    String METADATA_SEGMENT = "metadata"; //$NON-NLS-1$

    /**
     * The about segment
     */
    String ABOUT = "about"; //$NON-NLS-1$

    String VIRTUALIZATIONS_SEGMENT = "virtualizations"; //$NON-NLS-1$

    String TEIID_SOURCE = "teiidSourceName"; //$NON-NLS-1$

    /**
     * Placeholder added to an URI to allow a specific teiid source name
     */
    String TEIID_SOURCE_PLACEHOLDER = "{" + TEIID_SOURCE + "}"; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * The name of the URI path segment for schema refresh
     */
    String REFRESH_SCHEMA_SEGMENT = "refreshSchema"; //$NON-NLS-1$

    /**
     * The name of the URI path segment for the collection of views of a vdb model
     */
    String VIEWS_SEGMENT = "views"; //$NON-NLS-1$

    String VIEW_NAME = "viewName"; //$NON-NLS-1$

    String VIEW_PLACEHOLDER = "{"+ VIEW_NAME +"}"; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * The name of the URI vdb name parameter
     */
    String VDB_NAME_PARAMETER = "name"; //$NON-NLS-1$

    /**
     * The teiid segment for running a query against the teiid server
     */
    String QUERY_SEGMENT = "query"; //$NON-NLS-1$

    /**
     * syndesis source summaries segment
     */
    String SOURCE_STATUSES = "sourceStatuses"; //$NON-NLS-1$

    /**
     * The view editor state of the user profile
     */
    String VIEW_LISTINGS = "viewListings"; //$NON-NLS-1$

    String ID = "id"; //$NON-NLS-1$

    /**
     * id placeholder
     */
    String ID_PLACEHOLDER = "{" + ID + "}"; //$NON-NLS-1$ //$NON-NLS-2$

    /**
     * Publish VDB
     */
    String PUBLISH = "publish"; //$NON-NLS-1$

    String IMPORT = "import"; //$NON-NLS-1$

    /**
     * Get source schema, table, column information for view definition
     */
    String RUNTIME_METADATA = "runtimeMetadata"; //$NON-NLS-1$

    String VIRTUALIZATION = "virtualization"; //$NON-NLS-1$

    String REVISION = "revision"; //$NON-NLS-1$

    /**
     * Placeholder added to an URI to allow a specific virtualization name
     */
    String VIRTUALIZATION_PLACEHOLDER = "{" + VIRTUALIZATION + "}"; //$NON-NLS-1$ //$NON-NLS-2$

    String REVISION_PLACEHOLDER = "{" + REVISION + "}"; //$NON-NLS-1$ //$NON-NLS-2$

    String START = "start"; //$NON-NLS-1$

    String REVERT = "revert"; //$NON-NLS-1$

    String TEST_SUPPORT = "test-support"; //$NON-NLS-1$

}
