CREATE TABLE dv_table_privileges
  (
     view_definition_id VARCHAR(64) NOT NULL,
     role_name          VARCHAR(255),
     grant_privileges   VARCHAR(255),
     PRIMARY KEY (view_definition_id, role_name),
     FOREIGN KEY (view_definition_id) REFERENCES view_definition(id) ON DELETE CASCADE
  );
