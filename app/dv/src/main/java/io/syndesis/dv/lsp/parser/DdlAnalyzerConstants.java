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
package io.syndesis.dv.lsp.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.teiid.query.parser.SQLParserConstants;

public final class DdlAnalyzerConstants {

    public enum StatementType {
        UNKNOWN_STATEMENT_TYPE, CREATE_TABLE_TYPE, CREATE_FOREIGN_TABLE_TYPE, CREATE_FOREIGN_TEMPORARY_TABLE_TYPE,
        CREATE_GLOBAL_TEMPORARY_TABLE_TYPE, CREATE_VIEW_TYPE, CREATE_VIRTUAL_VIEW_TYPE
    }

    public enum Context {
        NONE_FOUND, PREFIX, TABLE_BODY, TABLE_OPTIONS, TABLE_ELEMENT, TABLE_ELEMENT_OPTIONS,
        TABLE_ELEMENT_OPTION_SEARCHABLE, QUERY_EXPRESSION, SELECT_CLAUSE, SELECT_CLAUSE_START, SELECT_COLUMN, FUNCTION,
        TABLE_ALIAS, COLUMN_NAME, FROM_CLAUSE, FROM_CLAUSE_START, FROM_CLAUSE_ALIAS, FROM_CLAUSE_AS,
        FROM_CLAUSE_AS_OR_WHERE, FROM_CLAUSE_ID, TABLE_SYMBOL, TABLE_SYMBOL_ID, TABLE_SYMBOL_AS, TABLE_NAME,
        WHERE_CLAUSE, WHERE_CLAUSE_START, WHERE_CLAUSE_TABLE_ALIAS
    }

    public enum MetadataType {
        SCHEMA, TABLE, COLUMN, SCHEMA_TABLE, SCHEMA_TABLE_COLUMN, TABLE_COLUMN
    }

    public static final Set<Integer> DATATYPES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            SQLParserConstants.STRING, SQLParserConstants.VARBINARY, SQLParserConstants.VARCHAR,
            SQLParserConstants.BOOLEAN, SQLParserConstants.BYTE, SQLParserConstants.TINYINT, SQLParserConstants.SHORT,
            SQLParserConstants.SMALLINT, SQLParserConstants.CHAR, SQLParserConstants.INTEGER, SQLParserConstants.LONG,
            SQLParserConstants.BIGINT, SQLParserConstants.BIGINTEGER, SQLParserConstants.FLOAT, SQLParserConstants.REAL,
            SQLParserConstants.DOUBLE, SQLParserConstants.BIGDECIMAL, SQLParserConstants.DECIMAL,
            SQLParserConstants.DATE, SQLParserConstants.TIME, SQLParserConstants.TIMESTAMP, SQLParserConstants.BLOB,
            SQLParserConstants.CLOB, SQLParserConstants.XML, SQLParserConstants.JSON, SQLParserConstants.GEOMETRY,
            SQLParserConstants.GEOGRAPHY, SQLParserConstants.OBJECT)));

    public static final List<String> DATATYPE_LIST = Collections.unmodifiableList(new ArrayList<>(Arrays.asList(
            getLabel(SQLParserConstants.STRING, false),
            getLabel(SQLParserConstants.VARBINARY, false), getLabel(SQLParserConstants.VARCHAR, false),
            getLabel(SQLParserConstants.BOOLEAN, false), getLabel(SQLParserConstants.BYTE, false),
            getLabel(SQLParserConstants.TINYINT, false), getLabel(SQLParserConstants.SHORT, false),
            getLabel(SQLParserConstants.SMALLINT, false), getLabel(SQLParserConstants.CHAR, false),
            getLabel(SQLParserConstants.INTEGER, false), getLabel(SQLParserConstants.LONG, false),
            getLabel(SQLParserConstants.BIGINT, false), getLabel(SQLParserConstants.BIGINTEGER, false),
            getLabel(SQLParserConstants.FLOAT, false), getLabel(SQLParserConstants.REAL, false),
            getLabel(SQLParserConstants.DOUBLE, false), getLabel(SQLParserConstants.BIGDECIMAL, false),
            getLabel(SQLParserConstants.DECIMAL, false), getLabel(SQLParserConstants.DATE, false),
            getLabel(SQLParserConstants.TIME, false), getLabel(SQLParserConstants.TIMESTAMP, false),
            getLabel(SQLParserConstants.BLOB, false), getLabel(SQLParserConstants.CLOB, false),
            getLabel(SQLParserConstants.XML, false), getLabel(SQLParserConstants.JSON, false),
            getLabel(SQLParserConstants.GEOMETRY, false), getLabel(SQLParserConstants.GEOGRAPHY, false),
            getLabel(SQLParserConstants.OBJECT, false))));

    /*
     * Array of tokens that match the start of a CREATE TABLE statement
     */
    static final int[] CREATE_TABLE_STATEMENT = { SQLParserConstants.CREATE, SQLParserConstants.TABLE };

    /*
     * Array of tokens that match the start of a CREATE FOREIGNT TABLE statement
     */
    static final int[] CREATE_FOREIGN_TABLE_STATEMENT = { SQLParserConstants.CREATE, SQLParserConstants.FOREIGN,
            SQLParserConstants.TABLE };

    /*
     * Array of tokens that match the start of a CREATE FOREIGN TEMPORARY TABLE
     * statement
     */
    static final int[] CREATE_FOREIGN_TEMPORARY_TABLE_STATEMENT = { SQLParserConstants.CREATE,
            SQLParserConstants.FOREIGN, SQLParserConstants.TEMPORARY, SQLParserConstants.TABLE };

    /*
     * Array of tokens that match the start of a CREATE TEMPORARY TABLE statement
     */
    static final int[] CREATE_GLOBAL_TEMPORARY_TABLE_STATEMENT = { SQLParserConstants.CREATE, SQLParserConstants.GLOBAL,
            SQLParserConstants.TEMPORARY, SQLParserConstants.TABLE };

    /*
     * Array of tokens that match the start of a CREATE VIEW statement
     */
    static final int[] CREATE_VIEW_STATEMENT = { SQLParserConstants.CREATE, SQLParserConstants.VIEW };

    /*
     * Array of tokens that match the start of a CREATE VIRTUAL VIEW statement
     */
    static final int[] CREATE_VIRTUAL_VIEW_STATEMENT = { SQLParserConstants.CREATE, SQLParserConstants.VIRTUAL,
            SQLParserConstants.VIEW };

    private DdlAnalyzerConstants() {
        // utility class
    }

    /**
     * The getLabel(...) call is returning strings wrapped in double-quotes
     *
     * Need to return a simple string
     *
     * @return string without double quotes
     */
    public static String getLabel(int keywordId, boolean upperCase) {
        String tokenImageStr = SQLParserConstants.tokenImage[keywordId];
        if (upperCase) {
            return tokenImageStr.substring(1, tokenImageStr.length() - 1).toUpperCase(Locale.US);
        }
        return tokenImageStr.substring(1, tokenImageStr.length() - 1);
    }

    public static HashSet<String> getMetadataTypes() {
        HashSet<String> values = new HashSet<String>();
        for (MetadataType c : MetadataType.values()) {
            values.add(c.name());
        }
        return values;
    }
}
