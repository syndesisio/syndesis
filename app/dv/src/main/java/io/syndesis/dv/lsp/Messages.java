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
package io.syndesis.dv.lsp;

import static io.syndesis.dv.StringConstants.DOT;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Localized messages for the {@code server rest} project.
 */
public final class Messages {

    /**
     * Messages relating to errors.
     */
    @SuppressWarnings("JavaLangClash")
    public enum Error {
        EMPTY_STATEMENT,
        INCOMPLETE_CREATE_VIEW_STATEMENT,
        STATEMENT_MUST_START_WITH_CREATE_VIEW,
        MISSING_VIEW_NAME,
        VIEW_NAME_RESERVED_WORD,
        VIEW_NAME_NON_RESERVED_WORD,
        MISSING_OPTION_VALUE,
        INVALID_OPTION_ID,
        INVALID_OPTION_VALUE,
        INVALID_OPTIONS_TOKENS,
        NO_TABLE_BODY_COLUMNS_DEFINED,
        INVALID_COLUMN_NAME,
        COLUMN_NAME_RESERVED_WORD,
        COLUMN_NAME_NON_RESERVED_WORD,
        VALUE_IS_NOT_INTEGER,
        INVALID_DATATYPE,
        INVALID_TOKEN_IN_TABLE_ELEMENT,
        INVALID_PRIMARY_KEY_ELEMENT,
        TABLE_ELEMENT_PROPERTY_ALREADY_SET,
        INVALID_TOKEN_IN_TABLE_SYMBOL,
        ALL_PARENS_DO_NOT_MATCH,
        ALL_BRACES_DO_NOT_MATCH,
        VIEW_NAME_IS_INVALID,
        PROJECTED_SYMBOLS_VIEW_COLUMNS_MISMATCH,
        NO_SELECT_CLAUSE_FOUND,
        INVALID_COLUMN_MISSING_COMMA,
        INCOMPLETE_PRIMARY_KEY,
        INCOMPLETE_FOREIGN_KEY,
        INVALID_TOKEN,
        MISSING_COMMA_SEPARATOR,
        INVALID_PRIMARY_KEY_COLUMN,
        INVALID_FOREIGN_KEY_COLUMN,
        MISSING_FK_TABLE_REF,
        INCOMPLETE_SCHEMA_REF,
        MISSING_FROM_KEYWORD,
        INVALID_TABLE_NAME,
        INVALID_TOKEN_EXPECTING_XXX_,
        NO_WITH_LIST_ELEMENTS,
        INCOMPLETE_WITH_LIST_ELEMENT,
        QUERY_EXPRESSION_MISSING_AS,
        INCOMPLETE_SELECT_EXPRESSION,
        UNEXPECTED_COMMA
    }

    private static final String BUNDLE_NAME = Messages.class.getPackage().getName() + DOT
                                              + Messages.class.getSimpleName().toLowerCase(Locale.US);

    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle( BUNDLE_NAME );

    /**
     * @param key
     *        the message key (cannot be <code>null</code>)
     * @param parameters
     *        the substitution parameters (can be <code>null</code>)
     * @return the localized message (never empty)
     */
    public static String getString( final Enum< ? > key,
                                    final Object... parameters ) {
        return io.syndesis.dv.utils.Messages.getString(key, RESOURCE_BUNDLE, parameters);
    }

    /**
     * Don't allow construction outside of this class.
     */
    private Messages() {
        // nothing to do
    }

}
