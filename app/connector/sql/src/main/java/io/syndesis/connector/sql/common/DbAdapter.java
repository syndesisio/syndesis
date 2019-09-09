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
package io.syndesis.connector.sql.common;

import java.sql.Connection;
import java.sql.SQLException;

import io.syndesis.connector.sql.db.Db;
import io.syndesis.connector.sql.db.DbDerby;
import io.syndesis.connector.sql.db.DbMysql;
import io.syndesis.connector.sql.db.DbOracle;
import io.syndesis.connector.sql.db.DbPostgresql;
import io.syndesis.connector.sql.db.DbStandard;
import io.syndesis.connector.sql.db.DbTeiid;

public class DbAdapter {

    private Db db;

    public DbAdapter(Connection conn) throws SQLException {
        this.db = getDb(conn);
    }

    public Db getDb() {
        return this.db;
    }

    private Db getDb(Connection conn) throws SQLException {
        DbEnum dbEnum =  DbEnum.fromName(conn.getMetaData().getDatabaseProductName());
        switch (dbEnum) {

        case APACHE_DERBY:
            return new DbDerby();
        case MYSQL:
            return new DbMysql();
        case ORACLE:
            return new DbOracle();
        case POSTGRESQL:
            return new DbPostgresql();
        case TEIID_SERVER:
        	return new DbTeiid();
        default:
            return new DbStandard();
        }
    }
}
