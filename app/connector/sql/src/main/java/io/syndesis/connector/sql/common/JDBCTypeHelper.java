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

import java.sql.JDBCType;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Types;

public final class JDBCTypeHelper {

    private JDBCTypeHelper() {
        // utility class
    }

    public static JDBCType determineJDBCType(final ResultSet columnResultSet) throws SQLException {
        final int columnType = columnResultSet.getInt("DATA_TYPE");
        final String columnTypeName = columnResultSet.getString("TYPE_NAME");

        return determineJDBCType(columnType, columnTypeName);
    }

    public static JDBCType determineJDBCType(final ResultSetMetaData metaData, final int column) throws SQLException {
        final int columnType = metaData.getColumnType(column);
        final String columnTypeName = metaData.getColumnTypeName(column);

        return determineJDBCType(columnType, columnTypeName);
    }

    private static JDBCType determineJDBCType(final int columnType, final String columnTypeName) {
        if (columnType == Types.OTHER) {
            return determineOtherJDBCType(columnTypeName);
        }

        return JDBCType.valueOf(columnType);
    }

    private static JDBCType determineOtherJDBCType(final String typeName) {
        if ("uuid".equalsIgnoreCase(typeName)) {
            // treat UUID column type as VARCHAR
            return JDBCType.VARCHAR;
        }

        return JDBCType.OTHER;
    }

}
