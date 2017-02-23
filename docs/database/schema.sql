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

CREATE TABLE connector
(
    id SERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    icon VARCHAR(2048)
);
CREATE UNIQUE INDEX connector_name_uindex ON connector (name);

CREATE TABLE configuration_property
(
    id SERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(256) NOT NULL,
    description VARCHAR(2048),
    secret BOOLEAN DEFAULT false NOT NULL,
    type VARCHAR(32) DEFAULT 'string' NOT NULL,
    default_value TEXT
);

CREATE TABLE connector_configuration_property
(
    connector_id INTEGER NOT NULL,
    configuration_property_id INTEGER NOT NULL,
    CONSTRAINT connector_configuration_property_connector_id_fk FOREIGN KEY (connector_id) REFERENCES connector (id),
    CONSTRAINT connector_configuration_property_configuration_property_id_fk FOREIGN KEY (configuration_property_id) REFERENCES configuration_property (id)
);
CREATE UNIQUE INDEX connector_configuration_property_configuration_property_id_uind ON connector_configuration_property (configuration_property_id);

CREATE TABLE configured_connector
(
    id SERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(256) NOT NULL,
    connector_id INTEGER,
    description VARCHAR(2048),
    properties HSTORE NOT NULL,
    CONSTRAINT connector_properties_connector_id_fk FOREIGN KEY (connector_id) REFERENCES connector (id)
);
CREATE UNIQUE INDEX connector_properties_name_connector_id_uindex ON configured_connector (name, connector_id);

CREATE TABLE action
(
    id SERIAL PRIMARY KEY NOT NULL,
    name VARCHAR(256) NOT NULL,
    connector_id INTEGER NOT NULL,
    description VARCHAR(2048),
    CONSTRAINT action_connector_id_fk FOREIGN KEY (connector_id) REFERENCES connector (id)
);
CREATE UNIQUE INDEX action_name_connector_id_uindex ON action (name, connector_id);

CREATE TABLE action_configuration_property
(
    action_id INTEGER NOT NULL,
    configuration_property_id INTEGER NOT NULL,
    CONSTRAINT action_configuration_property_action_id_fk FOREIGN KEY (action_id) REFERENCES action (id),
    CONSTRAINT action_configuration_property_configuration_property_id_fk FOREIGN KEY (configuration_property_id) REFERENCES configuration_property (id)
);
CREATE UNIQUE INDEX action_configuration_property_configuration_property_id_uindex ON action_configuration_property (configuration_property_id);
