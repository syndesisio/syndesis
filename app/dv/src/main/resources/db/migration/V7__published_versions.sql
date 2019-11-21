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