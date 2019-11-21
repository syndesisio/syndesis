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

--no longer used, was not multi-pod safe
alter table data_virtualization drop column dirty;

alter table data_virtualization add column ddl VARCHAR(1000000);
alter table data_virtualization add column source_id VARCHAR(64);
alter table data_virtualization add column type char(1) default 'v';

create unique index data_virtualization_source_id on data_virtualization (source_id);