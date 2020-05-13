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

import org.teiid.query.parser.SQLParserConstants;

public final class DdlCompletionConstants {

    private static final  String[] VIEW_ITEM_DATA = {
            /*
             * The label of this completion item. By default also the text that is inserted
             * when selecting this completion.
             */
            getLabel(SQLParserConstants.VIEW, true), // String label;

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
            getLabel(SQLParserConstants.VIEW, true), // String insertText;
    };

    private static final  String[] CREATE_ITEM_DATA = {
            getLabel(SQLParserConstants.CREATE, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(SQLParserConstants.CREATE, true), // String insertText;
    };

    private static final  String[] VIRTUAL_ITEM_DATA = {
            getLabel(SQLParserConstants.VIRTUAL, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(SQLParserConstants.VIRTUAL, true), // String insertText;
    };

    private static final  String[] PROCEDURE_ITEM_DATA = {
            getLabel(SQLParserConstants.PROCEDURE, true),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(SQLParserConstants.PROCEDURE, true), // String insertText;
    };

    public static final Map<String, String[]> KEYWORDS_ITEM_DATA = Collections
            .unmodifiableMap(new HashMap<String, String[]>() {
                private static final long serialVersionUID = 1L;

                {
                    put(getLabel(SQLParserConstants.VIEW, true), VIEW_ITEM_DATA);
                    put(getLabel(SQLParserConstants.VIEW, true), CREATE_ITEM_DATA);
                    put(getLabel(SQLParserConstants.VIEW, true), VIRTUAL_ITEM_DATA);
//                    put(getLabel(VIEW, true), GLOBAL_ITEM_DATA);
//                    put(getLabel(VIEW, true), FOREIGN_ITEM_DATA);
//                    put(getLabel(VIEW, true), TABLE_ITEM_DATA);
//                    put(getLabel(VIEW, true), TEMPORARY_ITEM_DATA);
//                    put(getLabel(VIEW, true), ROLE_ITEM_DATA);
//                    put(getLabel(VIEW, true), SCHEMA_ITEM_DATA);
//                    put(getLabel(VIEW, true), SERVER_ITEM_DATA);
//                    put(getLabel(VIEW, true), DATABASE_ITEM_DATA);
                    put(getLabel(SQLParserConstants.VIEW, true), PROCEDURE_ITEM_DATA);
                }
            });

    /**
     * DATATYPES (in lower case)
     */

    private static final String[] STRING_ITEM_DATA = {
            getLabel(SQLParserConstants.STRING, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypeLength(SQLParserConstants.STRING) // String insertText;
    };

    private static final String[] VARBINARY_ITEM_DATA = {
            getLabel(SQLParserConstants.VARBINARY, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] VARCHAR_ITEM_DATA = {
            getLabel(SQLParserConstants.VARCHAR, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypeLength(SQLParserConstants.VARCHAR) // String insertText;
    };

    private static final String[] BOOLEAN_ITEM_DATA = {
            getLabel(SQLParserConstants.BOOLEAN, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] BYTE_ITEM_DATA = {
            getLabel(SQLParserConstants.BYTE, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] TINYINT_ITEM_DATA = {
            getLabel(SQLParserConstants.TINYINT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getLabel(SQLParserConstants.TINYINT, false) // String insertText;
    };

    private static final String[] SHORT_ITEM_DATA = {
            getLabel(SQLParserConstants.SHORT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] SMALLINT_ITEM_DATA = {
            getLabel(SQLParserConstants.SMALLINT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] CHAR_ITEM_DATA = {
            getLabel(SQLParserConstants.CHAR, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] INTEGER_ITEM_DATA = {
            getLabel(SQLParserConstants.INTEGER, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] LONG_ITEM_DATA = {
            getLabel(SQLParserConstants.LONG, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] BIGINT_ITEM_DATA = {
            getLabel(SQLParserConstants.BIGINT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] BIGINTEGER_ITEM_DATA = {
            getLabel(SQLParserConstants.BIGINTEGER, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] FLOAT_ITEM_DATA = {
            getLabel(SQLParserConstants.FLOAT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypePrecision(SQLParserConstants.FLOAT) // String insertText;
    };

    private static final String[] REAL_ITEM_DATA = {
            getLabel(SQLParserConstants.REAL, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] DOUBLE_ITEM_DATA = {
            getLabel(SQLParserConstants.DOUBLE, false),   // String label;

            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null, // String insertText;
    };

    private static final String[] BIGDECIMAL_ITEM_DATA = {
            getLabel(SQLParserConstants.BIGDECIMAL, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null  // String insertText;
    };

    private static final String[] DECIMAL_ITEM_DATA = {
            getLabel(SQLParserConstants.DECIMAL, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypePrecisionAndScale(SQLParserConstants.DECIMAL) // String insertText;
    };

    private static final String[] DATE_ITEM_DATA = {
            getLabel(SQLParserConstants.DATE, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] TIME_ITEM_DATA = {
            getLabel(SQLParserConstants.TIME, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypePrecision(SQLParserConstants.TIME) // String insertText;
    };

    private static final String[] TIMESTAMP_ITEM_DATA = {
            getLabel(SQLParserConstants.TIMESTAMP, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypePrecision(SQLParserConstants.TIMESTAMP) // String insertText;
    };

    private static final String[] BLOB_ITEM_DATA = {
            getLabel(SQLParserConstants.BLOB, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypeLength(SQLParserConstants.BLOB) // String insertText;
    };

    private static final String[] CLOB_ITEM_DATA = {
            getLabel(SQLParserConstants.CLOB, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            getBracketedDatatypeLength(SQLParserConstants.CLOB) // String insertText;
    };

    private static final String[] XML_ITEM_DATA = {
            getLabel(SQLParserConstants.XML, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] JSON_ITEM_DATA = {
            getLabel(SQLParserConstants.JSON, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] GEOMETRY_ITEM_DATA = {
            getLabel(SQLParserConstants.GEOMETRY, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] GEOGRAPHY_ITEM_DATA = {
            getLabel(SQLParserConstants.GEOGRAPHY, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    private static final String[] OBJECT_ITEM_DATA = {
            getLabel(SQLParserConstants.OBJECT, false),   // String label;
            null, // String detail;
            null, // Either<String, MarkupContent> documentation;
            null // String insertText;
    };

    public static final Map<String, String[]> DATATYPES_ITEM_DATA = Collections
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
                    put(getLabel(SQLParserConstants.STRING, false), STRING_ITEM_DATA);
                    put(getLabel(SQLParserConstants.VARBINARY, false), VARBINARY_ITEM_DATA);
                    put(getLabel(SQLParserConstants.VARCHAR, false), VARCHAR_ITEM_DATA);
                    put(getLabel(SQLParserConstants.BOOLEAN, false), BOOLEAN_ITEM_DATA);
                    put(getLabel(SQLParserConstants.BYTE, false), BYTE_ITEM_DATA);
                    put(getLabel(SQLParserConstants.TINYINT, false), TINYINT_ITEM_DATA);
                    put(getLabel(SQLParserConstants.SHORT, false), SHORT_ITEM_DATA);
                    put(getLabel(SQLParserConstants.SMALLINT, false), SMALLINT_ITEM_DATA);
                    put(getLabel(SQLParserConstants.CHAR, false), CHAR_ITEM_DATA);
                    put(getLabel(SQLParserConstants.INTEGER, false), INTEGER_ITEM_DATA);
                    put(getLabel(SQLParserConstants.LONG, false), LONG_ITEM_DATA);
                    put(getLabel(SQLParserConstants.BIGINT, false), BIGINT_ITEM_DATA);
                    put(getLabel(SQLParserConstants.BIGINTEGER, false), BIGINTEGER_ITEM_DATA);
                    put(getLabel(SQLParserConstants.FLOAT, false), FLOAT_ITEM_DATA);
                    put(getLabel(SQLParserConstants.REAL, false), REAL_ITEM_DATA);
                    put(getLabel(SQLParserConstants.DOUBLE, false), DOUBLE_ITEM_DATA);
                    put(getLabel(SQLParserConstants.BIGDECIMAL, false), BIGDECIMAL_ITEM_DATA);
                    put(getLabel(SQLParserConstants.DECIMAL, false), DECIMAL_ITEM_DATA);
                    put(getLabel(SQLParserConstants.DATE, false), DATE_ITEM_DATA);
                    put(getLabel(SQLParserConstants.TIME, false), TIME_ITEM_DATA);
                    put(getLabel(SQLParserConstants.TIMESTAMP, false), TIMESTAMP_ITEM_DATA);
                    put(getLabel(SQLParserConstants.BLOB, false), BLOB_ITEM_DATA);
                    put(getLabel(SQLParserConstants.CLOB, false), CLOB_ITEM_DATA);
                    put(getLabel(SQLParserConstants.XML, false), XML_ITEM_DATA);
                    put(getLabel(SQLParserConstants.JSON, false), JSON_ITEM_DATA);
                    put(getLabel(SQLParserConstants.GEOMETRY, false), GEOMETRY_ITEM_DATA);
                    put(getLabel(SQLParserConstants.GEOGRAPHY, false), GEOGRAPHY_ITEM_DATA);
                    put(getLabel(SQLParserConstants.OBJECT, false), OBJECT_ITEM_DATA);
                }
            });

    private DdlCompletionConstants() {
        // utility class
    }

     public static String getLabel(int keywordId, boolean upperCase) {
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
