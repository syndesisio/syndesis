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

create table edition 
(
    id VARCHAR(64) NOT NULL, 
    dv_name varchar(255) not null, 
    revision bigint not null, 
    dv_export bytea,
    description varchar(4096), 
    created_at timestamp, 
    PRIMARY KEY (id),
    UNIQUE (dv_name, revision),
    FOREIGN KEY (dv_name) REFERENCES data_virtualization(name) ON DELETE CASCADE
);

alter table data_virtualization add column modified boolean not null default true;