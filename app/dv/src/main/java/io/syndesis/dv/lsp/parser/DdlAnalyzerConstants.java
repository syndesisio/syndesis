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

import org.teiid.query.parser.SQLParserConstants;

public interface DdlAnalyzerConstants extends SQLParserConstants {

    enum STATEMENT_TYPE {
        UNKNOWN_STATEMENT_TYPE,
        CREATE_TABLE_TYPE,
        CREATE_FOREIGN_TABLE_TYPE,
        CREATE_FOREIGN_TEMPORARY_TABLE_TYPE,
        CREATE_GLOBAL_TEMPORARY_TABLE_TYPE,
        CREATE_VIEW_TYPE,
        CREATE_VIRTUAL_VIEW_TYPE
    }

    enum CONTEXT {
        NONE_FOUND,
        PREFIX,
        TABLE_BODY,
        TABLE_OPTIONS,
        TABLE_ELEMENT,
        TABLE_ELEMENT_OPTIONS,
        TABLE_ELEMENT_OPTION_SEARCHABLE,
        QUERY_EXPRESSION,
//        AS_TOKEN,
        SELECT_CLAUSE,
        FROM_CLAUSE,
        WHERE_CLAUSE
    }

    int[] DATATYPES = {
        STRING,
        VARBINARY,
        VARCHAR,
        BOOLEAN,
        BYTE,
        TINYINT,
        SHORT,
        SMALLINT,
        CHAR,
        INTEGER,
        LONG,
        BIGINT,
        BIGINTEGER,
        FLOAT,
        REAL,
        DOUBLE,
        BIGDECIMAL,
        DECIMAL,
        DATE,
        TIME,
        TIMESTAMP,
        BLOB,
        CLOB,
        XML,
        JSON,
        GEOMETRY,
        GEOGRAPHY,
        OBJECT
    };

    String[] DATATYPE_LIST = {
        getLabel(STRING, false),
        getLabel(VARBINARY, false),
        getLabel(VARCHAR, false),
        getLabel(BOOLEAN, false),
        getLabel(BYTE, false),
        getLabel(TINYINT, false),
        getLabel(SHORT, false),
        getLabel(SMALLINT, false),
        getLabel(CHAR, false),
        getLabel(INTEGER, false),
        getLabel(LONG, false),
        getLabel(BIGINT, false),
        getLabel(BIGINTEGER, false),
        getLabel(FLOAT, false),
        getLabel(REAL, false),
        getLabel(DOUBLE, false),
        getLabel(BIGDECIMAL, false),
        getLabel(DECIMAL, false),
        getLabel(DATE, false),
        getLabel(TIME, false),
        getLabel(TIMESTAMP, false),
        getLabel(BLOB, false),
        getLabel(CLOB, false),
        getLabel(XML, false),
        getLabel(JSON, false),
        getLabel(GEOMETRY, false),
        getLabel(GEOGRAPHY, false),
        getLabel(OBJECT, false)
    };

    /*
     * Array of tokens that match the start of a CREATE TABLE statement
     */
    int[] CREATE_TABLE_STATEMENT = { CREATE, TABLE };

    /*
     * Array of tokens that match the start of a CREATE FOREIGNT TABLE statement
     */
    int[] CREATE_FOREIGN_TABLE_STATEMENT = { CREATE, FOREIGN, TABLE };

    /*
     * Array of tokens that match the start of a CREATE FOREIGN TEMPORARY TABLE
     * statement
     */
    int[] CREATE_FOREIGN_TEMPORARY_TABLE_STATEMENT = { CREATE, FOREIGN, TEMPORARY, TABLE };

    /*
     * Array of tokens that match the start of a CREATE TEMPORARY TABLE
     * statement
     */
    int[] CREATE_GLOBAL_TEMPORARY_TABLE_STATEMENT = { CREATE, GLOBAL, TEMPORARY, TABLE };

    /*
     * Array of tokens that match the start of a CREATE VIEW
     * statement
     */
    int[] CREATE_VIEW_STATEMENT = { CREATE, VIEW };

    /*
     * Array of tokens that match the start of a CREATE VIRTUAL VIEW
     * statement
     */
    int[] CREATE_VIRTUAL_VIEW_STATEMENT = { CREATE, VIRTUAL, VIEW };

    int[] COLUMN_DEFINITION_EXTRAS = {
            AUTO_INCREMENT,
            DEFAULT_KEYWORD,
            NOT,
            NULL,
            PRIMARY,
            KEY,
            INDEX,
            UNIQUE
    };

    /*
     * The getLabel(...] call is returning strings wrapped in double-quotes
     *
     * Need to return a simple string
     * @param tokenImageString string
     * @return string without double quotes
     */
     static String getLabel(int keywordId, boolean upperCase) {
         String tokenImageStr = tokenImage[keywordId];
         if( upperCase ) {
             return tokenImageStr.substring(1, tokenImageStr.length()-1).toUpperCase();
         }
         return tokenImageStr.substring(1, tokenImageStr.length()-1);
     }
}
