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
package io.syndesis.dv.lsp.completion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import io.syndesis.dv.lsp.parser.DdlAnalyzerConstants;

public interface DdlCompletionConstants extends DdlAnalyzerConstants {

    int ALL_KIND = -1;
    int EMPTY_DDL = -2;

    String[] VIEW_ITEM_DATA = {
            /*
             * The label of this completion item. By default also the text that is inserted
             * when selecting this completion.
             */
            getLabel(VIEW, true), // String label;

            /*
             * A human-readable string with additional information about this item, like
             * type or symbol information.
             */
            null, // String detail;

            /*
             * A human-readable string that represents a doc-comment.
             */
            null, // Either<String, MarkupContent> documentation;

            /*
             * A string that should be inserted a document when selecting this completion.
             * When `falsy` the label is used.
             */
            getLabel(VIEW, true), // String insertText;
    };

    String[] CREATE_ITEM_DATA = {
            getLabel(CREATE, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(CREATE, true), // String insertText;
    };

    String[] VIRTUAL_ITEM_DATA = {
            getLabel(VIRTUAL, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(VIRTUAL, true), // String insertText;
    };

    String[] TABLE_ITEM_DATA = {
            getLabel(TABLE, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(TABLE, true), // String insertText;
    };

    String[] PROCEDURE_ITEM_DATA = {
            getLabel(PROCEDURE, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(PROCEDURE, true), // String insertText;
    };

    String[] GLOBAL_ITEM_DATA = {
            getLabel(GLOBAL, true),   // String label;d;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(GLOBAL, true), // String insertText;
    };

    String[] FOREIGN_ITEM_DATA = {
            getLabel(FOREIGN, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(FOREIGN, true), // String insertText;
    };

    String[] TRIGGER_ITEM_DATA = {
            getLabel(TRIGGER, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(TRIGGER, true), // String insertText;
    };

    String[] TEMPORARY_ITEM_DATA = {
            getLabel(TEMPORARY, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(TEMPORARY, true), // String insertText;
    };

    String[] ROLE_ITEM_DATA = {
            getLabel(ROLE, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(ROLE, true), // String insertText;
    };

    String[] SCHEMA_ITEM_DATA = {
            getLabel(SCHEMA, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(SCHEMA, true), // String insertText;
    };

    String[] SERVER_ITEM_DATA = {
            getLabel(SERVER, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(SERVER, true), // String insertText;
    };

    String[] DATABASE_ITEM_DATA = {
            getLabel(DATABASE, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(DATABASE, true), // String insertText;
    };

    String[] SELECT_ITEM_DATA = {
            getLabel(SELECT, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(SELECT, true), // String insertText;
    };

    static final Map<String, String[]> KEYWORDS_ITEM_DATA = Collections
            .unmodifiableMap(new HashMap<String, String[]>() {
                private static final long serialVersionUID = 1L;

                {
                    put(getLabel(VIEW, true), VIEW_ITEM_DATA);
                    put(getLabel(VIEW, true), CREATE_ITEM_DATA);
                    put(getLabel(VIEW, true), VIRTUAL_ITEM_DATA);
//                    put(getLabel(VIEW, true), GLOBAL_ITEM_DATA);
//                    put(getLabel(VIEW, true), FOREIGN_ITEM_DATA);
//                    put(getLabel(VIEW, true), TABLE_ITEM_DATA);
//                    put(getLabel(VIEW, true), TEMPORARY_ITEM_DATA);
//                    put(getLabel(VIEW, true), ROLE_ITEM_DATA);
//                    put(getLabel(VIEW, true), SCHEMA_ITEM_DATA);
//                    put(getLabel(VIEW, true), SERVER_ITEM_DATA);
//                    put(getLabel(VIEW, true), DATABASE_ITEM_DATA);
                    put(getLabel(VIEW, true), PROCEDURE_ITEM_DATA);
                }
            });

    /**
     * DATATYPES (in lower case)
     */

    String[] STRING_ITEM_DATA = {
            getLabel(STRING, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypeLength(STRING) // String insertText;
    };

    String[] VARBINARY_ITEM_DATA = {
            getLabel(VARBINARY, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] VARCHAR_ITEM_DATA = {
            getLabel(VARCHAR, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypeLength(VARCHAR) // String insertText;
    };

    String[] BOOLEAN_ITEM_DATA = {
            getLabel(BOOLEAN, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] BYTE_ITEM_DATA = {
            getLabel(BYTE, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] TINYINT_ITEM_DATA = {
            getLabel(TINYINT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(TINYINT, false) // String insertText;
    };

    String[] SHORT_ITEM_DATA = {
            getLabel(SHORT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] SMALLINT_ITEM_DATA = {
            getLabel(SMALLINT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] CHAR_ITEM_DATA = {
            getLabel(CHAR, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] INTEGER_ITEM_DATA = {
            getLabel(INTEGER, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] LONG_ITEM_DATA = {
            getLabel(LONG, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] BIGINT_ITEM_DATA = {
            getLabel(BIGINT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] BIGINTEGER_ITEM_DATA = {
            getLabel(BIGINTEGER, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] FLOAT_ITEM_DATA = {
            getLabel(FLOAT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypePrecision(FLOAT) // String insertText;
    };

    String[] REAL_ITEM_DATA = {
            getLabel(REAL, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] DOUBLE_ITEM_DATA = {
            getLabel(DOUBLE, false),   // String label;

            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null, // String insertText;
    };

    String[] BIGDECIMAL_ITEM_DATA = {
            getLabel(BIGDECIMAL, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null  // String insertText;
    };

    String[] DECIMAL_ITEM_DATA = {
            getLabel(DECIMAL, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypePrecisionAndScale(DECIMAL) // String insertText;
    };

    String[] DATE_ITEM_DATA = {
            getLabel(DATE, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] TIME_ITEM_DATA = {
            getLabel(TIME, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypePrecision(TIME) // String insertText;
    };

    String[] TIMESTAMP_ITEM_DATA = {
            getLabel(TIMESTAMP, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypePrecision(TIMESTAMP) // String insertText;
    };

    String[] BLOB_ITEM_DATA = {
            getLabel(BLOB, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypeLength(BLOB) // String insertText;
    };

    String[] CLOB_ITEM_DATA = {
            getLabel(CLOB, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypeLength(CLOB) // String insertText;
    };

    String[] XML_ITEM_DATA = {
            getLabel(XML, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] JSON_ITEM_DATA = {
            getLabel(JSON, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] GEOMETRY_ITEM_DATA = {
            getLabel(GEOMETRY, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] GEOGRAPHY_ITEM_DATA = {
            getLabel(GEOGRAPHY, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    String[] OBJECT_ITEM_DATA = {
            getLabel(OBJECT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    static final Map<String, String[]> DATATYPES_ITEM_DATA = Collections
            .unmodifiableMap(new HashMap<String, String[]>() {
                private static final long serialVersionUID = 1L;
                /*
                STRING, VARBINARY, VARCHAR, BOOLEAN, BYTE,
                 TINYINT, SHORT, SMALLINT, CHAR, INTEGER,
                 LONG, BIGINT, BIGINTEGER, FLOAT, REAL,
                 DOUBLE, BIGDECIMAL, DECIMAL, DATE, TIME,
                 TIMESTAMP, BLOB, CLOB, XML,
                 JSON, GEOMETRY, GEOGRAPHY, OBJECT
                 */
                {
                    put(getLabel(STRING, false), STRING_ITEM_DATA);
                    put(getLabel(VARBINARY, false), VARBINARY_ITEM_DATA);
                    put(getLabel(VARCHAR, false), VARCHAR_ITEM_DATA);
                    put(getLabel(BOOLEAN, false), BOOLEAN_ITEM_DATA);
                    put(getLabel(BYTE, false), BYTE_ITEM_DATA);
                    put(getLabel(TINYINT, false), TINYINT_ITEM_DATA);
                    put(getLabel(SHORT, false), SHORT_ITEM_DATA);
                    put(getLabel(SMALLINT, false), SMALLINT_ITEM_DATA);
                    put(getLabel(CHAR, false), CHAR_ITEM_DATA);
                    put(getLabel(INTEGER, false), INTEGER_ITEM_DATA);
                    put(getLabel(LONG, false), LONG_ITEM_DATA);
                    put(getLabel(BIGINT, false), BIGINT_ITEM_DATA);
                    put(getLabel(BIGINTEGER, false), BIGINTEGER_ITEM_DATA);
                    put(getLabel(FLOAT, false), FLOAT_ITEM_DATA);
                    put(getLabel(REAL, false), REAL_ITEM_DATA);
                    put(getLabel(DOUBLE, false), DOUBLE_ITEM_DATA);
                    put(getLabel(BIGDECIMAL, false), BIGDECIMAL_ITEM_DATA);
                    put(getLabel(DECIMAL, false), DECIMAL_ITEM_DATA);
                    put(getLabel(DATE, false), DATE_ITEM_DATA);
                    put(getLabel(TIME, false), TIME_ITEM_DATA);
                    put(getLabel(TIMESTAMP, false), TIMESTAMP_ITEM_DATA);
                    put(getLabel(BLOB, false), BLOB_ITEM_DATA);
                    put(getLabel(CLOB, false), CLOB_ITEM_DATA);
                    put(getLabel(XML, false), XML_ITEM_DATA);
                    put(getLabel(JSON, false), JSON_ITEM_DATA);
                    put(getLabel(GEOMETRY, false), GEOMETRY_ITEM_DATA);
                    put(getLabel(GEOGRAPHY, false), GEOGRAPHY_ITEM_DATA);
                    put(getLabel(OBJECT, false), OBJECT_ITEM_DATA);
                }
            });

     static String getLabel(int keywordId, boolean upperCase) {
         return DdlAnalyzerConstants.getLabel(keywordId, upperCase);
     }

     static String getKeyword(int keywordId) {
         return DdlAnalyzerConstants.getLabel(keywordId, false);
     }

     static String getBracketedDatatypeLength(int id) {
         return getLabel(id, false) + "(255)"; // InsertTextFormat insertTextFormat;
     }

     static String getBracketedDatatypePrecision(int id) {
         return getLabel(id, false) + "(10)"; // InsertTextFormat insertTextFormat;
     }

     static String getBracketedDatatypePrecisionAndScale(int id) {
         return getLabel(id, false)+ "(10, 2)"; // InsertTextFormat insertTextFormat;
     }
}
