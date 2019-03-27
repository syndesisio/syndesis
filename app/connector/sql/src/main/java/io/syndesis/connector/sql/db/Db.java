/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.sql.db;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public interface Db {

    String getDefaultSchema(String dbUser);
    String adaptPattern(String pattern);
    ResultSet fetchProcedureColumns(DatabaseMetaData meta, String catalog,
        String schema, String procedureName) throws SQLException;
    ResultSet fetchProcedures(DatabaseMetaData meta, String catalog,
        String schemaPattern, String procedurePattern) throws SQLException;
    String getAutoIncrementGrammar();
    String getName();
}
