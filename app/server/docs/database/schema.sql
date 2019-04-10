--
-- Copyright (C) 2016 Red Hat, Inc.
--
-- Licensed under the Apache License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
--         http://www.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--

CREATE EXTENSION hstore;

-- ===== Meta data ====================================================

-- A connector groups actions and of a specific type ("twitter")
CREATE TABLE connector
(
    id SERIAL PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    icon TEXT,
    tags TEXT[]
);
CREATE UNIQUE INDEX connector_name_uindex ON connector (name);
CREATE INDEX connector_tags_index ON connector USING GIN(tags);

-- A connector property describes a single configuration option of connector
-- which are shared by all actions belonging to the connector (meta data)
CREATE TABLE connector_property
(
    id SERIAL PRIMARY KEY NOT NULL,
    connector_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    secret BOOLEAN DEFAULT false NOT NULL,
    type TEXT DEFAULT 'string' NOT NULL,
    default_value TEXT,
    required BOOLEAN DEFAULT false NOT NULL,
    property_index SMALLINT NOT NULL,
    CONSTRAINT connector_property_connector_id_fk FOREIGN KEY (connector_id) REFERENCES connector (id)
);
CREATE UNIQUE INDEX connector_property_connector_id_property_index_uindex ON connector_property (connector_id, property_index);

-- A action specifies a specific action on a connector ("twitter_mention" belonging to connector
-- "twitter")
CREATE TABLE action
(
    id SERIAL PRIMARY KEY NOT NULL,
    connector_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    tags TEXT[],
    camel_connector_gav TEXT NOT NULL,
    CONSTRAINT action_connector_id_fk FOREIGN KEY (connector_id) REFERENCES connector (id)
);
CREATE UNIQUE INDEX action_name_connector_id_uindex ON action (name, connector_id);
CREATE INDEX action_tags_index ON action (tags);

-- A action property describes a single configuration option for an action
-- It is not shared and individual for an action (meta data)
CREATE TABLE action_property
(
    id SERIAL PRIMARY KEY NOT NULL,
    action_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    secret BOOLEAN DEFAULT false NOT NULL,
    type TEXT DEFAULT 'string' NOT NULL,
    default_value TEXT,
    property_index SMALLINT NOT NULL,
    CONSTRAINT action_property_action_id_fk FOREIGN KEY (action_id) REFERENCES action (id)
);
CREATE INDEX action_property_action_id_property_index_index ON action_property (action_id, property_index);

-- ==== Instances ====================================================================

-- The actual configuration values for connector. The keys correspond to the names of
-- configuration_property for the same connector. (value)
CREATE TABLE configured_connector
(
    id SERIAL PRIMARY KEY NOT NULL,
    connector_id INTEGER,
    name TEXT NOT NULL,
    description TEXT,
    -- connector specific properties:
    properties HSTORE NOT NULL,
    tags TEXT[],
    CONSTRAINT connector_properties_connector_id_fk FOREIGN KEY (connector_id) REFERENCES connector (id)
);
CREATE UNIQUE INDEX connector_properties_name_connector_id_uindex ON configured_connector (name, connector_id);
CREATE INDEX configured_connector_tags_index ON configured_connector USING GIN(tags);

-- An action instance is the single entry point in this sub domain. It is referenced
-- by an integration and has all configuration needed for creating an instance of
-- a fully configured Camel connector
CREATE TABLE action_instance
(
    id SERIAL PRIMARY KEY NOT NULL,
    action_id INTEGER NOT NULL,
    configured_connector_id INTEGER NOT NULL,
    -- action specific properties here:
    properties HSTORE NOT NULL,
    CONSTRAINT action_instance_action_id_fk FOREIGN KEY (action_id) REFERENCES action (id),
    CONSTRAINT action_instance_configured_connector_id_fk FOREIGN KEY (configured_connector_id) REFERENCES configured_connector (id)
);

-- An integration is the integration definition. It contains a list of integration steps.
CREATE TABLE integration
(
    id SERIAL PRIMARY KEY NOT NULL,
    name TEXT NOT NULL,
    description TEXT,
    tags TEXT[]
);
CREATE UNIQUE INDEX integration_name_uindex ON integration (name);
CREATE INDEX integration_tags_index ON integration USING GIN(tags);

-- An integration step is the definition of a single step in an integration. It contains configuration
-- for each and every step in the integration flow.
-- This can reference an action instance or be a supported integration pattern (e.g. log, wiretap, choice, etc).
CREATE TABLE integration_step
(
    id SERIAL PRIMARY KEY NOT NULL,
    integration_id INTEGER NOT NULL,
    action_instance_id INTEGER,
    step_index SMALLINT NOT NULL,
    step_type TEXT NOT NULL,
    -- integration step specific properties here:
    properties HSTORE NOT NULL,
    CONSTRAINT integration_step_integration_id_fk FOREIGN KEY (integration_id) REFERENCES integration (id),
    CONSTRAINT integration_step_action_instance_id_fk FOREIGN KEY (action_instance_id) REFERENCES action_instance (id)
);
CREATE UNIQUE INDEX integration_step_integration_id_step_index_uindex ON integration_step (integration_id, step_index);
