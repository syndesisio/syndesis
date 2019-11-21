--no longer used, was not multi-pod safe
alter table data_virtualization drop column dirty;

alter table data_virtualization add column ddl VARCHAR(1000000);
alter table data_virtualization add column source_id VARCHAR(64);
alter table data_virtualization add column type char(1) default 'v';

create unique index data_virtualization_source_id on data_virtualization (source_id);