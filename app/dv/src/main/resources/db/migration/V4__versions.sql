alter table data_virtualization add column created_at timestamp default CURRENT_TIMESTAMP;
alter table data_virtualization add column modified_at timestamp default CURRENT_TIMESTAMP;
alter table data_virtualization add column version bigint default 0;

alter table data_virtualization add column upper_name varchar(255);
update data_virtualization set upper_name = upper(name);
create unique index data_virtualization_name_index on data_virtualization (upper_name);

alter table source_schema add column created_at timestamp default CURRENT_TIMESTAMP;
alter table source_schema add column modified_at timestamp default CURRENT_TIMESTAMP;
alter table source_schema add column version bigint default 0;

alter table source_schema add column upper_name varchar(255);
update source_schema set upper_name = upper(name);
create unique index source_schema_name_index on source_schema (upper_name);

alter table view_definition add column created_at timestamp default CURRENT_TIMESTAMP; 
alter table view_definition add column modified_at timestamp default CURRENT_TIMESTAMP;
alter table view_definition add column version bigint default 0;

alter table view_definition add column upper_name varchar(255);
update view_definition set upper_name = upper(name);
create unique index view_definition_name_index on view_definition (upper_name, dv_name);

alter table view_definition add column parsable boolean;
update view_definition set parsable = complete;
