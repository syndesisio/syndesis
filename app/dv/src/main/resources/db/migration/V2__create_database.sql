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

-- start clean
drop table if exists source_schema;
drop index if exists view_definition_dv_name;
drop table if exists view_definition;
drop table if exists data_virtualization;

CREATE TABLE data_virtualization 
  ( 
     id          VARCHAR(64) NOT NULL, 
     description VARCHAR(4096), 
     name        VARCHAR(255) UNIQUE, 
     PRIMARY KEY (id) 
  ); 

CREATE TABLE source_schema 
  ( 
     id   VARCHAR(64) NOT NULL,
     name VARCHAR(255) UNIQUE, 
     ddl  VARCHAR(1000000), 
     PRIMARY KEY (id) 
  ); 

CREATE TABLE view_definition 
  ( 
     id           VARCHAR(64) NOT NULL, 
     complete     BOOLEAN NOT NULL, 
     ddl          VARCHAR(100000), 
     description  VARCHAR(4096), 
     name         VARCHAR(255), 
     state        VARCHAR(100000), 
     user_defined BOOLEAN NOT NULL, 
     dv_name      VARCHAR(255) NOT NULL, 
     PRIMARY KEY (id),
     UNIQUE (name, dv_name),
     FOREIGN KEY (dv_name) REFERENCES data_virtualization(name) ON DELETE CASCADE
  ); 

CREATE INDEX view_definition_dv_name ON view_definition(dv_name);
