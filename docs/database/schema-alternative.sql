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
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    icon VARCHAR(2048)
);
CREATE UNIQUE INDEX connector_name_uindex ON connector (name);

-- A connector property describes a single configuration option of connector
-- which are shared by all actions belonging to the connector (meta data)
CREATE TABLE connector_property
(
    id SERIAL PRIMARY KEY NOT NULL,
    connector_id INTEGER NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    secret BOOLEAN DEFAULT false NOT NULL,
    type VARCHAR(32) DEFAULT 'string' NOT NULL,
    default_value TEXT,
    CONSTRAINT connector_property_connector_id_fk FOREIGN KEY (connector_id) REFERENCES connector (id)
);

-- A action specifies a specific action on a connector ("twitter_mention" belonging to connector
-- "twitter")
CREATE TABLE action
(
    id SERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(256) NOT NULL,
    connector_id INTEGER NOT NULL,
    description VARCHAR(2048),
    CONSTRAINT action_connector_id_fk FOREIGN KEY (connector_id) REFERENCES connector (id)
);
CREATE UNIQUE INDEX action_name_connector_id_uindex ON action (name, connector_id);

-- A action property describes a single configuration option for an action
-- It is not shared and individual for an action (meta data)
CREATE TABLE action_property
(
    id SERIAL PRIMARY KEY NOT NULL,
    action_id INTEGER NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    secret BOOLEAN DEFAULT false NOT NULL,
    type VARCHAR(32) DEFAULT 'string' NOT NULL,
    default_value TEXT,
    CONSTRAINT action_property_action_id_fk FOREIGN KEY (action_id) REFERENCES action (id)
);

-- ==== Instances ====================================================================

-- The actual configuration values for connector. The keys correspond to the names of
-- configuration_property for the same connector. (value)
CREATE TABLE configured_connector
(
    id SERIAL PRIMARY KEY NOT NULL,
    connector_id INTEGER,
    -- connector specific properties:
    properties HSTORE NOT NULL,
    -- Not sure whether these are needed:
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    CONSTRAINT connector_properties_connector_id_fk FOREIGN KEY (connector_id) REFERENCES connector (id)
);
CREATE UNIQUE INDEX connector_properties_name_connector_id_uindex ON configured_connector (name, connector_id);

-- An action instance is the single entry point in this sub domain. It is referenced
-- by an integration and has all configuration needed for creating an instance of
-- a fully configured Camel connector
CREATE TABLE action_instance
(
    id SERIAL PRIMARY KEY NOT NULL,
    action_id INTEGER,
    configured_connector_id INTEGER,
    -- action specific properties here:
    properties HSTORE NOT NULL,
    CONSTRAINT action_instance_action_id_fk FOREIGN KEY (action_id) REFERENCES action (id),
    CONSTRAINT action_instance_configured_connector_id_fk FOREIGN KEY (configured_connector_id) REFERENCES configured_connector (id)
);

