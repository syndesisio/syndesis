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
package io.syndesis.dv;

public interface StringConstants {

    /**
     * An empty string array.
     */
    String[] EMPTY_ARRAY = new String[0];

    /**
     * An empty string
     */
    String EMPTY_STRING = ""; //$NON-NLS-1$

    /**
     * A space.
     */
    String SPACE = " "; //$NON-NLS-1$

    /**
     * A star.
     */
    String STAR = "*"; //$NON-NLS-1$

    /**
     * A percent.
     */
    String PERCENT = "%"; //$NON-NLS-1$

    /**
     * An underscore.
     */
    String UNDERSCORE = "_"; //$NON-NLS-1$

    /**
     * An underscore character.
     */
    char UNDERSCORE_CHAR = UNDERSCORE.charAt(0);

    /**
     * The String "\n"
     */
    String NEW_LINE = "\n"; //$NON-NLS-1$

    /**
     * The String "\t"
     */
    String TAB = "\t"; //$NON-NLS-1$

    /**
     * AT sign.
     */
    String AT = "@"; //$NON-NLS-1$

    /**
     * A Comma.
     */
    String COMMA = ","; //$NON-NLS-1$

    /**
     * A Colon.
     */
    String COLON = ":"; //$NON-NLS-1$

    /**
     * A Semi Colon.
     */
    String SEMI_COLON = ";"; //$NON-NLS-1$

    /**
     * A Hyphen.
     */
    String HYPHEN = "-"; //$NON-NLS-1$

    /**
     * A Dot.
     */
    String DOT = "."; //$NON-NLS-1$

    /**
     * A dot character.
     */
    char DOT_CHAR = DOT.charAt(0);

    /**
     * A Dollar Sign.
     */
    String DOLLAR_SIGN = "$"; //$NON-NLS-1$

    /**
     * A Speech mark.
     */
    String SPEECH_MARK = "\""; //$NON-NLS-1$

    /**
     * A Quote mark.
     */
    String QUOTE_MARK = "'"; //$NON-NLS-1$

    /**
     * A Question mark.
     */
    String QUESTION_MARK = "?"; //$NON-NLS-1$

    /**
     * Two Dots
     */
    String DOT_DOT = ".."; //$NON-NLS-1$

    /**
     * class
     */
    String CLASS = "class"; //$NON-NLS-1$

    /**
     * interface
     */
    String INTERFACE = "interface"; //$NON-NLS-1$

    /**
     * enum
     */
    String ENUM = "enum"; //$NON-NLS-1$

    /**
     * xml extension
     */
    String XML = "xml"; //$NON-NLS-1$

    /**
     * ddl extension
     */
    String DDL = "ddl"; //$NON-NLS-1$

    /**
     * The name of the System property that specifies the string that should be used to separate lines. This property is a standard
     * environment property that is usually set automatically.
     */
    String LINE_SEPARATOR_PROPERTY_NAME = "line.separator"; //$NON-NLS-1$

    /**
     * The String that should be used to separate lines; defaults to {@link #NEW_LINE}
     */
    String LINE_SEPARATOR = System.getProperty(LINE_SEPARATOR_PROPERTY_NAME, NEW_LINE);

    /**
     * Forward slash
     */
    String FS = "/"; //$NON-NLS-1$

    /**
     * Back slash used in regular expressions
     */
    String DOUBLE_BACK_SLASH = "\\"; //$NON-NLS-1$

    /**
     * Equals
     */
    String EQUALS = "="; //$NON-NLS-1$

    /**
     * Open Bracket
     */
    String OPEN_BRACKET = "("; //$NON-NLS-1$

    /**
     * Close Bracket
     */
    String CLOSE_BRACKET = ")"; //$NON-NLS-1$

    /**
     * Hash Symbol
     */
    String HASH = "#"; //$NON-NLS-1$

    /**
     * Ampersand Symbol
     */
    String AMPERSAND = "&"; //$NON-NLS-1$

    /**
     * Open Angle Bracket
     */
    String OPEN_ANGLE_BRACKET = "<"; //$NON-NLS-1$

    /**
     * Close Angle Bracket
     */
    String CLOSE_ANGLE_BRACKET = ">"; //$NON-NLS-1$

    /**
     * Open Square Bracket
     */
    String OPEN_SQUARE_BRACKET = "["; //$NON-NLS-1$

    /**
     * Close Square Bracket
     */
    String CLOSE_SQUARE_BRACKET = "]"; //$NON-NLS-1$

    /**
     * Open Brace
     */
    String OPEN_BRACE = "{"; //$NON-NLS-1$

    /**
     * Close Brace
     */
    String CLOSE_BRACE = "}"; //$NON-NLS-1$

    /**
     * Minus Sign
     */
    String MINUS = "-"; //$NON-NLS-1$

    /**
     * Plus Sign
     */
    String PLUS = "+"; //$NON-NLS-1$

    /**
     * Multiple Sign
     */
    String MULTIPLY = STAR;

    /**
     * Divide Sign
     */
    String DIVIDE = FS;

    /**
     * Pipe Sign
     */
    String PIPE = "|"; //$NON-NLS-1$

    /**
     * Breaking tag
     */
    String BR = "<br>"; //$NON-NLS-1$

    /**
     * Opening pre tag
     */
    String OPEN_PRE_TAG = "<pre>"; //$NON-NLS-1$

    /**
     * Closing pre tag
     */
    String CLOSE_PRE_TAG = "</pre>"; //$NON-NLS-1$

    /**
     * Non-breaking HTML space
     */
    String NBSP = "&nbsp;"; //$NON-NLS-1$

    /**
     * Open pre-cmt tag with class
     */
    String OPEN_PRE_CMT = "<pre-cmt class=\"json-comment\">"; //$NON-NLS-1$

    /**
     * Close pre-cmt tag
     */
    String CLOSE_PRE_CMT = "</pre-cmt>"; //$NON-NLS-1$

    /**
     * log
     */
    String LOG = "log"; //$NON-NLS-1$

    /**
     * Target directory
     */
    String TARGET = "target"; //$NON-NLS-1$

    /**
     * sources jar component
     */
    String SOURCES = "sources"; //$NON-NLS-1$

    /**
     * JAR File Extension
     */
    String JAR = "jar"; //$NON-NLS-1$

    /**
     * JAVA File Extension
     */
    String JAVA = "java"; //$NON-NLS-1$

    /**
     * ZIP File Extension
     */
    String ZIP = "zip"; //$NON-NLS-1$

    /**
     * Vdb Prefix
     */
    String VDB_PREFIX = "Vdb-"; //$NON-NLS-1$

    /**
     * XML Suffix
     */
    String XML_SUFFIX = DOT + XML;

    /**
     * ZIP Suffix
     */
    String ZIP_SUFFIX = DOT + ZIP;

    /**
     * Vdb Deployment Suffix
     */
    String VDB_DEPLOYMENT_SUFFIX = "-vdb" + XML_SUFFIX; //$NON-NLS-1$

    /**
     * Connection Suffix
     */
    String CONNECTION_SUFFIX = "-connection.xml"; //$NON-NLS-1$

    /**
     * Java temp directory
     */
    String JAVA_IO_TMPDIR = "java.io.tmpdir"; //$NON-NLS-1$

    /**
     * Service vdb suffix
     */
    String SERVICE_VDB_SUFFIX = "vdb"; //$NON-NLS-1$

    String ID_LABEL = "id"; //$NON-NLS-1$
}
